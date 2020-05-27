package ChampionParsing;

/**
 * Exception thrown when the ChampionSynonymMatcher does not find a champion
 * name corresponding to the one said by the user.
 *
 * @author cnivera
 */
public class ChampionNotFoundException extends Exception {

  /**
   * Basic constructor.
   * @param userInput the user champ name that they spoke.
   */
  public ChampionNotFoundException(String userInput) {
    super("No champion was found for " + userInput);
  }
}
