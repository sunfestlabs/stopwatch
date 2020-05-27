package SpellParsing;

import ChampionParsing.ChampionWithSynonyms;;

/**
 * Interface to be implemented by objects that match the spell that has been
 * uttered by the user to its cooldown. For the first version, I'm using
 * statically coded variables (e.g. Flash always has 300s cooldown). But maybe
 * in the future, it's possible to get exact values through live builds and runes.
 */
public interface SpellCooldownMatcher {

  /**
   * Returns the cooldown of a spell based on whether the player has Lucidity
   * boots or cosmic insight, which reduce the cooldown of a summoner spell.
   *
   * @param spell the SummonerSpell whose cooldown to find
   * @param champion the champion whose summ we are timing
   * @return a double representing the full cooldown of the spell, in seconds.
   */
  double getCooldownOf(SummonerSpell spell, ChampionWithSynonyms champion);

  /**
   * Same as the getCooldownOf() method, but instead of returning the cooldown,
   * returns the full spell.
   * @param spellName the transcribed name of the spell
   * @return a SummonerSpell object representing that spell.
   * @throws SpellDoesNotExistException if the spell name is not recognized as any synonym.
   */
  SummonerSpell findActualSpell(String spellName) throws SpellDoesNotExistException;

}
