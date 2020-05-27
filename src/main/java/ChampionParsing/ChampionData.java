package ChampionParsing;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding instances of champion objects refering to each champion.
 *
 * Potentially thinking of moving this to SQL or some other data managing method
 * @author cnivera
 */
public class ChampionData {

  //list containing all champions. This should be populated by the CSVParser's
  //parseFile method.
  public static List<ChampionWithSynonyms> allChamps = new ArrayList<>();
}
