package ChampionParsing;

import java.util.List;

/**
 * Naive way of checking for champion synonyms. With this class, I compare the
 * transcribed audio snipped to a list of pre-defined synonyms for each champion.
 * If the transcription matches one of my synonyms, I return the corresponding
 * champion object.
 *
 * e.g. snippet = "Azrael" matches a synonym for Ezreal, so I'd return
 * Ezreal.
 */
public class SimpleSynonymMatcher implements ChampionSynonymMatcher {

  private List<ChampionWithSynonyms> championWithSynonyms;

  /**
   * Basic constructor.
   * @param championWithSynonyms a list of all championWithSynonyms whose synonyms to match on. this
   *                  will pretty much be a list of all champs in the game.
   */
  public SimpleSynonymMatcher(List<ChampionWithSynonyms> championWithSynonyms) {
    this.championWithSynonyms = championWithSynonyms;
  }

  @Override
  public ChampionWithSynonyms findActualChampion(String transcription) throws ChampionNotFoundException {
    //In this naive implementation, we look through our list of championWithSynonyms one by one
    //and see whether the given champion has a synonym matching the transcription.

    //In the future, might be worth matching first letter (to cut down on number of iterations)
    //or do some other optimization.

    for (ChampionWithSynonyms champ : championWithSynonyms){
      if (champ.matches(transcription)) {
        return champ;
      }
    }
    throw new ChampionNotFoundException(transcription);
  }

}
