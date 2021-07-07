package disparse.discord.smalld;

import static disparse.discord.smalld.Utils.*;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import disparse.discord.PermissionEnumConverter;
import disparse.discord.smalld.guilds.Guilds;
import disparse.discord.smalld.permissions.Permission;
import disparse.discord.smalld.permissions.PermissionBase;
import disparse.discord.smalld.permissions.PermissionUtils;
import disparse.parser.Command;
import disparse.utils.help.Help;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher extends AbstractDispatcher<Event, JsonElement> {

  private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private PermissionEnumConverter<Permission> enumConverter = new PermissionMapping();

  private SmallD smalld;

  private Dispatcher() {
    this("", null, 5, "");
  }

  private Dispatcher(String prefix, SmallD smalld, int pageLimit, String description) {
    super(prefix, pageLimit, description);
    this.smalld = smalld;
  }

  public static void init(Dispatcher dispatcher) {
    dispatcher.smalld.onGatewayPayload(dispatcher::onMessageReceived);
  }

  public void onMessageReceived(String message) {
    JsonObject json = JsonParser.parseString(message).getAsJsonObject();
    Event event = new Event(this.smalld, json);

    if (!isMessageCreate(json)) return;
    this.dispatch(event);
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
  public String guildFromEvent(Event event) {

    if (Utils.isTextChannel(event)) {
      return Guilds.getGuildId(event);
    }

    return null;
  }

  @Override
  public String rawMessageContentFromEvent(Event event) {
    return Utils.getMessageContent(event.getJson());
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
  public void setBuilderFooter(JsonElement builder, String footer) {
    JsonObject json = new JsonObject();
    json.addProperty("text", footer);

    builder.getAsJsonObject().add("footer", json);
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
        .noneMatch(
            role -> {
              for (String commandRole : commandRoles) {
                if (role.getName().equalsIgnoreCase(commandRole)) {
                  return true;
                }
              }

              return false;
            });
  }

  @Override
  public boolean commandIntentsNotMet(Event event, Command command) {
    if (command.getPerms().length == 0) {
      return false;
    }

    return Arrays.stream(command.getPerms())
        .map(enumConverter::into)
        .noneMatch(
            p -> {
              PermissionBase perm = PermissionUtils.computeAllPerms(event);
              return perm.contains(p);
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

  @Override
  public boolean isSentFromChannel(Event event) {
    return Utils.isTextChannel(event);
  }

  @Override
  public boolean isSentFromDM(Event event) {
    return Utils.isDm(event);
  }

  @Override
  public boolean isAuthorABot(Event event) {
    return isAuthorBot(event.getJson());
  }

  @Override
  public AbstractDiscordRequest<Event, JsonElement> createRequest(Event event, List<String> args) {
    return new DiscordRequest(this, event, args);
  }

  @Override
  public void sendReact(Event event, String value) {
    Utils.sendReact(event, value);
  }

  public static class Builder extends BaseBuilder<Event, JsonElement, Dispatcher, Builder> {
    public Builder(Class<?> clazz) {
      super(clazz);
    }

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

    @Override
    public Dispatcher build() {
      if (actualClass.smalld == null)
        throw new NullPointerException("SmallD instance cannot be null!");
      return super.build();
    }
  }
}
