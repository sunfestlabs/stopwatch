package FileParsing;

import ChampionParsing.ChampionWithSynonyms;
import SpellParsing.SummonerSpell;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class representing a CSVParser object, which is used to read data in from
 * a .csv file and, in this case, obtain champion/spell names and synonyms.
 */
public class CSVParser {


  /**
   * Basic constructor.
   */
  public CSVParser() {

  }

  /**
   * A method used to parse CSV files. For the sake of this project, the CSV
   * data is read line by line and each champion is added to a list.
   *
   * @param filename the CSV file to be read
   */
  public List<ChampionWithSynonyms> parseChampionFile(String filename) {
    BufferedReader breader;
    List<ChampionWithSynonyms> champs = new ArrayList<>();
    try {
      breader = new BufferedReader(new FileReader(filename));
      String header = breader.readLine();
      int lineCount = 0;
      if (!header.equals("ChampName,Synonyms")) {
        throw new MalformedHeaderException("ERROR: Header "
            + "of CSV file is malformed.");
      } else {
        String line = breader.readLine();
        while (line != null) {
          //separates the CSV line into the champion name and a semicolon-separated
          //string representing its synonyms.
          String[] oneChamp = line.split(",");
          String champName = oneChamp[0];
          String semicolonSynonyms = oneChamp[1];

          //split the semicolon-separated string into its individual components
          String[] champSynonyms = semicolonSynonyms.split(";");
          ChampionWithSynonyms newChamp = new ChampionWithSynonyms(champName, champSynonyms);
          champs.add(newChamp);
          lineCount++;
          line = breader.readLine();
        }
        System.out.println("Read " + lineCount + " champions from " + filename);
      }
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: File " + filename + " not found.");
    } catch (MalformedHeaderException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.print("ERROR: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ERROR: A champion in the CSV file has malformed format.");
    }
    return champs;
  }


  /**
   * Similar to the above method, except this method parses the champions synonyms
   * into a hashmap from LOWERCASED champion name to the corresponding object.
   *
   * @param filename the filepath to the champion csv file
   * @return a map from champion names to their ChampionWithSynonyms objects.
   */
  public Map<String, ChampionWithSynonyms> parseChampionsToMap(String filename) {
    BufferedReader breader;
    Map<String, ChampionWithSynonyms> namesToChamps = new HashMap<>();
    try {
      breader = new BufferedReader(new FileReader(filename));
      String header = breader.readLine();
      int lineCount = 0;
      if (!header.equals("ChampName,Synonyms")) {
        throw new MalformedHeaderException("ERROR: Header "
            + "of CSV file is malformed.");
      } else {
        String line = breader.readLine();
        while (line != null) {
          //separates the CSV line into the champion name and a semicolon-separated
          //string representing its synonyms.
          String[] oneChamp = line.split(",");
          String champName = oneChamp[0];
          String semicolonSynonyms = oneChamp[1];

          //split the semicolon-separated string into its individual components
          String[] champSynonyms = semicolonSynonyms.split(";");
          ChampionWithSynonyms newChamp = new ChampionWithSynonyms(champName, champSynonyms);
          namesToChamps.put(champName, newChamp);
          lineCount++;
          line = breader.readLine();
        }
        System.out.println("Read " + lineCount + " champions from " + filename);
      }
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: File " + filename + " not found.");
    } catch (MalformedHeaderException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.print("ERROR: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ERROR: A champion in the CSV file has malformed format.");
    }
    return namesToChamps;
  }

  /**
   * Method used to parse a CSV file containing summoner spell names, synonyms,
   * and cooldowns. This information will be transformed into SummonerSpell
   * objects for the backend to use.
   *
   * @param filename the file location of the csv file
   * @return a list of summoner spells using the information in the CSV file.
   */
  public List<SummonerSpell> parseSpellFile(String filename) {
    BufferedReader breader;
    List<SummonerSpell> spells = new ArrayList<>();
    try {
      breader = new BufferedReader(new FileReader(filename));
      String header = breader.readLine();
      int lineCount = 0;
      if (!header.equals("SpellName,Synonyms,Cooldown")) {
        throw new MalformedHeaderException("ERROR: Header "
            + "of CSV file is malformed.");
      } else {
        String line = breader.readLine();
        while (line != null) {
          //separates the CSV line into the spell name and a semicolon-separated
          //string representing its synonyms.
          String[] oneSpell = line.split(",");
          String spellName = oneSpell[0];
          String semicolonSynonyms = oneSpell[1];
          int cooldown = Integer.parseInt(oneSpell[2]);

          //split the semicolon-separated string into its individual components
          String[] spellSynonyms = semicolonSynonyms.split(";");
          SummonerSpell newSpell = new SummonerSpell(cooldown, spellName, spellSynonyms);
          spells.add(newSpell);
          lineCount++;
          line = breader.readLine();
        }
        System.out.println("Read " + lineCount + " spells from " + filename);
      }
    } catch (FileNotFoundException e) {
      System.out.println("ERROR: File " + filename + " not found.");
    } catch (MalformedHeaderException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.print("ERROR: " + e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("ERROR: A spell in the CSV file has malformed format.");
    }
    return spells;
  }

}
