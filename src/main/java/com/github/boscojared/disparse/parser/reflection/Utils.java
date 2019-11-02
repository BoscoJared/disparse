package com.github.boscojared.disparse.parser.reflection;

import com.github.boscojared.disparse.parser.CommandFlag;
import com.github.boscojared.disparse.parser.Types;
import java.lang.reflect.Field;
import java.util.List;
public class Utils {

    public static CommandFlag createFlagFromAnnotation(Field field, Flag annotation) {
        String longName = annotation.longName();
        Character shortName = annotation.shortName();
        boolean required = annotation.required();
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

        String description = annotation.description();

        return new CommandFlag(longName, shortName, type, required, description);
    }
}
