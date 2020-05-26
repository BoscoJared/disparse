package disparse.parser.dispatch;

import disparse.parser.Command;

import java.util.Objects;

public class IdentityCommandPair {
    private final String identity;
    private final Command command;

    public IdentityCommandPair(String identity, Command command) {
        this.identity = identity;
        this.command = command;
    }

    public String getIdentity() {
        return identity;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityCommandPair that = (IdentityCommandPair) o;
        return identity.equals(that.identity) &&
                command.equals(that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, command);
    }
}
