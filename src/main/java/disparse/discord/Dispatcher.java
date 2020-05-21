package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.Types;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher extends ListenerAdapter implements Helpable<MessageReceivedEvent> {

  private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private static final Comparator<CommandFlag> helpCommandFlagComparator = new Comparator<>() {
    private Comparator<CommandFlag> comparator = Comparator
            .comparing(this::identity, (left, right) -> {
              String leftLong = left.getLongName();
              String rightLong = right.getLongName();
              Character leftShort = left.getShortName();
              Character rightShort = right.getShortName();

              boolean leftShortOnly = leftShort != null && leftLong == null;
              boolean leftLongOnly = leftShort == null && leftLong != null;
              boolean leftBoth = leftShort != null && leftLong != null;

              boolean rightShortOnly = rightShort != null && rightLong == null;
              boolean rightLongOnly = rightShort == null && rightLong != null;
              boolean rightBoth = rightShort != null && rightLong != null;

              if (leftShortOnly && rightShortOnly) {
                return leftShort.compareTo(rightShort);
              } else if (leftShortOnly && !rightShortOnly) {
                return -1;
              } else if (!leftShortOnly && rightShortOnly) {
                return 1;
              } else if (leftBoth && rightBoth) {
                return leftLong.compareTo(rightLong);
              } else if (leftBoth && !rightBoth) {
                return -1;
              } else if (!leftBoth && rightBoth) {
                return 1;
              } else if (leftLongOnly && rightLongOnly){
                return leftLong.compareTo(rightLong);
              } else {
                return 0;
              }
            });


    private Character shortNameKeyExtractor(CommandFlag flag) {
      Character ch = flag.getShortName();
      return ch == null ? null : Character.toLowerCase(ch);
    }

    private String longNameKeyExtractor(CommandFlag flag) {
      return flag.getLongName() == null ? null : flag.getLongName().toLowerCase();
    }

    private CommandFlag identity(CommandFlag flag) {
      return flag;
    }

    @Override
    public int compare(CommandFlag o1, CommandFlag o2) {
      return comparator.compare(o1, o2);
    }
  };
  private String prefix;
  private int pageLimit;

  public Dispatcher(String prefix) {
    this(prefix, 5);
  }

  public Dispatcher(String prefix, int pageLimit) {
    this.prefix = prefix;
    this.pageLimit = pageLimit;
  }

  public static JDABuilder init(JDABuilder builder, String prefix) {
    return init(builder, prefix, 5);
  }

  public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit) {
    Detector.detect();
    Dispatcher dispatcher = new Dispatcher(prefix, pageLimit);
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

  public void commandNotFound(MessageReceivedEvent event, String userInput) {
    event.getChannel().sendMessage("`" + userInput + "` is not a valid command!").queue();
    event.getChannel().sendMessage("Use !help to get a list of all available commands.").queue();
  }

  public void roleNotMet(MessageReceivedEvent event, Command command) {
    event.getChannel()
        .sendMessage(
            "You do not have the correct permissions to run:  `" + command.getCommandName() + "`")
        .queue();
  }

  public void optionRequired(MessageReceivedEvent event, String message) {
    event.getChannel().sendMessage(message).queue();
  }

  public void help(MessageReceivedEvent event, Command command, Collection<CommandFlag> flags,
      Collection<Command> commands, int pageNumber) {
    if (CommandRegistrar.REGISTRAR.commandRolesNotMet(event, command)) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle(String.format("%s:  %s", command.getCommandName(), command.getDescription()))
        .setDescription(String.format("Usage of command:  %s.  [+] may be repeated.",
            command.getCommandName()));

    List<Command> subcommands = getHelpSubCommands(command, commands);

    List<CommandFlag> sortedFlags =
            flags.stream().sorted(helpCommandFlagComparator).collect(Collectors.toList());

    int totalEntities = subcommands.size() + sortedFlags.size();
    int pages = (int) Math.ceil(((double) totalEntities) / this.pageLimit);
    int lowerBound = (pageNumber - 1) * this.pageLimit;
    int upperBound = (lowerBound + this.pageLimit);

    if (lowerBound >= subcommands.size()) {
      if ((lowerBound - subcommands.size()) > flags.size()) {
        String err = String.format("The specified page number **%d** is not within the range of valid pages.  The valid pages " +
                "are between **1** and **%d**.", pageNumber, pages);
        event.getMessage().getChannel().sendMessage(err).queue();
        return;
      } else {
        if ((upperBound - subcommands.size()) >= sortedFlags.size()) {
          upperBound = sortedFlags.size();
        } else {
          upperBound = upperBound - subcommands.size();
        }
        sortedFlags = sortedFlags.subList((lowerBound - subcommands.size()), upperBound);
        subcommands = List.of();
      }
    } else if (upperBound > subcommands.size()){
      subcommands = subcommands.subList(lowerBound, subcommands.size());
      int rest = this.pageLimit - subcommands.size();
      if (rest > sortedFlags.size()) {
        rest = sortedFlags.size();
      }
      sortedFlags = sortedFlags.subList(0, rest);
    } else {
      subcommands = subcommands.subList(lowerBound, upperBound);
      sortedFlags = List.of();
    }

    if (subcommands.size() > 0) {
      builder.addField("SUBCOMMANDS", "---------------------", false);
    }

    builder = addCommandsToEmbed(builder, subcommands, event);

    if (sortedFlags.size() > 0) {
      builder.addField("FLAGS", "--------", true);
    }

    for (CommandFlag flag : sortedFlags) {
      String flagName;
      if (flag.getShortName() == null) {
        flagName = String.format("--%s", flag.getLongName());
      } else if (flag.getLongName() == null) {
        flagName = String.format("-%s", flag.getShortName());
      } else {
        flagName = String.format("-%s | --%s", flag.getShortName(), flag.getLongName());
      }

      if (flag.getType() == Types.LIST) {
        flagName = flagName + " [+]";
      }
      builder.addField(flagName, flag.getDescription(), false);
    }

    String pageOutput = String.format("Currently viewing page %d of %d", pageNumber, pages);
    builder.addField(pageOutput, "Use --page to specify a page number", false);
    event.getChannel().sendMessage(builder.build()).queue();
  }

  private List<Command> getHelpSubCommands(final Command command, Collection<Command> commands) {
    Predicate<Command> predicate =
        c -> c.getCommandName().startsWith(command.getCommandName()) && !c.getCommandName().equals(command.getCommandName());

    Comparator<Command> comparator =
        Comparator.comparing(cmd -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder());

    return commands.stream().filter(predicate).sorted(comparator).collect(Collectors.toList());
  }

  public void helpSubcommands(MessageReceivedEvent event, String foundPrefix, Collection<Command> commands) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle(foundPrefix + " | Subcommands").setDescription("All registered subcommands for " + foundPrefix);

    List<Command> sortedCommands = commands.stream().sorted(Comparator
            .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
            .filter((Command cmd) -> !CommandRegistrar.commandRolesNotMet(event, cmd))
            .collect(Collectors.toList());

    builder = addCommandsToEmbed(builder, sortedCommands, event);

    event.getChannel().sendMessage(builder.build()).queue();
  }

  public void allCommands(MessageReceivedEvent event, Collection<Command> commands, int pageNumber) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle("All Commands").setDescription("All registered commands");

    List<Command> sortedCommands = commands.stream().sorted(Comparator
        .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
        .collect(Collectors.toList());

    int pages = (int) Math.ceil(((double) (sortedCommands.size())) / this.pageLimit);
    int lowerBound = (pageNumber - 1) * this.pageLimit;
    int upperBound = (lowerBound + this.pageLimit);

    if (pageNumber > pages) {
      String err = String.format("The specified page number **%d** is not within the range of valid pages.  The valid pages " +
              "are between **1** and **%d**.", pageNumber, pages);
      event.getMessage().getChannel().sendMessage(err).queue();
      return;
    }

    if (upperBound > sortedCommands.size()) {
      upperBound = sortedCommands.size();
    }

    sortedCommands = sortedCommands.subList(lowerBound, upperBound);

    builder = addCommandsToEmbed(builder, sortedCommands, event);

    String pageOutput = String.format("Currently viewing page %d of %d", pageNumber, pages);
    builder.addField(pageOutput, "Use --page to specify a page number", false);

    event.getChannel().sendMessage(builder.build()).queue();
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setPageLimit(int pageLimit) {
    this.pageLimit = pageLimit;
  }

  public int getPageLimit() {
    return this.pageLimit;
  }

  private EmbedBuilder addCommandsToEmbed(EmbedBuilder builder, List<Command> commands,
      MessageReceivedEvent event) {
    for (Command command : commands) {
      if (CommandRegistrar.REGISTRAR.commandRolesNotMet(event, command)) {
        continue;
      }
      builder.addField(command.getCommandName(), command.getDescription(), false);
    }
    return builder;
  }
}
