package disparse.parser.exceptions;

public class OptionRequiresValue extends RuntimeException {
    public OptionRequiresValue(String message) {
        super(message);
    }
}
