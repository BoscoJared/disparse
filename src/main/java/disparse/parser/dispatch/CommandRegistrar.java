package disparse.parser.dispatch;

import disparse.parser.Flag;
import disparse.parser.ParsedOutput;
import disparse.parser.Parser;
import disparse.parser.Types;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CommandRegistrar {
    public static CommandRegistrar registrar = new CommandRegistrar();

    private HashMap<String, Method> commandTable = new HashMap<>();
    private HashMap<String, List<Flag>> commandToFlags = new HashMap<>();
    private List<Method> injectables = new ArrayList<>();

    private CommandRegistrar() {
    }

    public void register(String command, Method method) {
        this.commandToFlags.putIfAbsent(command, new ArrayList<>());
        this.commandTable.put(command, method);
    }

    public void register(String command, Flag flag) {
        this.commandToFlags.putIfAbsent(command, new ArrayList<>());
        this.commandToFlags.get(command).add(flag);
    }

    public void register(Method method) {
        this.injectables.add(method);
    }

    public void dispatch(List<String> args, Object... injectables) {

        Parser parser = new Parser(this.commandToFlags);
        ParsedOutput output = parser.parse(args);
        String command = output.getCommand();
        if (!commandTable.containsKey(command)) {
            return;
        }
        try {
            Method handler = commandTable.get(command);
            Object[] objs = new Object[handler.getParameterTypes().length];

            int i = 0;
            for (Class<?> clazz : handler.getParameterTypes()) {
                if (clazz.isAnnotationPresent(ParsedEntity.class)) {
                    Constructor<?> entityCtor = clazz.getDeclaredConstructors()[0];
                    entityCtor.setAccessible(true);
                    Object newObject = entityCtor.newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(disparse.parser.reflection.Flag.class)) {
                            disparse.parser.reflection.Flag flagAnnotation =
                                    field.getAnnotation(disparse.parser.reflection.Flag.class);

                            disparse.parser.Flag flag =
                                    Utils.createFlagFromAnnotation(field, flagAnnotation);
                            if (output.getOptions().containsKey(flag)) {
                                field.setAccessible(true);
                                Object val = output.getOptions().get(flag);
                                if (flag.getType().equals(Types.INT)) {
                                    field.set(newObject, Integer.parseInt((String) val));
                                } else {
                                    field.set(newObject, val);
                                }
                            }
                        }
                    }
                    objs[i] = newObject;
                } else {
                    if (clazz.isAssignableFrom(List.class)) {
                        objs[i] = args;
                        continue;
                    }
                    for (Object injectable : injectables) {
                        if (clazz.isAssignableFrom(injectable.getClass())) {
                            objs[i] = injectable;
                        }
                    }

                    for (Method injectable : this.injectables) {
                        if (clazz.isAssignableFrom(injectable.getReturnType())) {
                            objs[i] = injectable.invoke(null);
                        }
                    }
                }
                i++;
            }
            handler.setAccessible(true);
            Constructor<?> ctor = handler.getDeclaringClass().getDeclaredConstructor();
            ctor.setAccessible(true);
            Object handlerObj = ctor.newInstance();
            handler.invoke(handlerObj, objs);
        } catch (Exception exec) {
            exec.printStackTrace();
        }

    }
}
