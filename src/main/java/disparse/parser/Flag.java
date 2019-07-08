package disparse.parser;

import java.util.Objects;

public class Flag {
    private final String longName;
    private final Character shortName;
    private final Types type;

    public Flag(final String longName,
                final Character shortName,
                final Types type) {
        this.longName = longName;
        this.shortName = shortName;
        this.type = type;
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

    @Override
    public String toString() {
        return getLongName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flag flag = (Flag) o;
        return longName.equals(flag.longName) &&
                Objects.equals(shortName, flag.shortName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longName, shortName);
    }
}
