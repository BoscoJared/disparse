package disparse.discord;

import disparse.discord.manager.DescriptionManager;
import disparse.discord.manager.provided.SingleDescriptionManager;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.discord.manager.CooldownManager;
import disparse.discord.manager.provided.InMemoryCooldownManager;
import disparse.discord.manager.provided.InMemoryPrefixManager;
import disparse.discord.manager.PrefixManager;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDispatcher<E, T> {

    protected PrefixManager<E, T> prefixManager;
    protected DescriptionManager<E, T> descriptionManager;
    protected int pageLimit;
    protected CooldownManager cooldownManager;

    public AbstractDispatcher(String prefix) {
        this(prefix, 5, "");
    }

    public AbstractDispatcher(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public AbstractDispatcher(String prefix, int pageLimit, String description) {
        this.prefixManager = new InMemoryPrefixManager<>(prefix);
        this.pageLimit = pageLimit;
        this.descriptionManager = new SingleDescriptionManager<>(description);
        this.cooldownManager = new InMemoryCooldownManager();
    }

    public void help(E event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {
        if (this.commandRolesNotMet(event, command)) return;

        T builder = createBuilder();
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

        List<Command> subcommands = Help.findSubcommands(command, commands);
        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(subcommands, flags, pageNumber, getPageLimit());
            subcommands = paginatedEntities.getCommands();
            flags = paginatedEntities.getFlags();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<CommandFlag> sortedFlags = Help.sortFlags(flags);

        if (subcommands.size() > 0) {
            addField(builder, "SUBCOMMANDS", "---------------------", false);
        }

        addCommandsToEmbed(builder, subcommands, event);

        if (sortedFlags.size() > 0) {
            addField(builder, "FLAGS", "--------", true);
        }

        for (CommandFlag flag : sortedFlags) {
            String flagName = Help.flagToUserFriendlyString(flag);
            addField(builder, flagName, flag.getDescription(), false);
        }

        addField(builder, currentlyViewing, "Use --page to specify a page number", false);
        sendEmbed(event, builder);
    }

    public void allCommands(E event, Collection<Command> commands, int pageNumber) {
        T builder = createBuilder();
        String title = this.getDescription(event);
        if (title == null || title.equals("")) {
            title = "All Commands";
        }
        setBuilderTitle(builder, title);
        setBuilderDescription(builder, "All registered commands");

        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(commands, List.of(), pageNumber, getPageLimit());
            commands = paginatedEntities.getCommands();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<Command> sortedCommands = Help.sortCommands(commands);

        addCommandsToEmbed(builder, sortedCommands, event);
        addField(builder, currentlyViewing, "Use --page to specify a page number", false);

        sendEmbed(event, builder);
    }

    public String getPrefix(E event) {
        return this.prefixManager.prefixForGuild(event, this);
    }

    public void setPrefix(E event, String prefix) {
        this.prefixManager.setPrefixForGuild(event, this, prefix);
    }

    public int getPageLimit() {
        return this.pageLimit;
    }

    public String getDescription(E event) {
        return this.descriptionManager.descriptionForGuild(event, this);
    }

    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    public void helpSubcommands(E event, String foundPrefix, Collection<Command> commands) {
        T builder = createBuilder();
        setBuilderTitle(builder, foundPrefix + " | Subcommands");
        setBuilderDescription(builder, "All registered subcommands for " + foundPrefix);

        List<Command> sortedCommands = commands.stream().sorted(Comparator
                .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
                .filter((Command cmd) -> !this.commandRolesNotMet(event, cmd))
                .collect(Collectors.toList());

        addCommandsToEmbed(builder, sortedCommands, event);

        sendEmbed(event, builder);
    }

    public void addCommandsToEmbed(T builder, List<Command> commands, E event) {
        for (Command command : commands) {
            if (this.commandRolesNotMet(event, command)) {
                continue;
            }
            addField(builder, command.getCommandName(), command.getDescription(), false);
        }
    }

    public abstract boolean commandRolesNotMet(E event, Command command);

    public abstract void sendMessage(E event, String message);

    public abstract void setBuilderTitle(T builder, String title);

    public abstract void setBuilderDescription(T builder, String description);

    public abstract void addField(T builder, String name, String value, boolean inline);

    public abstract T createBuilder();

    public abstract void sendEmbed(E event, T builder);

    public abstract String identityFromEvent(E event);

    public abstract String channelFromEvent(E event);

    public abstract String guildFromEvent(E event);

    public abstract boolean isSentFromChannel(E event);

    public abstract boolean isSentFromDM(E event);

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

    protected static abstract class BaseBuilder<E, T, A extends AbstractDispatcher<E, T>, B extends BaseBuilder> {
        protected A actualClass;
        protected B actualClassBuilder;

        protected abstract A getActual();
        protected abstract B getActualBuilder();
        protected BaseBuilder() {
            actualClass = getActual();
            actualClassBuilder = getActualBuilder();
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
            actualClass.pageLimit = pageLimit;
            return actualClassBuilder;
        }

        public B withCooldownStrategy(CooldownManager cooldownManager) {
            actualClass.cooldownManager = cooldownManager;
            return actualClassBuilder;
        }
    }

    private static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
