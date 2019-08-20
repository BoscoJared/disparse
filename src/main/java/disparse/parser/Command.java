package disparse.parser;

import java.util.Objects;

public class Command {

    private final String name;
    private final String description;

    public Command(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getCommandName() { return this.name; }

    public String getDescription() { return this.description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return name.equals(command.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
