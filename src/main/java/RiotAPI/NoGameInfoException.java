package RiotAPI;

/**
 * Exception thrown when the user tries to start a timer for a summoner
 * that is not currently in game.
 */
public class NoGameInfoException extends Exception {

  /**
   * Basic constructor.
   * @param summonerName the summoner name who tried to start the timer
   */
  public NoGameInfoException(String summonerName) {
    super(summonerName + " isn't in game right now.");
  }
}
