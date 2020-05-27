package SpellParsing;

/**
 * Exception thrown by SpellCooldownMatchers when a spell is given that we
 * do not know of.
 */
public class SpellDoesNotExistException extends Exception {

  /**
   * Basic constructor.
   * @param spellName the name of the spell
   */
  public SpellDoesNotExistException(String spellName) {
    super("No spell with name " + spellName + " found.");
  }
}
