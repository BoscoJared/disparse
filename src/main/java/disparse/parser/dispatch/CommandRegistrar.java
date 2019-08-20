package disparse.parser.dispatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import disparse.parser.exceptions.OptionRequired;
import java.util.*;

import disparse.discord.Helpable;
import disparse.parser.*;
import disparse.parser.reflection.Detector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Utils;

public class CommandRegistrar<E> {
    private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);

    private static final Flag HELP_FLAG = new Flag("help", 'h', Types.BOOL, false, "show usage of a particular command");
    private static final Command HELP_COMMAND = new Command("help", "show all commands or detailed help of one command");

    public static CommandRegistrar registrar = new CommandRegistrar();
    private HashMap<Command, Method> commandTable = new HashMap<>();
    private HashMap<Command, Set<Flag>> commandToFlags = new HashMap<>();
    private List<Method> injectables = new ArrayList<>();

    private CommandRegistrar() {
        this.commandToFlags.put(HELP_COMMAND, Set.of());
        this.commandTable.put(HELP_COMMAND, null);
    }

    public void register(Command command, Method method) {
        this.commandToFlags.putIfAbsent(command, new HashSet<>());
        this.commandTable.put(command, method);
    }

    public void register(Command command, Flag flag) {
        this.commandToFlags.putIfAbsent(command, new HashSet<>());
        this.commandToFlags.get(command).add(flag);
        this.commandToFlags.get(command).add(HELP_FLAG);
    }

    public void register(Method method) {
        this.injectables.add(method);
    }

    public void dispatch(List<String> args, Helpable<E> helper, E event, Object... injectables) {

        Parser parser = new Parser(this.commandToFlags);
        ParsedOutput output = parser.parse(args);
        Command command = output.getCommand();
        if (!commandTable.containsKey(command)) {
            return;
        }

        for (Command c : commandTable.keySet()) {
            if (c.equals(command)) {
                command = c;
            }
        }

        boolean help = (boolean) output.getOptions().getOrDefault(HELP_FLAG, false);
        if (command.getCommandName().equals("help")) {
            if (args.size() > 0) {
                String name = args.get(0);
                for (Command c : commandTable.keySet()) {
                    if (c.getCommandName().equals(name)) {
                        command = c;
                    }
                }
                help = true;
            } else {
                helper.allCommands(event, commandTable.keySet());
                return;
            }
        }
        if (help) {
            helper.help(event, command, commandToFlags.get(command));
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
                    for (Field field : Detector.allImplicitFields(clazz)) {
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
                            } else if (flag.isRequired()) {
                                throw new OptionRequired(flag + " is required for command to be ran!");
                            }
                        }
                    }
                    objs[i] = newObject;
                } else {
                    if (clazz.isAssignableFrom(List.class)) {
                        objs[i] = args;
                    }
                    if (clazz.isAssignableFrom(event.getClass())) {
                        objs[i] = event;
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
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException exec) {
            logger.error("Error occured", exec);
        }
    }
}
