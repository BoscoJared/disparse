package disparse.discord.jda;

import disparse.discord.Helpable;
import disparse.parser.Command;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Dispatcher extends ListenerAdapter implements Helpable<MessageReceivedEvent, EmbedBuilder> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private String prefix;
    private int pageLimit;
    private String description;

    public Dispatcher(String prefix) {
        this(prefix, 5);
    }

    public Dispatcher(String prefix, int pageLimit) {
        this(prefix, pageLimit, "");
    }

    public Dispatcher(String prefix, int pageLimit, String description) {
        this.prefix = prefix;
        this.pageLimit = pageLimit;
        this.description = description;
    }

    public static JDABuilder init(JDABuilder builder, String prefix) {
        return init(builder, prefix, 5);
    }

    public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit) {
        return init(builder, prefix, pageLimit, "");
    }

    public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit, String description) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix, pageLimit, description);
        builder.addEventListeners(dispatcher);
        return builder;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith(this.prefix)) {
            return;
        }

        String cleanedMessage = raw.substring(this.prefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
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
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getPageLimit() {
        return this.pageLimit;
    }

    public void setPageLimit(int pageLimit) {
        this.pageLimit = pageLimit;
    }

    @Override
    public void addField(EmbedBuilder builder, String name, String value, boolean inline) {
        builder.addField(name, value, inline);
    }

    @Override
    public void addCommandsToEmbed(EmbedBuilder builder, List<Command> commands,
                                   MessageReceivedEvent event) {
        for (Command command : commands) {
            if (this.commandRolesNotMet(event, command)) {
                continue;
            }
            builder.addField(command.getCommandName(), command.getDescription(), false);
        }
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
}
