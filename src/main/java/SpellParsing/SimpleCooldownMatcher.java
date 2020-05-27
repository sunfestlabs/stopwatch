package SpellParsing;

import ChampionParsing.ChampionWithSynonyms;
import RiotAPI.GameInfoViewer;
import RiotAPI.NoGameInfoException;
import RiotAPI.NoSummonerInfoException;
import io.github.cdimascio.dotenv.Dotenv;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.constant.Platform;
import java.util.List;
import java.util.Map;

/**
 * Class that recognizes spell names and has knowledge of their total cooldowns.
 * In this version, cooldowns are hard-coded, but perhaps it is possible
 * later to obtain accurate values based on real-time builds (do they have
 * Cosmic Insight? Lucidity Boots?)
 *
 * @author cnivera
 */
public class SimpleCooldownMatcher implements SpellCooldownMatcher {

  private List<SummonerSpell> spells;
  private String apiKey;
  private ApiConfig config;
  private RiotApi api;
  private Map<Integer, String> idsToChampNames;
  private Map<String, Boolean> hasCosmicInsight;

  //rune ID for the Cosmic Insight rune, which grants 5% CDR on summs.
  private static long COSMIC_INSIGHT = 8347;


  /**
   * Basic constructor.
   *
   * @param spells   a list of summoner spells
   * @param champIds the map from champion ids to their names.
   * @param summName the current player's summoner name (the one tracking summs)
   * @throws NoGameInfoException if the summoner is not in game
   * @throws NoSummonerInfoException if the summoner cannot be found
   */
  public SimpleCooldownMatcher(List<SummonerSpell> spells, Map<Integer, String> champIds, String summName) throws NoGameInfoException, NoSummonerInfoException {
    this.spells = spells;

    //initializing Riot API credentials to access match data
    apiKey = Dotenv.load().get("API_KEY");
    config = new ApiConfig().setKey(apiKey);
    idsToChampNames = champIds;
    api = new RiotApi(config);
    GameInfoViewer spectator = new GameInfoViewer(summName, Platform.NA, idsToChampNames);
    hasCosmicInsight = spectator.enemyHasRune(COSMIC_INSIGHT);

  }


  @Override
  public double getCooldownOf(SummonerSpell spell, ChampionWithSynonyms champion) {
    double cooldown = spell.getCooldown();
    if (hasCosmicInsight.get(champion.getName())) {
      return cooldown * 0.95;
    } else {
      return cooldown;
    }
  }

  @Override
  public SummonerSpell findActualSpell(String spellName) throws SpellDoesNotExistException {
    for (SummonerSpell spell : spells) {
      if (spell.matches(spellName)) {
        return spell;
      }
    }
    throw new SpellDoesNotExistException(spellName);
  }

}
