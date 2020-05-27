package FileParsing;

/**
 * A class representing the exception thrown when the CSV file has
 * an improper header.
 */
public class MalformedHeaderException extends Exception {

  /**
   * Basic constructor.
   *
   * @param errorMessage the error message to be thrown.
   */
  public MalformedHeaderException(String errorMessage) {
    super(errorMessage);
  }

}
