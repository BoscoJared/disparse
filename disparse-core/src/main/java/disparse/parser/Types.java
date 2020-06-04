package disparse.parser;

import java.util.Set;

public enum Types {
    INT,
    INT_LIST,
    STR,
    STR_LIST,
    BOOL,
    ENUM,
    ENUM_LIST;

    public static Set<Types> allLists() {
        return Set.of(INT_LIST, STR_LIST, ENUM_LIST);
    }
}
