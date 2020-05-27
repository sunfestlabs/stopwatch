package ChampionParsing;

/**
 * Interface for classes that will take in the champion name from JS (which will
 * come from the Dialogflow API) and match it to an actual champion object.
 *
 * Making this an interface because for v1.0, doing this in a pretty naive way,
 * by just listing out some arbitrary synonyms in a CSV file and then just checking
 * for string equality. Perhaps there is a better way to do this in the future.
 *
 * @author cnivera
 */
public interface ChampionSynonymMatcher {

  /**
   * Function for matching the transcribed champion name to the actual champion
   * object.
   * @param champName the name, as spoken by the user and transcribed by Dialogflow.
   * @return the ChampionWithSynonyms object representing that spoken name, if it exists.
   * @throws ChampionNotFoundException if a champion with such a name is not found.
   */
  ChampionWithSynonyms findActualChampion(String champName) throws ChampionNotFoundException;
}
