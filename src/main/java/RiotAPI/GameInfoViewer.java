package RiotAPI;

import ChampionParsing.ChampionSynonymMatcher;
import SpellParsing.SpellCooldownMatcher;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameInfo;
import net.rithms.riot.api.endpoints.spectator.dto.CurrentGameParticipant;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that interacts with the Riot API to view the summoner's current game
 * information.
 */
public class GameInfoViewer {

  private Summoner summoner;
  private String apiKey;
  private ApiConfig config;
  private RiotApi api;
  private Platform region;
  private Map<Integer, String> idsToChamps;
  private CurrentGameInfo currentGame;
  private int myTeamId;
  private int enemyTeamId;

  public SpellCooldownMatcher getSpellCooldownMatcher() {
    return spellCooldownMatcher;
  }

  public void setSpellCooldownMatcher(SpellCooldownMatcher spellCooldownMatcher) {
    this.spellCooldownMatcher = spellCooldownMatcher;
  }

  public ChampionSynonymMatcher getChampionSynonymMatcher() {
    return championSynonymMatcher;
  }

  public void setChampionSynonymMatcher(ChampionSynonymMatcher championSynonymMatcher) {
    this.championSynonymMatcher = championSynonymMatcher;
  }

  private SpellCooldownMatcher spellCooldownMatcher;
  private ChampionSynonymMatcher championSynonymMatcher;

  /**
   * Basic constructor.
   *
   * @param summonerName the name of the summoner who is timing spells.
   * @param region       the region that the summoner is in (currently only supports NA)
   * @param champIds     the map from champion ids to their names.
   * @throws NoSummonerInfoException if the summoner does not exist
   * @throws NoGameInfoException if the summoner is not in game
   */
  public GameInfoViewer(String summonerName, Platform region, Map<Integer, String> champIds) throws NoSummonerInfoException, NoGameInfoException {
    apiKey = Dotenv.load().get("API_KEY");
    config = new ApiConfig().setKey(apiKey);
    api = new RiotApi(config);
    this.region = region;
    this.idsToChamps = champIds;

    //need 2 different try block here because they both throw RiotApiException
    //for different reasons.

    try {
      summoner = api.getSummonerByName(region, summonerName);
    } catch (RiotApiException e) {
      if (e.getErrorCode() == 404) {
        throw new NoSummonerInfoException(summonerName);
      }
    }

    try {
      currentGame = api.getActiveGameBySummoner(region, summoner.getId());
      myTeamId = currentGame.getParticipantByParticipantId(summoner.getId()).getTeamId();
      if (myTeamId == 100) {
        enemyTeamId = 200;
      } else {
        enemyTeamId = 100;
      }
    } catch (RiotApiException e) {
      if (e.getErrorCode() == 404) {
        throw new NoGameInfoException(summonerName);
      }
    }


  }

  /**
   * Method that returns all of the enemy champion names.
   *
   * @return a list containing the enemy champion names, in no order.
   * @throws RiotApiException if the game cannot be found.
   */
  public List<String> getEnemyChampionNames() {
    List<String> opponents = new ArrayList<>();
    List<CurrentGameParticipant> players = currentGame.getParticipants();
    for (CurrentGameParticipant player : players) {
      if (player.getTeamId() == enemyTeamId) {
        opponents.add(idsToChamps.get(player.getChampionId()));
      }
    }
    return opponents;
  }

  /**
   * Method to check for the presence of a certain rune on the enemy team.
   *
   * @param runeId the ID of the rune we are looking for
   * @return a map from champion name to a boolean; true if that champion has
   * the rune, and false otherwise.
   * @throws RiotApiException if the game cannot be found.
   */
  public Map<String, Boolean> enemyHasRune(long runeId) {
    Map<String, Boolean> hasRune = new HashMap<>();

    List<CurrentGameParticipant> players = currentGame.getParticipants();
    for (CurrentGameParticipant player : players) {

      if (player.getTeamId() == enemyTeamId) {
        //identifies champion name of each opposing player
        String opponent = idsToChamps.get(player.getChampionId());
        List<Long> runes = player.getPerks().getPerkIds();

        System.out.println(opponent + " -> " + runes.contains(runeId));
        //populates map whether they have cosmic insight or not.
        hasRune.put(opponent, runes.contains(runeId));
      }
    }
    return hasRune;
  }
}
