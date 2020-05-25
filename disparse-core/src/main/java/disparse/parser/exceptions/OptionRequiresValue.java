package disparse.parser.exceptions;

import disparse.parser.CommandFlag;

public class OptionRequiresValue extends RuntimeException {

    private CommandFlag flag;

    public OptionRequiresValue(String message, CommandFlag flag) {
        super(message);
        this.flag = flag;
    }

    public CommandFlag getFlag() {
        return flag;
    }
}
