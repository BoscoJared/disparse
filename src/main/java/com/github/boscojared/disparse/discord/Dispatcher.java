package com.github.boscojared.disparse.discord;

import com.github.boscojared.disparse.parser.Command;
import com.github.boscojared.disparse.parser.CommandFlag;
import com.github.boscojared.disparse.parser.Types;
import com.github.boscojared.disparse.parser.dispatch.CommandRegistrar;
import com.github.boscojared.disparse.parser.reflection.Detector;
import com.github.boscojared.disparse.utils.Shlex;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.management.ConstructorParameters;
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
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
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

    public void optionRequired(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessage(message).queue();
    }

    private static final Comparator<CommandFlag> helpCommandFlagComparator = new Comparator<>() {
        private Comparator<CommandFlag> comparator =
            Comparator.comparing(this::shortNameKeyExtractor, Comparator.nullsLast(Comparator.naturalOrder()))
                      .thenComparing(this::longNameKeyExtractor);

        private Character shortNameKeyExtractor(CommandFlag flag) {
            Character ch = flag.getShortName();
            return ch == null ? null : Character.toLowerCase(ch);
        }

        private String longNameKeyExtractor(CommandFlag flag) {
            return flag.getLongName().toLowerCase();
        }

        @Override
        public int compare(CommandFlag o1, CommandFlag o2) {
            return comparator.compare(o1, o2);
        }
    };

    public void help(MessageReceivedEvent event, Command command, Collection<CommandFlag> flags, Collection<Command> commands) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("%s:  %s", command.getCommandName(), command.getDescription()))
                .setDescription(String.format("Usage of command:  %s.  [+] may be repeated.", command.getCommandName()));


        List<CommandFlag> sortedFlags = flags.stream().sorted(helpCommandFlagComparator).collect(Collectors.toList());

        List<Command> subCommands = getHelpSubCommands(command, commands);

        if (subCommands.size() > 0) {
            builder.addField("SUB-COMMANDS", "---------------------", false);
        }

        for (Command subCommand: subCommands) {
            builder.addField(subCommand.getCommandName(), subCommand.getDescription(), false);
        }

        if (sortedFlags.size() > 0) {
            builder.addField("FLAGS", "--------", false);
        }

        for (CommandFlag flag : sortedFlags) {
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

    private List<Command> getHelpSubCommands(final Command command, Collection<Command> commands) {
        Predicate<Command> predicate =
            c -> c.getCommandName()
                  .startsWith(command.getCommandName()) && c != command;

        Comparator<Command> comparator = Comparator.comparing(
                cmd -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder());

        return commands.stream()
                       .filter(predicate)
                       .sorted(comparator)
                       .collect(Collectors.toList());
    }

    public void allCommands(MessageReceivedEvent event, Collection<Command> commands) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("All Commands")
                .setDescription("All registered commands");

        List<Command> sortedCommands = commands.stream()
                .sorted(Comparator.comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
                .collect(Collectors.toList());

        for (Command command : sortedCommands) {
            builder.addField(command.getCommandName(), command.getDescription(), false);
        }

        event.getChannel().sendMessage(builder.build()).queue();
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
