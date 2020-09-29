package disparse.discord.d4j;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Permission;
import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import disparse.discord.PermissionEnumConverter;
import disparse.parser.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher extends AbstractDispatcher<MessageCreateEvent, EmbedCreateSpec> {

  private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private final PermissionEnumConverter<Permission> enumConverter = new PermissionMapping();

  private Dispatcher() {
    this("", 5, "");
  }

  private Dispatcher(String prefix, int pageLimit, String description) {
    super(prefix, pageLimit, description);
  }

  public static void init(GatewayDiscordClient gateway, Dispatcher dispatcher) {
    gateway.on(MessageCreateEvent.class).subscribe(dispatcher::onMessageReceived);
  }

  public void onMessageReceived(MessageCreateEvent event) {
    if (event.getMessage().getAuthor().isEmpty()) return;
    this.dispatch(event);
  }

  @Override
  public void sendMessage(MessageCreateEvent event, String message) {
    event.getMessage().getChannel().block().createMessage(message).block();
  }

  @Override
  public void setBuilderTitle(EmbedCreateSpec builder, String title) {
    builder.setTitle(title);
  }

  @Override
  public void setBuilderDescription(EmbedCreateSpec builder, String description) {
    builder.setDescription(description);
  }

  @Override
  public boolean commandRolesNotMet(MessageCreateEvent event, Command command) {
    if (command.getRoles().length == 0) {
      return false;
    }
    Member member = event.getMember().orElse(null);
    if (member != null) {
      for (String commandRole : command.getRoles()) {
        if (commandRole.equalsIgnoreCase("owner")
            && event.getGuild().block().getOwnerId().equals(member.getId())) {
          return false;
        }
        for (Role role : member.getRoles().toIterable()) {
          if (role.getName().equalsIgnoreCase(commandRole)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean commandIntentsNotMet(MessageCreateEvent event, Command command) {
    if (command.getPerms().length == 0) {
      return false;
    }
    return Arrays.stream(command.getPerms())
        .map(enumConverter::into)
        .noneMatch(
            p -> {
              Optional<Member> optionalMember = event.getMember();

              if (optionalMember.isEmpty()) return false;

              return optionalMember.get().getBasePermissions().block().contains(p);
            });
  }

  @Override
  public boolean isSentFromChannel(MessageCreateEvent event) {
    return event.getMessage().getChannel().block() instanceof TextChannel;
  }

  @Override
  public boolean isSentFromDM(MessageCreateEvent event) {
    return event.getMessage().getChannel().block() instanceof PrivateChannel;
  }

  @Override
  public boolean isAuthorABot(MessageCreateEvent event) {
    return event.getMessage().getAuthor().get().isBot();
  }

  @Override
  public AbstractDiscordRequest<MessageCreateEvent, EmbedCreateSpec> createRequest(
      MessageCreateEvent event, List<String> args) {
    return new DiscordRequest(this, event, args);
  }

  @Override
  public EmbedCreateSpec createBuilder() {
    return new EmbedCreateSpec();
  }

  @Override
  public void addField(EmbedCreateSpec builder, String name, String value, boolean inline) {
    builder.addField(name, value, inline);
  }

  @Override
  public void sendEmbed(MessageCreateEvent event, EmbedCreateSpec builder) {
    event
        .getMessage()
        .getChannel()
        .block()
        .createMessage(
            messageSpec -> {
              messageSpec.setEmbed(
                  embedSpec -> {
                    embedSpec.setTitle(builder.asRequest().title().toOptional().orElse(""));
                    embedSpec.setDescription(
                        builder.asRequest().description().toOptional().orElse(""));

                    builder
                        .asRequest()
                        .fields()
                        .toOptional()
                        .orElse(new ArrayList<>())
                        .forEach(
                            f -> {
                              embedSpec.addField(
                                  f.name(), f.value(), f.inline().toOptional().orElse(false));
                            });
                  });
            })
        .block();
  }

  @Override
  public String identityFromEvent(MessageCreateEvent event) {
    Optional<User> optionalUser = event.getMessage().getAuthor();
    if (optionalUser.isEmpty()) return null;

    return optionalUser.get().getId().toString();
  }

  @Override
  public String channelFromEvent(MessageCreateEvent event) {
    return event.getMessage().getChannelId().toString();
  }

  @Override
  public String guildFromEvent(MessageCreateEvent event) {
    return event.getGuildId().map(Snowflake::asString).orElse(null);
  }

  @Override
  public String rawMessageContentFromEvent(MessageCreateEvent event) {
    return event.getMessage().getContent();
  }

  public static class Builder
      extends BaseBuilder<MessageCreateEvent, EmbedCreateSpec, Dispatcher, Builder> {
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

  @Override
  public void reactToMessage(MessageCreateEvent event) {
    event.getMessage().addReaction(ReactionEmoji.unicode("\u1F552")).block();
  }

}
