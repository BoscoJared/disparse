package disparse.parser.dispatch;

import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDiscordResponse;
import disparse.discord.AbstractDispatcher;
import disparse.discord.manager.CooldownManager;
import disparse.parser.*;
import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequired;
import disparse.parser.exceptions.OptionRequiresValue;
import disparse.parser.reflection.Detector;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistrar<E, T> {

    public static final CommandRegistrar REGISTRAR = new CommandRegistrar<>();
    private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);
    private final CommandFlag helpFlag =
            new CommandFlag("help", 'h', Types.BOOL, false, "show usage of a particular command", Map.of());
    private final Command helpCommand =
            new Command("help", "show all commands or detailed help of one command", false);
    private final CommandFlag helpPageFlag =
            new CommandFlag("page", 'p', Types.INT, false, "select a specific page to showcase", Map.of());
    private final HashMap<Command, Method> commandTable = new HashMap<>();
    private final HashMap<Command, Set<CommandFlag>> commandToFlags = new HashMap<>();
    private final List<Method> injectables = new ArrayList<>();
    private final Map<Command, CommandContainer> disabledCommands = new HashMap<>();

    private CommandRegistrar() {
        this.commandToFlags.put(helpCommand, Set.of(helpPageFlag));
        this.commandTable.put(helpCommand, null);
    }

    public void register(Command command, Method method) {
        this.commandToFlags.putIfAbsent(command, new HashSet<>());
        this.commandToFlags.get(command).add(helpFlag);
        this.commandTable.put(command, method);

        for (String alias : command.getAliases()) {
            Command aliasCommand = new Command(alias,
                    command.getDescription(),
                    command.getRoles(),
                    command.canBeDisabled(),
                    command.getCooldownDuration(),
                    command.getScope(),
                    command.isSendCooldownMessage(),
                    command.getAcceptFrom(),
                    new String[]{},
                    command.getPerms(),
                    command.getUsageExamples());
            aliasCommand.setParentName(command.getCommandName());
            register(aliasCommand, method);
        }
    }

    public void register(Command command, CommandFlag flag) {
        this.commandToFlags.putIfAbsent(command, new HashSet<>());
        this.commandToFlags.get(command).add(flag);

        for (String alias : command.getAliases()) {
            Command aliasCommand = new Command(alias,
                    command.getDescription(),
                    command.getRoles(),
                    command.canBeDisabled(),
                    command.getCooldownDuration(),
                    command.getScope(),
                    command.isSendCooldownMessage(),
                    command.getAcceptFrom(),
                    new String[]{},
                    command.getPerms(),
                    command.getUsageExamples());
            aliasCommand.setParentName(command.getCommandName());
            register(aliasCommand, flag);
        }
    }

    public void register(Method method) {
        this.injectables.add(method);
    }

    public void dispatch(List<String> args, AbstractDispatcher<E, T> helper, E event) {
        List<String> originalArgs = new ArrayList<>(args);
        ParsedOutput parsedOutput = this.parse(args, helper, event);
        if (parsedOutput == null) return;

        String commandName = parsedOutput.getCommand().getCommandName();
        Command command = this.commandTable.keySet()
                .stream()
                .filter(c -> c.getCommandName().equals(commandName))
                .findFirst()
                .orElse(null);
        if (!commandTable.containsKey(command)) return;
        if (helper.commandRolesNotMet(event, command) || helper.commandIntentsNotMet(event, command)) {
            helper.roleNotMet(event, command);
            return;
        }

        if (this.help(originalArgs, helper, event, parsedOutput, command)) return;

        if (!canSendTo(helper, event, command)) return;

        if (!helper.isEnabledForGuild(event, command)) return;

        try {
            this.emitCommand(args, helper, event, parsedOutput, command);
        } catch (ReflectiveOperationException exec) {
            logger.error("Error occurred", exec);
        } catch (OptionRequired exec) {
            helper.optionRequired(event, exec.getCommand(), exec.getFlag());
        } catch (Exception exec) {
            logger.error("Unhandled exception: ", exec);
        }
    }

    private boolean canSendTo(AbstractDispatcher<E, T> helper, E event, Command command) {
        IncomingScope acceptFrom = command.getAcceptFrom();

        switch(acceptFrom) {
            case ALL:
                return true;
            case CHANNEL:
                return helper.isSentFromChannel(event);
            case DM:
                return helper.isSentFromDM(event);
            default:
                return false;
        }
    }

    private boolean help(List<String> args, AbstractDispatcher<E, T> helper, E event, ParsedOutput parsedOutput, Command command) {
        String parentName = command.getParentName();
        if (parentName != null) {
            command = this.commandTable.keySet()
                    .stream()
                    .filter(c -> c.getCommandName().equals(parentName))
                    .findFirst()
                    .orElse(null);
        }
        if (command == null) return false;

        if (command.getCommandName().equalsIgnoreCase("help")) {
            this.helpCommand(args, helper, event, parsedOutput);
            return true;
        } else if ((boolean) parsedOutput.getOptions().getOrDefault(helpFlag, false)) {
            this.emitHelp(args, helper, event, command);
            return true;
        }

        return false;
    }

    private void helpCommand(List<String> args, AbstractDispatcher<E, T> helper, E event, ParsedOutput parsedOutput) {
        args = new ArrayList<>(args);

        if (parsedOutput.getArguments().size() < 1) {
            int pageLimit = Integer.parseInt((String) parsedOutput.getOptions().getOrDefault(helpPageFlag, "1"));
            helper.allCommands(event, commandTable.keySet(), pageLimit);
            return;
        }

        Command foundCommand = null;
        args.remove(0); // remove "help" command name
        String name = args.get(0);
        name = String.join(".", name.split(" "));

        for (Command c : commandTable.keySet()) {
            if (c.getCommandName().equals(name)) {
                foundCommand = c;
            }
        }

        if (foundCommand == null) { // foundCommand is null so we need to prefix match to see if we can provide more help
            foundCommand = this.prefixHelp(args, helper, event, name);
        }

        if (foundCommand == null) return; // no prefix was matched so we can't really help

        String parentName = foundCommand.getParentName();

        if (parentName != null) {
            foundCommand = this.commandTable.keySet()
                    .stream()
                    .filter(c -> c.getCommandName().equals(parentName))
                    .findFirst()
                    .orElse(foundCommand);
        }
        this.emitHelp(args, helper, event, foundCommand);
    }

    private Command prefixHelp(List<String> args, AbstractDispatcher<E, T> helper, E event, String prefix) {
        PrefixContainer prefixContainer = findCommandPrefixes(commandTable.keySet(), args);
        List<Command> prefixes = prefixContainer.getPrefixes();
        String foundPrefix = prefixContainer.getFoundPrefix();
        if (prefixes.size() == 0) {
            helper.commandNotFound(event, prefix.replace(".", " "));
        } else if (prefixes.size() == 1 && !helper.commandRolesNotMet(event, prefixes.get(0))) {
            return prefixes.get(0);
        } else {
            helper.helpSubcommands(event, foundPrefix, prefixes);
        }
        return null;
    }

    private void emitHelp(List<String> args, AbstractDispatcher<E, T> helper, E event, Command command) {
        List<String> translatedArgs = new ArrayList<>();
        Parser parser = new Parser(this.commandToFlags);
        translatedArgs.add("help");
        translatedArgs.addAll(args);
        ParsedOutput parsedOutput = parser.parse(translatedArgs);
        // This should only fail due to programmer error, so the cast *should* be safe... famous last words
        int pageLimit = Integer.parseInt((String) parsedOutput.getOptions().getOrDefault(helpPageFlag, "1"));
        helper.help(event, command, commandToFlags.get(command), commandTable.keySet(), pageLimit);
    }

    private void emitCommand(List<String> args, AbstractDispatcher<E, T> helper, E event, ParsedOutput parsedOutput, Command foundCommand)
            throws ReflectiveOperationException, OptionRequired {
        Method commandHandler = commandTable.get(foundCommand);

        if (isOnCooldown(foundCommand, helper, event)) {
            return;
        }

        String realCommandName = foundCommand.getParentName();
        if (realCommandName == null) {
            realCommandName = foundCommand.getCommandName();
        }

        if (!helper.runMiddleware(event, realCommandName)) {
            return;
        }

        Object[] objects = new Object[commandHandler.getParameterTypes().length];

        int i = 0;
        for (Class<?> clazz : commandHandler.getParameterTypes()) {
            if (clazz.isAnnotationPresent(ParsedEntity.class)) {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                constructor.setAccessible(true);
                Object newObject = constructor.newInstance();

                for (Field field : Detector.allImplicitFields(clazz)) {
                    if (field.isAnnotationPresent(Flag.class)) {
                        Flag flagAnnotation = field.getAnnotation(Flag.class);

                        CommandFlag flag = Utils.createFlagFromAnnotation(field, flagAnnotation);
                        if (parsedOutput.getOptions().containsKey(flag)) {
                            Object val = parsedOutput.getOptions().get(flag);
                            field.setAccessible(true);

                            if (flag.getType().equals(Types.INT)) {
                                try {
                                    field.set(newObject, Integer.parseInt((String) val));
                                } catch (NumberFormatException numberFormatException) {
                                    helper.flagRequiresInt(event, flag, (String) val);
                                    return;
                                }
                            } else if (flag.getType().equals(Types.ENUM)) {
                                Map<String, String> choices = flag.getChoices();
                                String choice = choices.getOrDefault(val, (String) val);
                                try {
                                    field.set(newObject, Enum.valueOf((Class<Enum>) field.getType(), choice));
                                } catch (IllegalArgumentException illegalArgumentException) {
                                    String name = flagAnnotation.longName();
                                    if (name.equals("")) {
                                        name = String.valueOf(flagAnnotation.shortName());
                                    }
                                    String options = choices.keySet().stream().map(s -> "`" + s + "`").collect(Collectors.joining(", "));
                                    helper.incorrectOption(event, (String) val, name, options);
                                    return;
                                }
                            } else if (flag.getType().equals(Types.INT_LIST)) {
                                List<Integer> ints = new ArrayList<>();
                                for (String str : (List<String>) val) {
                                    try {
                                        ints.add(Integer.parseInt(str));
                                    } catch (NumberFormatException numberFormatException) {
                                        helper.flagRequiresInt(event, flag, str);
                                        return;
                                    }
                                }
                                field.set(newObject, ints);
                            } else if (flag.getType().equals(Types.ENUM_LIST)) {
                                List<Enum<?>> enums = new ArrayList<>();
                                Map<String, String> choices = flag.getChoices();
                                Class<?> genericTypes = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                for (String str : (List<String>) val) {
                                    String choice = choices.getOrDefault(str, str);
                                    try {
                                        enums.add(Enum.valueOf((Class<Enum>) genericTypes, choice));
                                    } catch (IllegalArgumentException illegalArgumentException) {
                                        String name = flagAnnotation.longName();
                                        if (name.equals("")) {
                                            name = String.valueOf(flagAnnotation.shortName());
                                        }
                                        String options = choices.keySet().stream().map(s -> "`" + s + "`").collect(Collectors.joining(", "));
                                        helper.incorrectOption(event, choice, name, options);
                                        return;
                                    }
                                }
                                field.set(newObject, enums);
                            } else {
                                field.set(newObject, val);
                            }
                        } else if (flag.isRequired()) {
                            throw new OptionRequired("The flag `--" + flag + "` is required for `"
                                    + foundCommand.getCommandName() + "` to be ran!", foundCommand, flag);
                        }
                    }
                }
                objects[i] = newObject;
            } else {
                this.fillObjectArr(objects, i, clazz, args, event, helper);
            }
            i++;
        }
        commandHandler.setAccessible(true);
        Object handlerObj = null; // null can work for static methods invocation
        Constructor<?>[] ctors = commandHandler.getDeclaringClass().getDeclaredConstructors();
        Arrays.sort(ctors, Comparator.comparing(Constructor::getParameterCount, Comparator.reverseOrder()));

        Object[] bestCtorParams = null;
        Constructor<?> bestCtor = null;
        int bestNonNull = Integer.MAX_VALUE;

        for (Constructor<?> ctor : ctors) {
            Object[] ctorParams = new Object[ctor.getParameterCount()];
            int idx = 0;

            for (Class<?> clazz : ctor.getParameterTypes()) {
                this.fillObjectArr(ctorParams, idx, clazz, args, event, helper);
                idx++;
            }

            boolean noneNull = Arrays.stream(ctorParams).noneMatch(Objects::isNull);

            if (noneNull) {
                bestCtor = ctor;
                bestCtorParams = ctorParams;
                break;
            } else {
                int amountNotNull = (int) Arrays.stream(ctorParams).filter(Objects::isNull).count();
                if (ctor.getParameterCount() > 0 && amountNotNull < bestNonNull) {
                    bestCtor = ctor;
                    bestCtorParams = ctorParams;
                }
            }
        }

        if (bestCtor != null) {
            bestCtor.setAccessible(true);
            handlerObj = bestCtor.newInstance(bestCtorParams);
        }

        if (commandHandler.getReturnType().equals(String.class)) {
            String msg = (String) commandHandler.invoke(handlerObj, objects);
            helper.sendMessage(event, msg);
        } else if (commandHandler.getReturnType() == helper.createBuilder().getClass()) {
            T builder = (T) commandHandler.invoke(handlerObj, objects);
            helper.sendEmbed(event, builder);
        } else if (AbstractDiscordResponse.class.isAssignableFrom(commandHandler.getReturnType())) {
            AbstractDiscordResponse<T> response = (AbstractDiscordResponse<T>) commandHandler.invoke(handlerObj, objects);
            response.getOptionalMessage().ifPresent(msg -> helper.sendMessage(event, msg));
            response.getOptionalBuilder().ifPresent(builder -> helper.sendEmbed(event, builder));
        } else {
            commandHandler.invoke(handlerObj, objects);
        }

        cooldown(foundCommand, helper, event);
    }

    private void cooldown(Command command, AbstractDispatcher<E, T> helper, E event) {
        if (!command.getCooldownDuration().isZero()) {
            CooldownCompositeKey<String> cooldownCompositeKey = createPairWithScope(command, helper, event);
            helper.getCooldownManager().cooldown(cooldownCompositeKey);
        }
    }

    private boolean isOnCooldown(Command command, AbstractDispatcher<E, T> helper, E event) {
        Duration cooldownDuration = command.getCooldownDuration();
        CooldownManager cooldownManager = helper.getCooldownManager();
        CooldownScope scope = command.getScope();

        if (!cooldownDuration.isZero()) {
            CooldownCompositeKey<String> cooldownCompositeKey = createPairWithScope(command, helper, event);
            String cooldownMessage = scope.getCooldownMessage();
            Duration left = cooldownManager.timeLeft(cooldownCompositeKey, cooldownDuration);

            if (!left.isZero()) {
                if (command.isSendCooldownMessage()) {
                    helper.sendMessage(event, cooldownMessage);
                }
                return true;
            }
        }

        return false;
    }

    private CooldownCompositeKey<String> createPairWithScope(Command command, AbstractDispatcher<E, T> helper, E event) {
        String commandName = command.getParentName();
        if (commandName == null) {
            commandName = command.getCommandName();
        }

        String guildId = helper.guildFromEvent(event);
        switch (command.getScope()) {
            case USER:
                return CooldownCompositeKey.of(guildId, helper.identityFromEvent(event), commandName);
            case CHANNEL:
                return CooldownCompositeKey.of(guildId, helper.channelFromEvent(event), commandName);
            case GUILD:
                return CooldownCompositeKey.of(guildId, null, commandName);
        }

        return CooldownCompositeKey.of(null, null, null);
    }

    private void fillObjectArr(Object[] objects, int index, Class<?> clazz, List<String> args, E event, AbstractDispatcher<E, T> helper)
            throws ReflectiveOperationException {
        if (clazz.isAssignableFrom(List.class)) {
            objects[index] = args;
        }

        if (clazz.isAssignableFrom(event.getClass())) {
            objects[index] = event;
        }

        if (clazz.isAssignableFrom(helper.getClass())) {
            objects[index] = helper;
        }

        if (AbstractDiscordRequest.class.isAssignableFrom(clazz)) {
            objects[index] = helper.createRequest(event, args);
        }

        for (Method injectable : this.injectables) {
            if (clazz.isAssignableFrom(injectable.getReturnType())) {
                objects[index] = injectable.invoke(null);
            }
        }
    }

    private ParsedOutput parse(List<String> args, AbstractDispatcher<E, T> helper, E event) {
        Parser parser = new Parser(this.commandToFlags);

        try {
            return parser.parse(args);
        } catch (NoCommandNameFound noCommandNameFound) {
            noCommandNameFound(args, helper, event);
        } catch (OptionRequiresValue optionRequiresValue) {
            helper.optionRequiresValue(event, optionRequiresValue.getFlag());
        }

        return null;
    }

    private void noCommandNameFound(List<String> args, AbstractDispatcher<E, T> helper, E event) {
        PrefixContainer prefixContainer = findCommandPrefixes(commandTable.keySet(), args);
        List<Command> prefixMatchedCommands = prefixContainer.getPrefixes();
        String foundPrefix = prefixContainer.getFoundPrefix();

        if (prefixMatchedCommands.size() == 0) {
            // no prefixes matched means no valid command was found
            helper.commandNotFound(event, args.get(0));
        } else if (prefixMatchedCommands.size() == 1) {
            // one prefix matched means we can directly help for this command
            Command matchedCommand = prefixMatchedCommands.get(0);
            helper.help(event, matchedCommand, commandToFlags.get(matchedCommand), commandTable.keySet(), 1);
        } else {
            helper.helpSubcommands(event, foundPrefix, prefixMatchedCommands);
        }
    }

    public Collection<Command> getAllCommands() {
        return this.commandTable.keySet();
    }

    private Optional<Command> findCommand(Set<Command> commands, String commandName) {
        return commands.stream().filter(command -> command.getCommandName().equals(commandName))
                .findFirst();
    }

    private PrefixContainer findCommandPrefixes(Set<Command> commands, List<String> args) {

        List<Command> prefixes = new ArrayList<>();

        for (int i = args.size(); i > 0; i--) {
            List<String> trimmed = args.subList(0, i);
            String possibleCommandName = String.join(".", trimmed);

            prefixes = commands.stream()
                    .filter(command -> command.getCommandName().startsWith(possibleCommandName))
                    .collect(Collectors.toList());

            if (prefixes.size() > 0) {
                return new PrefixContainer(possibleCommandName, prefixes);
            }
        }

        return new PrefixContainer("", prefixes);
    }

    class CommandContainer {
        private Method method;
        private Set<CommandFlag> flags;

        CommandContainer(Method method, Set<CommandFlag> flags) {
            this.method = method;
            this.flags = flags;
        }

        public Method getMethod() {
            return method;
        }

        public Set<CommandFlag> getFlags() {
            return flags;
        }
    }

    class PrefixContainer {
        private String foundPrefix;
        private List<Command> prefixes;

        PrefixContainer(String foundPrefix, List<Command> prefixes) {
            this.foundPrefix = foundPrefix;
            this.prefixes = prefixes;
        }

        public String getFoundPrefix() {
            return this.foundPrefix;
        }

        public List<Command> getPrefixes() {
            return this.prefixes;
        }
    }
}
