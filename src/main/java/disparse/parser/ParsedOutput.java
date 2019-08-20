package disparse.parser;

import java.util.List;
import java.util.Map;

public class ParsedOutput {
    private final Command command;
    private final List<String> arguments;
    private final Map<Flag, Object> options;

    public ParsedOutput(final Command command, final List<String> arguments,
            final Map<Flag, Object> options) {
        this.command = command;
        this.arguments = arguments;
        this.options = options;
    }

    public Command getCommand() {
        return command;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public Map<Flag, Object> getOptions() {
        return options;
    }
}
