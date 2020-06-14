package disparse.utils.help;

public class PageNumberOutOfBounds extends Exception {

  public PageNumberOutOfBounds(int pageNumber, int pages) {
    super(
        String.format(
            "The specified page number **%d** is not within the range of valid pages.  The valid pages "
                + "are between **1** and **%d**.",
            pageNumber, pages));
  }
}
