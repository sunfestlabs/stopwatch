package ChampionParsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class representing a champion, which contains information such as a name and
 * possible mistaken words for that champion's name.
 *
 * NOTE: this is separate from the Champion class belonging to the riot-api-java
 * dependency.
 *
 * @author cnivera
 */
public class ChampionWithSynonyms {

  private String name;
  private Set<String> synonyms;

  /**
   * Basic constructor.
   * @param champName the name of the champion (e.g. Ezreal)
   * @param altNames the alternative pronounciation of the champ (e.g. Azrael)
   */
  public ChampionWithSynonyms(String champName, String[] altNames){
    this.name = champName;
    this.synonyms = new HashSet<>();
    synonyms.addAll(Arrays.asList(altNames));
  }

  /**
   * Getter for a champion's name.
   * @return this champion's name
   */
  public String getName() {
    return name;
  }

  public Set<String> getSynonyms() {
    return synonyms;
  }

  /**
   * Method that checks to see whether a given synonym is contained in the
   * champion's synonym set.
   * @param synonym the synonym to check for
   * @return true if the synonym is contained in the set, and false otherwise.
   */
  public boolean matches(String synonym) {
    return synonyms.contains(synonym);
  }
}
