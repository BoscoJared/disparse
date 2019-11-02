package com.github.boscojared.disparse.parser;

import java.util.List;
import java.util.Map;

public class ParsedOutput {
    private final Command command;
    private final List<String> arguments;
    private final Map<CommandFlag, Object> options;

    public ParsedOutput(final Command command, final List<String> arguments,
            final Map<CommandFlag, Object> options) {
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

    public Map<CommandFlag, Object> getOptions() {
        return options;
    }
}
