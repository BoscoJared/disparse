package disparse.discord;

import disparse.discord.manager.*;
import disparse.discord.manager.provided.*;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractDispatcher<E, T> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDispatcher.class);

    protected PrefixManager<E, T> prefixManager;
    protected DescriptionManager<E, T> descriptionManager;
    protected PageLimitManager<E, T> pageLimitManager;
    protected CooldownManager cooldownManager;
    protected DisabledCommandManager disabledCommandManager;
    protected BaseEmbedManager<E, T> baseEmbedManager;
    protected ExecutorService executorService;
    protected Reflections reflections;
    protected CommandRegistrar<E, T> registrar;
    protected boolean respondToBots;

    protected List<BiFunction<E, String, Boolean>> registeredMiddleware = new ArrayList<>();

    protected AbstractDispatcher(String prefix, int pageLimit, String description) {
        this.prefixManager = new InMemoryPrefixManager<>(prefix);
        this.pageLimitManager = new InMemoryPageLimitManager<>(pageLimit);
        this.descriptionManager = new SingleDescriptionManager<>(description);
        this.cooldownManager = new InMemoryCooldownManager();
        this.disabledCommandManager = new InMemoryDisabledCommandManager();
        this.baseEmbedManager = new SingleBaseEmbedManager<>(this::createBuilder);
        this.executorService = Executors.newSingleThreadExecutor();
        this.reflections = this.defaultReflection(this.getClass());
        this.registrar = null;
        this.respondToBots = false;
    }

    public void dispatch(E event) {
        if (!respondToBots && this.isAuthorABot(event)) return;

        String raw = this.rawMessageContentFromEvent(event);
        String currentPrefix = this.prefixManager.prefixForGuild(event, this);

        if (!raw.startsWith(currentPrefix)) {
            return;
        }

        String cleanedMessage = raw.substring(currentPrefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        this.executorService.submit(() -> this.registrar.dispatch(args, this, event));

    }

    public void help(E event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {
        if (this.commandRolesNotMet(event, command) || this.commandIntentsNotMet(event, command)) return;

        if (!this.disabledCommandManager.commandAllowedInGuild(this.guildFromEvent(event), command)) return;

        T builder = this.baseEmbedManager.baseHelpEmbedForGuild(event, this);
        setBuilderTitle(builder, Help.getTitle(command));
        setBuilderDescription(builder, Help.getDescriptionUsage(command));

        if (command.getAliases().length > 0) {
            String[] aliases = command.getAliases();
            Arrays.sort(aliases, Comparator.comparingInt(String::length));
            String aliasString = String.join(", ", aliases);
            addField(builder, "Aliases", aliasString, false);
        }

        if (!command.getCooldownDuration().isZero()) {
            String type = "";
            switch(command.getScope()) {
                case USER:
                    type = "User";
                    break;
                case CHANNEL:
                    type = "Channel";
                    break;
                case GUILD:
                    type = "Guild";
                    break;
            }
            addField(builder, type + " Cooldown Enabled", humanReadableFormat(command.getCooldownDuration()), false);
        }

        if (command.getUsageExamples().size() > 0 && pageNumber == 1) {
            String prefix = this.prefixManager.prefixForGuild(event, this);
            addField(builder, "USAGE", "---------------------", false);
            command.getUsageExamples().forEach(usage -> {
                String msg = usage.getUsage();
                msg = "`" + prefix + command.getCommandName() + " " + msg + "`";
                addField(builder, msg, usage.getDescription(), false);
            });
        }

        List<Command> subcommands = Help.findSubcommands(command, commands).stream()
                .filter(c -> !this.commandRolesNotMet(event, c) && !this.commandIntentsNotMet(event, c))
                .collect(Collectors.toList());

        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(subcommands, Help.sortFlags(flags), pageNumber, getPageLimit(event));
            subcommands = paginatedEntities.getCommands();
            flags = paginatedEntities.getFlags();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<CommandFlag> sortedFlags = new ArrayList<>(flags);

        if (subcommands.size() > 0) {
            addField(builder, "SUBCOMMANDS", "---------------------", false);
        }

        addCommandsToEmbed(builder, subcommands);

        if (sortedFlags.size() > 0) {
            addField(builder, "FLAGS", "--------", true);
        }

        for (CommandFlag flag : sortedFlags) {
            String flagName = Help.flagToUserFriendlyString(flag);
            addField(builder, flagName, flag.getDescription(), false);
        }

        addField(builder, currentlyViewing, "Use `-p | --page` to specify a page number", false);
        sendEmbed(event, builder);
    }

    public void allCommands(E event, Collection<Command> commands, int pageNumber) {
        String guildId = guildFromEvent(event);

        Collection<Command> filteredCommands = commands.stream()
                .filter(c -> this.disabledCommandManager.commandAllowedInGuild(guildId, c))
                .filter(c -> !this.commandRolesNotMet(event, c) && !this.commandIntentsNotMet(event, c))
                .collect(Collectors.toList());

        T builder = this.baseEmbedManager.baseHelpEmbedForGuild(event, this);
        String title = this.getDescription(event);
        if (title == null || title.equals("")) {
            title = "All Commands";
        }
        setBuilderTitle(builder, title);
        setBuilderDescription(builder, "All registered commands");

        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(Help.sortCommands(filteredCommands), List.of(), pageNumber, getPageLimit(event));
            commands = paginatedEntities.getCommands();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<Command> sortedCommands = new ArrayList<>(commands);

        addCommandsToEmbed(builder, sortedCommands);
        addField(builder, currentlyViewing, "Use `-p | --page` to specify a page number", false);

        sendEmbed(event, builder);
    }

    public String getPrefix(E event) {
        return this.prefixManager.prefixForGuild(event, this);
    }

    public void setPrefix(E event, String prefix) {
        this.prefixManager.setPrefixForGuild(event, this, prefix);
    }

    public int getPageLimit(E event) {
        return this.pageLimitManager.pageLimitForGuild(event, this);
    }

    public void setPageLimit(E event, int pageLimit) {
        this.pageLimitManager.setPageLimitForGuild(event, this, pageLimit);
    }

    public String getDescription(E event) {
        return this.descriptionManager.descriptionForGuild(event, this);
    }

    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    public void helpSubcommands(E event, String foundPrefix, Collection<Command> commands) {
        T builder = this.baseEmbedManager.baseHelpEmbedForGuild(event, this);
        setBuilderTitle(builder, foundPrefix + " | Subcommands");
        setBuilderDescription(builder, "All registered subcommands for " + foundPrefix);

        String guildId = guildFromEvent(event);

        List<Command> sortedCommands = commands.stream()
                .filter((Command cmd) -> !this.commandRolesNotMet(event, cmd) && !this.commandIntentsNotMet(event, cmd))
                .filter((Command cmd) -> this.disabledCommandManager.commandAllowedInGuild(guildId, cmd))
                .sorted(Comparator.comparing((Command cmd) ->
                        cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
                .collect(Collectors.toList());

        addCommandsToEmbed(builder, sortedCommands);

        sendEmbed(event, builder);
    }

    public void addCommandsToEmbed(T builder, List<Command> commands) {
        for (Command command : commands) {
            addField(builder, "**" + command.getCommandName() + "**", command.getDescription(), false);
        }
    }

    public boolean runMiddleware(E event, String command) {
        if (this.registeredMiddleware.size() == 0) return true;

        return this.registeredMiddleware.stream().allMatch(f -> f.apply(event, command));
    }

    public void flagRequiresInt(E event, CommandFlag flag, String received) {
        String flagOutput = Help.flagToUserFriendlyString(flag);
        String msg = "`" + flagOutput + "` requires an integer value!  Received:  `" + received + "`";
        this.sendMessage(event, msg);
    }

    public void disableCommand(E event, String commandName) {
        Collection<Command> commands = this.registrar.getAllCommands();
        Optional<Command> foundCommand = commands.stream()
                .filter(c -> c.getCommandName().equals(commandName)).findFirst();
        String guildId = guildFromEvent(event);

        foundCommand.ifPresent(c -> this.disabledCommandManager.disableCommandForGuild(guildId, c));
    }

    public void enableCommand(E event, String commandName) {
        Collection<Command> commands = this.registrar.getAllCommands();
        Optional<Command> foundCommand = commands.stream()
                .filter(c -> c.getCommandName().equals(commandName)).findFirst();
        String guildId = guildFromEvent(event);

        foundCommand.ifPresent(c -> this.disabledCommandManager.enableCommandForGuild(guildId, c));
    }

    public boolean isEnabledForGuild(E event, Command command) {
        return this.disabledCommandManager.commandAllowedInGuild(guildFromEvent(event), command);
    }

    public abstract boolean commandRolesNotMet(E event, Command command);

    public abstract boolean commandIntentsNotMet(E event, Command command);

    public abstract void sendMessage(E event, String message);

    public abstract void setBuilderTitle(T builder, String title);

    public abstract void setBuilderDescription(T builder, String description);

    public abstract void addField(T builder, String name, String value, boolean inline);

    public abstract T createBuilder();

    public abstract void sendEmbed(E event, T builder);

    public abstract String identityFromEvent(E event);

    public abstract String channelFromEvent(E event);

    public abstract String guildFromEvent(E event);

    public abstract String rawMessageContentFromEvent(E event);

    public abstract boolean isSentFromChannel(E event);

    public abstract boolean isSentFromDM(E event);

    public abstract boolean isAuthorABot(E event);

    public abstract AbstractDiscordRequest<E, T> createRequest(E event, List<String> args);

    public void sendMessages(E event, Collection<String> messages) {
        for (String message : messages) {
            sendMessage(event, message);
        }
    }

    public void commandNotFound(E event, String userInput) {
        sendMessages(event, Help.commandNotFound(userInput, this.prefixManager.prefixForGuild(event, this)));
    }

    public void roleNotMet(E event, Command command) {
        sendMessage(event, Help.roleNotMet(command));
    }

    public void optionRequired(E event, Command command, CommandFlag flag) {
        sendMessage(event, Help.optionRequired(command, flag));
    }

    public void optionRequiresValue(E event, CommandFlag flag) {
        sendMessage(event, Help.optionRequiresValue(flag));
    }

    public void incorrectOption(E event, String userChoice, String flagName, String options) {
        sendMessages(event, Help.incorrectOption(userChoice, flagName, options));
    }

    public Reflections defaultReflection(Class<?> clazz) {
        return new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(clazz.getPackage().getName()))
                        .setScanners(new MethodAnnotationsScanner())
        );
    }

    protected static abstract class BaseBuilder<E, T, A extends AbstractDispatcher<E, T>, B extends BaseBuilder> {
        protected A actualClass;
        protected B actualClassBuilder;

        protected abstract A getActual();
        protected abstract B getActualBuilder();
        protected BaseBuilder(Class<?> clazz) {
            actualClass = getActual();
            actualClassBuilder = getActualBuilder();
            actualClass.reflections = actualClass.defaultReflection(clazz);
        }

        public B prefix(String prefix) {
            actualClass.prefixManager = new InMemoryPrefixManager<>(prefix);
            return actualClassBuilder;
        }

        public B prefixManager(PrefixManager<E, T> prefixManager) {
            actualClass.prefixManager = prefixManager;
            return actualClassBuilder;
        }

        public B description(String description) {
            actualClass.descriptionManager = new SingleDescriptionManager<>(description);
            return actualClassBuilder;
        }

        public B descriptionManager(DescriptionManager<E, T> descriptionManager) {
            actualClass.descriptionManager = descriptionManager;
            return actualClassBuilder;
        }

        public B pageLimit(int pageLimit) {
            actualClass.pageLimitManager = new InMemoryPageLimitManager<>(pageLimit);
            return actualClassBuilder;
        }

        public B withExecutorService(ExecutorService executorService) {
            actualClass.executorService = executorService;
            return actualClassBuilder;
        }

        public B withPageLimitManager(PageLimitManager<E, T> pageLimitManager) {
            actualClass.pageLimitManager = pageLimitManager;
            return actualClassBuilder;
        }

        public B withCooldownStrategy(CooldownManager cooldownManager) {
            actualClass.cooldownManager = cooldownManager;
            return actualClassBuilder;
        }

        public B withDisabledCommandManager(DisabledCommandManager disabledCommandManager) {
            actualClass.disabledCommandManager = disabledCommandManager;
            return actualClassBuilder;
        }

        public B withMiddleware(BiFunction<E, String, Boolean> middleware) {
            actualClass.registeredMiddleware.add(middleware);
            return actualClassBuilder;
        }

        public B withHelpBaseEmbed(Supplier<T> builderSupplier) {
            actualClass.baseEmbedManager = new SingleBaseEmbedManager<>(builderSupplier);
            return actualClassBuilder;
        }

        public B withHelpBaseEmbedManager(BaseEmbedManager<E, T> baseEmbedManager) {
            actualClass.baseEmbedManager = baseEmbedManager;
            return actualClassBuilder;
        }

        public B allowIncomingBotMessages() {
            actualClass.respondToBots = true;
            return actualClassBuilder;
        }

        public B disallowIncomingBotMessages() {
            actualClass.respondToBots = false;
            return actualClassBuilder;
        }

        public B withReflections(Reflections reflections) {
            actualClass.reflections = reflections;
            return actualClassBuilder;
        }

        public A build() {
            actualClass.registrar = Detector.detect(actualClass.reflections);
            return actualClass;
        }
    }

    private static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
