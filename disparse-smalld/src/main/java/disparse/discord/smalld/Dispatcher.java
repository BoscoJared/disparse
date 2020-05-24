package disparse.discord.smalld;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import disparse.discord.Helpable;
import disparse.discord.smalld.guilds.Guilds;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static disparse.discord.smalld.Utils.*;

public class Dispatcher implements Helpable<Event> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private String prefix;
    private SmallD smalld;
    private int pageLimit;
    private Gson gson = new Gson();

    public Dispatcher(String prefix, SmallD smalld) {
        this(prefix, smalld, 5);
    }

    public Dispatcher(String prefix, SmallD smalld, int pageLimit) {
        this.prefix = prefix;
        this.smalld = smalld;
        this.pageLimit = pageLimit;
    }

    public static void init(SmallD smalld, String prefix) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix, smalld);
        smalld.onGatewayPayload(dispatcher::onMessageReceived);
    }

    public void onMessageReceived(String message) {
        JsonObject json = JsonParser.parseString(message).getAsJsonObject();

        if (!isMessageCreate(json) || isAuthorBot(json)) return;

        String raw = getMessageContent(json);
        if (!raw.startsWith(this.prefix)) return;

        String cleanedMessage = raw.substring(this.prefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        Event event = new Event(this.smalld, json);
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
    }

    @Override
    public void help(Event event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {
        if (this.commandRolesNotMet(event, command)) return;

        JsonObject embed = new JsonObject();
        embed.addProperty("title", Help.getTitle(command));
        embed.addProperty("type", "rich");
        embed.addProperty("description", Help.getDescriptionUsage(command));

        List<Command> subcommands = Help.findSubcommands(command, commands);
        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(subcommands, flags, pageNumber, pageLimit);
            subcommands = paginatedEntities.getCommands();
            flags = paginatedEntities.getFlags();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<CommandFlag> sortedFlags = Help.sortFlags(flags);

        JsonArray fields = new JsonArray();
        if (subcommands.size() > 0) {
            JsonObject field = new JsonObject();
            field.addProperty("name", "SUBCOMMANDS");
            field.addProperty("value", "---------------------");
            field.addProperty("inline", false);
            fields.add(field);
        }

        addCommandsToEmbed(fields, subcommands, event);

        if (sortedFlags.size() > 0) {
            JsonObject field = new JsonObject();
            field.addProperty("name", "FLAGS");
            field.addProperty("value", "--------");
            field.addProperty("inline", true);
            fields.add(field);
        }

        for (CommandFlag flag : sortedFlags) {
            String flagName = Help.flagToUserFriendlyString(flag);
            JsonObject field = new JsonObject();
            field.addProperty("name", flagName);
            field.addProperty("value", flag.getDescription());
            field.addProperty("inline", false);
            fields.add(field);
        }

        JsonObject field = new JsonObject();
        field.addProperty("name", currentlyViewing);
        field.addProperty("value", "Use --page to specify a page number");
        field.addProperty("inline", false);
        fields.add(field);
        embed.add("fields", fields);
        sendEmbed(event, embed);
    }

    @Override
    public void allCommands(Event event, Collection<Command> commands, int pageNumber) {
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "All Commands");
        embed.addProperty("description", "All registered commands");
        embed.addProperty("type", "rich");

        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(commands, List.of(), pageNumber, pageLimit);
            commands = paginatedEntities.getCommands();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            sendMessage(event, pageNumberOutOfBounds.getMessage());
            return;
        }

        List<Command> sortedCommands = Help.sortCommands(commands);
        JsonArray fields = new JsonArray();
        addCommandsToEmbed(fields, sortedCommands, event);
        JsonObject field = new JsonObject();
        field.addProperty("name", currentlyViewing);
        field.addProperty("value", "Use --page to specify a page number");
        field.addProperty("inline", false);
        fields.add(field);
        embed.add("fields", fields);

        sendEmbed(event, embed);
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void commandNotFound(Event event, String userInput) {
        Help.commandNotFound(userInput, this.prefix).forEach(line -> sendMessage(event, line));
    }

    @Override
    public void helpSubcommands(Event event, String foundPrefix, Collection<Command> commands) {
        JsonObject embed = new JsonObject();
        embed.addProperty("title", foundPrefix + " | Subcommands");
        embed.addProperty("description", "All registered subcommands for " + foundPrefix);
        embed.addProperty("type", "rich");

        List<Command> sortedCommands = commands.stream().sorted(Comparator
                .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
                .filter((Command cmd) -> !this.commandRolesNotMet(event, cmd))
                .collect(Collectors.toList());

        JsonArray fields = new JsonArray();
        addCommandsToEmbed(fields, sortedCommands, event);
        embed.add("fields", fields);

        sendEmbed(event, embed);
    }

    @Override
    public void roleNotMet(Event event, Command command) {
        sendMessage(event, Help.roleNotMet(command));
    }

    @Override
    public void optionRequired(Event event, String message) {
        sendMessage(event, message);
    }

    @Override
    public void incorrectOption(Event event, String message) {
        sendMessage(event, message);
    }

    @Override
    public boolean commandRolesNotMet(Event event, Command command) {
        if (command.getRoles().length == 0) {
            return false;
        }

        String[] commandRoles = command.getRoles();

        return Guilds.getRolesForGuildMember(event, getAuthorId(event)).stream()
                .noneMatch(role -> {
                    for (String commandRole : commandRoles) {
                        if (role.getName().equalsIgnoreCase(commandRole)) {
                            return true;
                        }
                    }

                    return false;
                });
    }

    private void addCommandsToEmbed(JsonArray fields, List<Command> commands, Event event) {
        for (Command command : commands) {
            if (this.commandRolesNotMet(event, command)) {
                continue;
            }
            JsonObject field = new JsonObject();
            field.addProperty("name", command.getCommandName());
            field.addProperty("value", command.getDescription());
            field.addProperty("inline", false);
            fields.add(field);
        }
    }

}
