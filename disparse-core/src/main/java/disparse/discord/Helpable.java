package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface Helpable<E, T> {

    default void help(E event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {
        if (this.commandRolesNotMet(event, command)) return;

        T builder = createBuilder();
        setBuilderTitle(builder, Help.getTitle(command));
        setBuilderDescription(builder, Help.getDescriptionUsage(command));

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

    default void allCommands(E event, Collection<Command> commands, int pageNumber) {
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

    String getPrefix();

    void setPrefix(String prefix);

    int getPageLimit();

    String getDescription();

    default void helpSubcommands(E event, String foundPrefix, Collection<Command> commands) {
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

    boolean commandRolesNotMet(E event, Command command);

    void sendMessage(E event, String message);

    void setBuilderTitle(T builder, String title);

    void setBuilderDescription(T builder, String description);

    void addCommandsToEmbed(T builder, List<Command> commands, E event);

    void addField(T builder, String name, String value, boolean inline);

    T createBuilder();

    void sendEmbed(E event, T builder);

    default void sendMessages(E event, Collection<String> messages) {
        for (String message : messages) {
            sendMessage(event, message);
        }
    }

    default void commandNotFound(E event, String userInput) {
        sendMessages(event, Help.commandNotFound(userInput, getPrefix()));
    }

    default void roleNotMet(E event, Command command) {
        sendMessage(event, Help.roleNotMet(command));
    }

    default void optionRequired(E event, Command command, CommandFlag flag) {
        sendMessage(event, Help.optionRequired(command, flag));
    }

    default void optionRequiresValue(E event, CommandFlag flag) {
        sendMessage(event, Help.optionRequiresValue(flag));
    }

    default void incorrectOption(E event, String userChoice, String flagName, String options) {
        sendMessages(event, Help.incorrectOption(userChoice, flagName, options));
    }

}
