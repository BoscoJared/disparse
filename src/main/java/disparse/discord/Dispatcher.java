package disparse.discord;

import java.util.*;
import java.util.stream.Collectors;

import disparse.parser.Command;
import disparse.parser.Flag;
import disparse.parser.Types;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Dispatcher extends ListenerAdapter implements Helpable<MessageReceivedEvent> {

    private String prefix;

    public Dispatcher(String prefix) {
        this.prefix = prefix;
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
        String cleanedMessage = raw.replace(this.prefix, "");
        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.registrar.dispatch(args, this, event);
    }

    public void commandNotFound(MessageReceivedEvent event, String userInput) {
        event.getChannel().sendMessage("`" + userInput + "` is not a valid command!").queue();
        event.getChannel().sendMessage("Use !help to get a list of all available commands.").queue();
    }

    public void roleNotMet(MessageReceivedEvent event, Command command) {
        event.getChannel()
                .sendMessage("You do not have the correct permissions to run:  `" + command.getCommandName() + "`")
                .queue();
    }

    public void help(MessageReceivedEvent event, Command command, Collection<Flag> flags) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("%s:  %s", command.getCommandName(), command.getDescription()))
                .setDescription(String.format("Usage of command:  %s.  [+] may be repeated.", command.getCommandName()));

        List<Flag> sortedFlags = flags.stream()
                .sorted(
                        Comparator.comparing((Flag flag) -> toLower(flag.getShortName()), Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(flag -> flag.getLongName().toLowerCase())
                )
                .collect(Collectors.toList());

        for (Flag flag : sortedFlags) {
            String flagName;
            if (flag.getShortName() == null) {
                flagName = String.format("--%s", flag.getLongName());
            } else {
                flagName = String.format("-%s | --%s", flag.getShortName(), flag.getLongName());
            }

            if (flag.getType() == Types.LIST) {
                flagName = flagName + " [+]";
            }
            builder.addField(flagName, flag.getDescription(), false);
        }
        event.getChannel().sendMessage(builder.build()).queue();
    }

    public void allCommands(MessageReceivedEvent event, Collection<Command> commands) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("All Commands")
                .setDescription("All registered commands");

        for (Command command : commands) {
            builder.addField(command.getCommandName(), command.getDescription(), false);
        }

        event.getChannel().sendMessage(builder.build()).queue();
    }

    private static Character toLower(Character in) {
        if (in == null) return null;
        return Character.toLowerCase(in);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public static JDABuilder init(JDABuilder builder, String prefix) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix);
        builder.addEventListeners(dispatcher);
        return builder;
    }
}
