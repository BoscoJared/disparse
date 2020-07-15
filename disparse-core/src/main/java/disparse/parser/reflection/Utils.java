package disparse.parser.reflection;

import disparse.parser.CommandFlag;
import disparse.parser.Types;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

  public static CommandFlag createFlagFromAnnotation(Field field, Flag annotation) {
    String longName = annotation.longName();
    Character shortName = annotation.shortName();
    boolean required = annotation.required();
    if (shortName == ' ') {
      shortName = null;
    }

    if (longName.isEmpty()) {
      longName = null;
    }

    Map<String, String> choices = Map.of();

    Types type;
    if (field.getType().isAssignableFrom(Integer.class) || field.getType() == Integer.TYPE) {
      type = Types.INT;
    } else if (field.getType().isAssignableFrom(String.class)) {
      type = Types.STR;
    } else if (field.getType().isAssignableFrom(List.class)) {
      Type[] genericTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
      if (genericTypes.length == 0) {
        type = Types.STR_LIST;
      } else {
        Class<?> genericClass = (Class<?>) genericTypes[0];
        if (genericClass.isAssignableFrom(Integer.class)) {
          type = Types.INT_LIST;
        } else if (genericClass.isEnum()) {
          type = Types.ENUM_LIST;
          choices = new HashMap<>();
          for (ChoiceMapping mapping : annotation.choices()) {
            choices.put(mapping.userChoice(), mapping.mapTo());
          }
        } else {
          type = Types.STR_LIST;
        }
      }
    } else if (field.getType().isEnum()) {
      type = Types.ENUM;
      choices = new HashMap<>();
      for (ChoiceMapping mapping : annotation.choices()) {
        choices.put(mapping.userChoice(), mapping.mapTo());
      }
    } else {
      type = Types.BOOL;
    }

    String description = annotation.description();

    return new CommandFlag(longName, shortName, type, required, description, choices);
  }
}
