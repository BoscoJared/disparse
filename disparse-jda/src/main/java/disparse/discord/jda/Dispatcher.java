package disparse.discord.jda;

import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import disparse.discord.PermissionEnumConverter;
import disparse.parser.Command;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher extends AbstractDispatcher<MessageReceivedEvent, EmbedBuilder> {

  private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private final PermissionEnumConverter<Permission> enumConverter = new PermissionMapping();

  private Dispatcher() {
    this("", 5, "");
  }

  private Dispatcher(String prefix, int pageLimit, String description) {
    super(prefix, pageLimit, description);
  }

  public static JDABuilder init(JDABuilder builder, Dispatcher dispatcher) {
    builder.addEventListeners(
        new ListenerAdapter() {
          @Override
          public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
            dispatcher.dispatch(event);
          }
        });
    return builder;
  }

  @Override
  public void sendMessage(MessageReceivedEvent event, String message) {
    event.getChannel().sendMessage(message).queue();
  }

  @Override
  public void sendEmbed(MessageReceivedEvent event, EmbedBuilder builder) {
    event.getChannel().sendMessage(builder.build()).queue();
  }

  @Override
  public EmbedBuilder createBuilder() {
    return new EmbedBuilder();
  }

  @Override
  public void setBuilderTitle(EmbedBuilder builder, String title) {
    builder.setTitle(title);
  }

  @Override
  public void setBuilderDescription(EmbedBuilder builder, String description) {
    builder.setDescription(description);
  }

  @Override
  public void setBuilderFooter(EmbedBuilder builder, String footer) {
    builder.setFooter(footer);
  }

  @Override
  public void addField(EmbedBuilder builder, String name, String value, boolean inline) {
    builder.addField(name, value, inline);
  }

  @Override
  public String identityFromEvent(MessageReceivedEvent event) {
    return event.getAuthor().getId();
  }

  @Override
  public String channelFromEvent(MessageReceivedEvent event) {
    return event.getChannel().getId();
  }

  @Override
  public String guildFromEvent(MessageReceivedEvent event) {
    if (isSentFromChannel(event)) {
      return event.getGuild().getId();
    }

    return null;
  }

  @Override
  public String rawMessageContentFromEvent(MessageReceivedEvent event) {
    return event.getMessage().getContentRaw();
  }

  @Override
  public boolean isSentFromChannel(MessageReceivedEvent event) {
    return event.getChannelType() == ChannelType.TEXT;
  }

  @Override
  public boolean isSentFromDM(MessageReceivedEvent event) {
    return event.getChannelType() == ChannelType.PRIVATE
        || event.getChannelType() == ChannelType.GROUP;
  }

  @Override
  public boolean isAuthorABot(MessageReceivedEvent event) {
    return event.getAuthor().isBot();
  }

  @Override
  public AbstractDiscordRequest<MessageReceivedEvent, EmbedBuilder> createRequest(
      MessageReceivedEvent event, List<String> args) {
    return new DiscordRequest(this, event, args);
  }

  @Override
  public void sendReact(MessageReceivedEvent event, String value) {
    event.getMessage().addReaction(value).queue();
  }

  @Override
  public boolean commandRolesNotMet(MessageReceivedEvent event, Command command) {
    if (command.getRoles().length == 0) {
      return false;
    }
    Member member = event.getMember();
    if (member != null) {
      for (String commandRole : command.getRoles()) {
        if (commandRole.equalsIgnoreCase("owner") && event.getMember().isOwner()) {
          return false;
        }
        for (Role role : member.getRoles()) {
          if (role.getName().equalsIgnoreCase(commandRole)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean commandIntentsNotMet(MessageReceivedEvent event, Command command) {

    if (command.getPerms().length == 0) {
      return false;
    }

    return Arrays.stream(command.getPerms())
        .map(enumConverter::into)
        .noneMatch(p -> event.getMember().hasPermission(p));
  }

  public static class Builder
      extends BaseBuilder<MessageReceivedEvent, EmbedBuilder, Dispatcher, Builder> {
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
  }
}
