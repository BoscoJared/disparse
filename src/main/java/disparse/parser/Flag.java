package disparse.parser;

import java.util.Objects;

public class Flag {
    private final String longName;
    private final Character shortName;
    private final boolean isRequired;
    private final Types type;
    private final String description;

    public Flag(final String longName, final Character shortName, final Types type, final boolean isRequired, final String description) {
        this.longName = longName;
        this.shortName = shortName;
        this.type = type;
        this.isRequired = isRequired;
        this.description = description;
    }

    public String getLongName() {
        return longName;
    }

    public Character getShortName() {
        return shortName;
    }

    public Types getType() {
        return type;
    }

    public boolean isRequired() { return isRequired; }

    public String getDescription() { return description; }

    @Override
    public String toString() {
        return getLongName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Flag flag = (Flag) o;
        return longName.equals(flag.longName) && Objects.equals(shortName, flag.shortName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longName, shortName);
    }
}
