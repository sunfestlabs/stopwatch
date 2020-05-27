package SpellParsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a summoner spell. Similar to the ChampionWithSynonyms class, it contains
 * a name and synonyms. These objects will also contain their own cooldowns.
 *
 * @author cnivera
 */
public class SummonerSpell {

  private int cooldown;
  private String name;
  private Set<String> synonyms;

  /**
   * Basic constructor.
   *
   * @param cooldown the cooldown of the summoner spell
   * @param name     the name of the spell
   * @param synonyms any alternate synonyms (e.g. teleport => "tp")
   */
  public SummonerSpell(int cooldown, String name, String[] synonyms) {
    this.cooldown = cooldown;
    this.name = name;
    this.synonyms = new HashSet<>(Arrays.asList(synonyms));
  }

  //Getters.

  public int getCooldown() {
    return cooldown;
  }

  public String getName() {
    return name;
  }

  /**
   * Method that checks whether the synonym is recognized as a valid synonym
   * for this summoner spell. As of now, synonyms are arbitrarily generated
   * by myself - I came up with a list of synonyms for each summoner spell.
   *
   * @param synonym the word needed to be verified as a synonym
   * @return true if the word is a recognized synonym of the spell, and false
   * otherwise.
   */
  public boolean matches(String synonym) {
    return synonyms.contains(synonym);
  }


}
