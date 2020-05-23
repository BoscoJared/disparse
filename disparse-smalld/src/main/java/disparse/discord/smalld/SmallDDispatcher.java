package disparse.discord.smalld;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.Gson;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static disparse.discord.smalld.SmallDUtils.*;

public class SmallDDispatcher implements Helpable<SmallDEvent> {

    private final static Logger logger = LoggerFactory.getLogger(SmallDDispatcher.class);

    private String prefix;
    private SmallD smalld;
    private int pageLimit;
    private Gson gson = new Gson();

    public SmallDDispatcher(String prefix, SmallD smalld) {
        this(prefix, smalld, 5);
    }

    public SmallDDispatcher(String prefix, SmallD smalld, int pageLimit) {
        this.prefix = prefix;
        this.smalld = smalld;
        this.pageLimit = pageLimit;
    }

    public static void init(SmallD smalld, String prefix) {
        Detector.detect();
        SmallDDispatcher dispatcher = new SmallDDispatcher(prefix, smalld);
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
        SmallDEvent event = new SmallDEvent(this.smalld, json);
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
    }

    @Override
    public void help(SmallDEvent event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {

    }

    @Override
    public void allCommands(SmallDEvent event, Collection<Command> commands, int pageNumber) {

    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void commandNotFound(SmallDEvent event, String userInput) {

    }

    @Override
    public void helpSubcommands(SmallDEvent event, String foundPrefix, Collection<Command> commands) {

    }

    @Override
    public void roleNotMet(SmallDEvent event, Command command) {
        sendMessage(event, Help.roleNotMet(command));
    }

    @Override
    public void optionRequired(SmallDEvent event, String message) {

    }

    @Override
    public boolean commandRolesNotMet(SmallDEvent event, Command command) {
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

}
