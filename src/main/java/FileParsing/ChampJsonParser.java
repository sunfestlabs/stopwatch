package FileParsing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for parsing the ddragon file containing Riot's champion info into a
 * JSON object we can read.
 */
public class ChampJsonParser {

  /**
   * Method that converts a JSON file into a map from champion keys -> champion names.
   * @param filePath the string representing the filepath to the JSON file.
   * @return a map from champion keys to their names.
   * @throws FileNotFoundException if the JSON file cannot be found
   */
  public Map<Integer, String> idMapFromJson(String filepath) throws FileNotFoundException {
    Map<Integer, String> idsToChamps = new HashMap<>();

    //filling the ids-to-champs hashmap by reading champion.json.
    //this hashmap will be used to match the champion whose spell we want to
    //time with the enemy team's champions.
    JsonParser parser = new JsonParser();
    JsonObject champJson = parser.parse(new FileReader(filepath)).getAsJsonObject();
    JsonObject champData = champJson.getAsJsonObject("data");
    for (String champName : champData.keySet()) {
      int champId = champData.getAsJsonObject(champName).get("key").getAsInt();
      System.out.println("id: " + champId + ", champ name " + champName);
      idsToChamps.put(champId, champName);
    }
    return idsToChamps;
  }

}
