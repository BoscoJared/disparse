package disparse.parser;

import java.util.Objects;

public class Command {

    private final String name;
    private final String description;
    private final String[] roles;

    public Command(final String name, final String description) {
        this(name, description, new String[0]);
    }
    
    public Command(final String name, final String description, final String[] roles) {
        this.name = name;
        this.description = description;
        this.roles = roles;
    }

    public String getCommandName() { return this.name; }

    public String getDescription() { return this.description; }
    
    public String[] getRoles() { return this.roles; }

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
