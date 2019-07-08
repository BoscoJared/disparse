package disparse.parser.reflection;

import disparse.parser.Types;

import java.lang.reflect.Field;
import java.util.List;

public class Utils {

    public static disparse.parser.Flag createFlagFromAnnotation(Field field, Flag annotation) {
        String longName = annotation.longName();
        Character shortName = annotation.shortName();
        if (shortName == ' ') {
            shortName = null;
        }
        Types type = null;
        if (field.getType().isAssignableFrom(Integer.class)) {
            type = Types.INT;
        } else if (field.getType().isAssignableFrom(String.class)) {
            type = Types.STR;
        } else if (field.getType().isAssignableFrom(List.class)) {
            type = Types.LIST;
        } else {
            type = Types.BOOL;
        }

        return new disparse.parser.Flag(longName, shortName, type);
    }
}
