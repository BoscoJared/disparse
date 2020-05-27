package disparse.discord.smalld;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import disparse.discord.Helpable;
import disparse.discord.smalld.guilds.Guilds;
import disparse.parser.Command;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import disparse.utils.help.Help;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static disparse.discord.smalld.Utils.*;

public class Dispatcher extends Helpable<Event, JsonElement> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private SmallD smalld;

    public Dispatcher() {this("", null); }

    public Dispatcher(String prefix, SmallD smalld) {
        this(prefix, smalld, 5);
    }

    public Dispatcher(String prefix, SmallD smalld, int pageLimit) {
        this(prefix, smalld, pageLimit, "");
    }

    public Dispatcher(String prefix, SmallD smalld, int pageLimit, String description) {
        super(prefix, pageLimit, description);
        this.smalld = smalld;
    }

    public static void init(SmallD smalld, String prefix) {
        init(smalld, prefix, 5, "");
    }

    public static void init(SmallD smalld, String prefix, int pageLimit) {
        init(smalld, prefix, pageLimit, "");
    }

    public static void init(SmallD smalld, String prefix, int pageLimit, String description) {
        Dispatcher dispatcher = new Dispatcher(prefix, smalld, pageLimit, description);
        init(dispatcher);
    }

    public static void init(Dispatcher dispatcher) {
        if (dispatcher.smalld == null) throw new NullPointerException("SmallD instance cannot be null!");
        Detector.detect();
        dispatcher.smalld.onGatewayPayload(dispatcher::onMessageReceived);
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
    public void sendMessage(Event event, String message) {
        Utils.sendMessage(event, message);
    }

    @Override
    public void sendEmbed(Event event, JsonElement element) {
        Utils.sendEmbed(event, element.getAsJsonObject());
    }

    @Override
    public String identityFromEvent(Event event) {
        return Utils.getAuthorId(event);
    }

    @Override
    public String channelFromEvent(Event event) {
        return Utils.getChannelId(event.getJson());
    }

    @Override
    public JsonElement createBuilder() {
        JsonObject builder = new JsonObject();
        builder.addProperty("type", "rich");
        builder.add("fields", new JsonArray());
        return builder;
    }

    @Override
    public void setBuilderTitle(JsonElement builder, String title) {
        builder.getAsJsonObject().addProperty("title", title);
    }

    @Override
    public void setBuilderDescription(JsonElement builder, String description) {
        builder.getAsJsonObject().addProperty("description", description);
    }

    @Override
    public void roleNotMet(Event event, Command command) {
        sendMessage(event, Help.roleNotMet(command));
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

    @Override
    public void addField(JsonElement element, String name, String value, boolean inline) {
        JsonArray fields = element.getAsJsonObject().getAsJsonArray("fields");
        JsonObject field = new JsonObject();
        field.addProperty("name", name);
        field.addProperty("value", value);
        field.addProperty("inline", inline);
        fields.add(field);
    }

    public static class Builder extends BaseBuilder<Dispatcher, Builder> {
        @Override
        protected Dispatcher getActual() {
            return new Dispatcher();
        }

        @Override
        protected Builder getActualBuilder() {
            return this;
        }

        public Builder withSmalldClient(SmallD smalld) {
            actualClass.smalld = smalld;
            return this;
        }

        public Dispatcher build() {
            return actualClass;
        }
    }
}
