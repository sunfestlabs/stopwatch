package RiotAPI;

/**
 * Exception thrown when information for the given summoner name couldn't
 * be found.
 */
public class NoSummonerInfoException extends Exception {

  /**
   * Basic constructor.
   * @param summName the invalid summoner name
   */
  public NoSummonerInfoException(String summName) {
    super("Summoner not found with name " + summName);
  }

}
