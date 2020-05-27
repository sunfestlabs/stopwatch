import ChampionParsing.*;
import FileParsing.CSVParser;
import FileParsing.ChampJsonParser;
import RiotAPI.GameInfoViewer;
import RiotAPI.NoGameInfoException;
import RiotAPI.NoSummonerInfoException;
import SpellParsing.SimpleCooldownMatcher;
import SpellParsing.SpellCooldownMatcher;
import SpellParsing.SpellDoesNotExistException;
import SpellParsing.SummonerSpell;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.constant.Platform;
import spark.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Main {

  private static final int INTERNAL_SERVER_ERROR = 500;
  private static final int PORT_NUM = 6789;
  private static Map<String, ChampionWithSynonyms> namesToChamps = new HashMap<>();
  private static Map<String, GameInfoViewer> currentGames = new HashMap<>();
  private static List<SummonerSpell> spells = new ArrayList<>();
  private static Map<Integer, String> idsToChamps = new HashMap<>();
  private static final Gson GSON = new Gson();
  private static final String CHAMP_CSV = "data/ChampionData/namesSynonyms.csv";
  private static final String SPELL_CSV = "data/SpellData/spellSynonyms.csv";
  private static final String CHAMP_JSON = "data/ChampionData/champion.json";
  private static List<ChampionWithSynonyms> champs = new ArrayList<>();

  //Statically initiated Champion and Spell name matchers in order to avoid
  //re-declaration every time the handler is called.

  private static ChampionSynonymMatcher champMatcher;
  private static SpellCooldownMatcher spellMatcher;

  public static void main(String[] args) {
    try {
      CSVParser parser = new CSVParser();
      ChampJsonParser jsonParser = new ChampJsonParser();
      champs = parser.parseChampionFile(CHAMP_CSV);
      namesToChamps = parser.parseChampionsToMap(CHAMP_CSV);
      spells = parser.parseSpellFile(SPELL_CSV);
      idsToChamps = jsonParser.idMapFromJson(CHAMP_JSON);
      runSparkServer();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }


  private static void runSparkServer() {
    Spark.port(PORT_NUM);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());
    Spark.post("/nameValidator", new NameValidateHandler());
    Spark.post("/gameStart", new OnGameStartHandler());
  }

  /**
   * Class for handling requests from the bot to check the champion name, spell
   * name, and spell cooldown from the bot. This handler is pinged by both
   * timer inquiries and timer start requests.
   */
  private static class NameValidateHandler implements Route {

    @Override
    public String handle(Request req, Response res) {
      //removing quotation marks and lowercasing input to make synonym checking consistent
      JsonObject json = new JsonParser().parse(req.body()).getAsJsonObject();

      String requestType = json.get("type").getAsString();
      String champName = json.get("champName").getAsString().toLowerCase();
      String spellName = json.get("spellName").getAsString().toLowerCase();
      String voiceId = json.get("voiceChannel").getAsString();
      //depending on the request type, want to do something different.
      JsonObject ret = new JsonObject();
      GameInfoViewer currentGame = currentGames.get(voiceId);
      try {
        ChampionWithSynonyms championWithSynonyms = currentGame.getChampionSynonymMatcher().findActualChampion(champName);
        SummonerSpell spell = currentGame.getSpellCooldownMatcher().findActualSpell(spellName);
        ret.addProperty("champName", championWithSynonyms.getName());
        ret.addProperty("spellName", spell.getName());

        if (requestType.equals("timer")) {
          ret.addProperty("type", "timer");
          System.out.println("getting timer");
          ret.addProperty("spellCooldown", currentGame.getSpellCooldownMatcher().getCooldownOf(spell, championWithSynonyms));
        } else if (requestType.equals("inquiry")) {
          ret.addProperty("type", "inquiry");
        }
      } catch (ChampionNotFoundException e) {
        ret.addProperty("statusCode", "failure");
        ret.addProperty("statusMsg", "Champion not found.");
        System.out.println("can't find champ");
        return GSON.toJson(ret);
      } catch (SpellDoesNotExistException e) {
        ret.addProperty("statusCode", "failure");
        ret.addProperty("statusMsg", "Spell not found.");
        System.out.println("can't find spell");
        return GSON.toJson(ret);
      }
      System.out.println("returning");
      ret.addProperty("statusCode", "success");
      return GSON.toJson(ret);
    }
  }

  /**
   * A handler function that is called when the user starts the timer for their
   * match (i.e. when the user types "!timer <summName>" in Discord. This will
   * use Riot's API to obtain data about the match, namely the enemy team's
   * champions.
   */
  private static class OnGameStartHandler implements Route {
    @Override
    public Object handle(Request req, Response response) {
      JsonObject json = new JsonParser().parse(req.body()).getAsJsonObject();
      String summName = json.get("summName").getAsString();
      String voiceId = json.get("voiceChannel").getAsString();
      JsonObject ret = new JsonObject();

      List<ChampionWithSynonyms> enemyChamps = new ArrayList<>();
      try {

        GameInfoViewer spectator = new GameInfoViewer(summName, Platform.NA, idsToChamps);
        List<String> enemyChampNames = spectator.getEnemyChampionNames();

        //for each champion name (String), find the corresponding ChampionWithSynonyms
        //and add it to the list.
        for (String name : enemyChampNames) {
          System.out.println(name);
          enemyChamps.add(namesToChamps.get(name));
        }

        //adding roles manually
        enemyChamps.add(namesToChamps.get("Top"));
        enemyChamps.add(namesToChamps.get("Jungle"));
        enemyChamps.add(namesToChamps.get("Mid"));
        enemyChamps.add(namesToChamps.get("ADC"));
        enemyChamps.add(namesToChamps.get("Support"));

        //setting matcher objects for this summoner's GameInfoViewer object
        spectator.setChampionSynonymMatcher(new SimpleSynonymMatcher(enemyChamps));
        spectator.setSpellCooldownMatcher(new SimpleCooldownMatcher(spells, idsToChamps, summName));

        //adds the GameInfoViewer to a dictionary from Discord voice channel id -> GameInfoViewer
        currentGames.put(voiceId, spectator);

        ret.addProperty("statusCode", "success");

      } catch (NoGameInfoException | NoSummonerInfoException e) {
        ret.addProperty("statusCode", "failure");
        ret.addProperty("statusMsg", e.getMessage());
      }
      return GSON.toJson(ret);
    }
  }


  /**
   * A handler to print an Exception as text into the Response.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    public void handle(Exception e, Request req, Response res) {
      res.status(INTERNAL_SERVER_ERROR);
      StringWriter stacktrace = new StringWriter();
      PrintWriter pw = new PrintWriter(stacktrace);
      pw.println("<pre>");
      e.printStackTrace(pw);
      pw.println("</pre>");
      pw.close();
      res.body(stacktrace.toString());
    }
  }

  static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
  }
}
