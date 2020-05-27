package disparse.discord.d4j;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import disparse.discord.Helpable;
import disparse.parser.Command;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dispatcher extends Helpable<MessageCreateEvent, EmbedCreateSpec> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    public Dispatcher() {this("", 5, "");}

    public Dispatcher(String prefix) {
        this(prefix, 5, "");
    }

    public Dispatcher(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public Dispatcher(String prefix, int pageLimit, String description) {
        super(prefix, pageLimit, description);
    }

    public static void init(GatewayDiscordClient gateway, String prefix, int pageLimit, String description) {
        Dispatcher dispatcher = new Dispatcher(prefix, pageLimit, description);
        init(gateway, dispatcher);
    }

    public static void init(GatewayDiscordClient gateway, Dispatcher dispatcher) {
        Detector.detect();
        gateway.on(MessageCreateEvent.class).subscribe(dispatcher::onMessageReceived);
    }

    public void onMessageReceived(MessageCreateEvent event) {
        if (event.getMessage().getAuthor().isEmpty()) return;
        if (event.getMessage().getAuthor().get().isBot()) return;

        String raw = event.getMessage().getContent();
        if (!raw.startsWith(prefix)) return;

        String cleanedMessage = raw.substring(this.prefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
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
                if (commandRole.equalsIgnoreCase("owner") &&
                        event.getGuild().block().getOwnerId().equals(member.getId())) {
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
    public EmbedCreateSpec createBuilder() {
        return new EmbedCreateSpec();
    }

    @Override
    public void addField(EmbedCreateSpec builder, String name, String value, boolean inline) {
        builder.addField(name, value, inline);
    }

    @Override
    public void sendEmbed(MessageCreateEvent event, EmbedCreateSpec builder) {
        event.getMessage()
                .getChannel()
                .block()
                .createMessage(messageSpec -> {
                    messageSpec.setEmbed(embedSpec -> {
                        embedSpec.setTitle(builder.asRequest().title().toOptional().orElse(""));
                        embedSpec.setDescription(builder.asRequest().description().toOptional().orElse(""));

                        builder.asRequest().fields().toOptional().orElse(new ArrayList<>()).forEach(f -> {
                            embedSpec.addField(f.name(), f.value(), f.inline().toOptional().orElse(false));
                        });
                    });
                }).block();
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

    public static class Builder extends BaseBuilder<Dispatcher, Builder> {
        @Override
        protected Dispatcher getActual() {
            return new Dispatcher();
        }

        @Override
        protected Builder getActualBuilder() {
            return this;
        }

        public Dispatcher build() {
            return actualClass;
        }
    }
}
