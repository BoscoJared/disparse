package disparse.discord;

import disparse.parser.Command;
import disparse.utils.Shlex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDispatcher extends AbstractDispatcher<Object, StringBuilder> {

  List<String> messages = new ArrayList<>();

  protected TestDispatcher(String prefix, int pageLimit, String description) {
    super(prefix, pageLimit, description);
  }

  @Override
  public boolean commandRolesNotMet(Object event, Command command) {
    return false;
  }

  @Override
  public boolean commandIntentsNotMet(Object event, Command command) {
    return false;
  }

  @Override
  public void sendMessage(Object event, String message) {
    this.messages.add(message);
  }

  @Override
  public void setBuilderTitle(StringBuilder builder, String title) {
    builder.append("title|").append(title).append("\n");
  }

  @Override
  public void setBuilderDescription(StringBuilder builder, String description) {
    builder.append("description|").append(description).append("\n");
  }

  @Override
  public void addField(StringBuilder builder, String name, String value, boolean inline) {
    builder.append(String.join("|", name, value, String.valueOf(inline))).append("\n");
  }

  @Override
  public StringBuilder createBuilder() {
    return new StringBuilder();
  }

  @Override
  public void sendEmbed(Object event, StringBuilder builder) {
    Collections.addAll(this.messages, builder.toString().split("\\n"));
  }

  @Override
  public String identityFromEvent(Object event) {
    return "USER";
  }

  @Override
  public String channelFromEvent(Object event) {
    return "CHANNEL";
  }

  @Override
  public String guildFromEvent(Object event) {
    return "GUILD";
  }

  @Override
  public String rawMessageContentFromEvent(Object event) {
    return null;
  }

  @Override
  public boolean isSentFromChannel(Object event) {
    return false;
  }

  @Override
  public boolean isSentFromDM(Object event) {
    return false;
  }

  @Override
  public boolean isAuthorABot(Object event) {
    return false;
  }

  public void dispatch(String raw) {
    String currentPrefix = this.prefixManager.prefixForGuild(null, this);
    if (!raw.startsWith(currentPrefix)) {
      return;
    }

    String cleanedMessage = raw.substring(currentPrefix.length());

    if (cleanedMessage.isEmpty()) {
      return;
    }

    List<String> args = Shlex.shlex(cleanedMessage);
    this.registrar.dispatch(args, this, new Object());
  }

  @Override
  public AbstractDiscordRequest<Object, StringBuilder> createRequest(
      Object event, List<String> args) {
    return new TestDiscordRequest(this, event, args);
  }

  @Override
  public void sendReact(Object event, String value) {
    this.messages.add(value);
  }

  public List<String> getMessages() {
    return this.messages;
  }

  public static class Builder extends BaseBuilder<Object, StringBuilder, TestDispatcher, Builder> {

    public Builder(Class<?> clazz) {
      super(clazz);
    }

    @Override
    protected TestDispatcher getActual() {
      return new TestDispatcher("!", 6, "");
    }

    @Override
    protected Builder getActualBuilder() {
      return this;
    }
  }
}
