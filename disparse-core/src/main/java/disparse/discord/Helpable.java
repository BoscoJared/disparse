package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.dispatch.Cooldown;
import disparse.parser.dispatch.InMemoryCooldown;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Helpable<E, T> {

    protected String prefix;
    protected String description;
    protected int pageLimit;
    protected Cooldown cooldownManager;

    public Helpable(String prefix) {
        this(prefix, 5, "");
    }

    public Helpable(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public Helpable(String prefix, int pageLimit, String description) {
        this.prefix = prefix;
        this.pageLimit = pageLimit;
        this.description = description;
        this.cooldownManager = new InMemoryCooldown();
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
        String title = this.getDescription();
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

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getPageLimit() {
        return this.pageLimit;
    }

    public String getDescription() {
        return this.description;
    }

    public Cooldown getCooldownManager() {
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

    public abstract boolean isSentFromChannel(E event);

    public abstract boolean isSentFromDM(E event);

    public void sendMessages(E event, Collection<String> messages) {
        for (String message : messages) {
            sendMessage(event, message);
        }
    }

    public void commandNotFound(E event, String userInput) {
        sendMessages(event, Help.commandNotFound(userInput, getPrefix()));
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

    protected static abstract class BaseBuilder<A extends Helpable, B extends BaseBuilder> {
        protected A actualClass;
        protected B actualClassBuilder;

        protected abstract A getActual();
        protected abstract B getActualBuilder();
        protected BaseBuilder() {
            actualClass = getActual();
            actualClassBuilder = getActualBuilder();
        }

        public B prefix(String prefix) {
            actualClass.prefix = prefix;
            return actualClassBuilder;
        }

        public B description(String description) {
            actualClass.description = description;
            return actualClassBuilder;
        }

        public B pageLimit(int pageLimit) {
            actualClass.pageLimit = pageLimit;
            return actualClassBuilder;
        }

        public B withCooldownStrategy(Cooldown cooldown) {
            actualClass.cooldownManager = cooldown;
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
