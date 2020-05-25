package disparse.discord.d4j;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
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

public class Dispatcher extends Helpable<MessageCreateEvent, EmbedCreateSpec> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    public Dispatcher(String prefix) {
        this(prefix, 5, "");
    }

    public Dispatcher(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public Dispatcher(String prefix, int pageLimit, String description) {
        super(prefix, pageLimit, description);
    }

    public static void init(GatewayDiscordClient gateway, String prefix) {
        init(gateway, prefix, 5);
    }

    public static void init(GatewayDiscordClient gateway, String prefix, int pageLimit) {
        init(gateway, prefix, pageLimit, "");
    }

    public static void init(GatewayDiscordClient gateway, String prefix, int pageLimit, String description) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix, pageLimit, description);
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
    public void addCommandsToEmbed(EmbedCreateSpec builder, List<Command> commands, MessageCreateEvent event) {

        for (Command command : commands) {
            if (this.commandRolesNotMet(event, command)) {
                continue;
            }
            builder.addField(command.getCommandName(), command.getDescription(), false);
        }
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
}
