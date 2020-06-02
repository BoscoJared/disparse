package disparse.discord.jda;

import disparse.discord.AbstractDispatcher;
import disparse.parser.Command;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

public class Dispatcher extends AbstractDispatcher<MessageReceivedEvent, EmbedBuilder> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    public Dispatcher() { this(""); }

    public Dispatcher(String prefix) {
        this(prefix, 5);
    }

    public Dispatcher(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public Dispatcher(String prefix, int pageLimit, String description) {
        super(prefix, pageLimit, description);
    }

    public static JDABuilder init(JDABuilder builder, String prefix) {
        return init(builder, prefix, 5);
    }

    public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit) {
        return init(builder, prefix, pageLimit, "");
    }

    public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit, String description) {
        Dispatcher dispatcher = new Dispatcher(prefix, pageLimit, description);
        return init(builder, dispatcher);
    }

    public static JDABuilder init(JDABuilder builder, Dispatcher dispatcher) {
        Detector.detect();
        builder.addEventListeners(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
                dispatcher.onMessageReceived(event);
            }
        });
        return builder;
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String raw = event.getMessage().getContentRaw();
        String currentPrefix = this.prefixManager.prefixForGuild(event, this);

        if (!raw.startsWith(currentPrefix)) {
            return;
        }

        String cleanedMessage = raw.substring(currentPrefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        this.executorService.submit(() -> CommandRegistrar.REGISTRAR.dispatch(args, this, event));
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
    public boolean isSentFromChannel(MessageReceivedEvent event) {
        return event.getChannelType() == ChannelType.TEXT;
    }

    @Override
    public boolean isSentFromDM(MessageReceivedEvent event) {
        return event.getChannelType() == ChannelType.PRIVATE || event.getChannelType() == ChannelType.GROUP;
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
        return false;
    }

    public static class Builder extends BaseBuilder<MessageReceivedEvent, EmbedBuilder, Dispatcher, Builder> {
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
