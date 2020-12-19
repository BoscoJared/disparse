package disparse.utils.help;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Help {

  private static final Comparator<CommandFlag> helpCommandFlagComparator =
      new Comparator<>() {
        private Comparator<CommandFlag> comparator =
            Comparator.comparing(
                this::identity,
                (left, right) -> {
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
                  } else if (leftShortOnly) {
                    return -1;
                  } else if (rightShortOnly) {
                    return 1;
                  } else if (leftBoth && rightBoth) {
                    return leftLong.compareTo(rightLong);
                  } else if (leftBoth) {
                    return -1;
                  } else if (rightBoth) {
                    return 1;
                  } else if (leftLongOnly && rightLongOnly) {
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

  public static List<String> commandNotFound(String input, String prefix) {
    String first = "`" + input + "` is not a valid command!";
    String second = "Use " + prefix + "help to get a list of all available commands.";

    return List.of(first, second);
  }

  public static String optionRequired(Command command, CommandFlag flag) {
    return "The flag `--"
        + flag
        + "` is required for `"
        + command.getCommandName()
        + "` to be ran!";
  }

  public static String optionRequiresValue(CommandFlag flag) {
    String flagName;
    if (!flag.getLongName().equals("")) {
      flagName = "--" + flag.getLongName();
    } else {
      flagName = "-" + flag.getShortName();
    }
    return "The flag `" + flagName + "` was not provided a value!";
  }

  public static List<String> incorrectOption(String userChoice, String flagName, String options) {
    String first =
        "`" + userChoice + "` is not a valid option for the enum flag:  `" + flagName + "`";
    String second = "Pick from:  " + options;

    return List.of(first, second);
  }

  public static String roleNotMet(Command command) {
    String base = "You do not have the correct permissions.";
    if (command == null) {
      return base;
    } else {
      return base.substring(0, base.length() - 1) + " to run:  `" + command.getCommandName() + "`";
    }
  }

  public static List<Command> findSubcommands(Command command, Collection<Command> commands) {
    Predicate<Command> predicate =
        c ->
            c.getCommandName().startsWith(command.getCommandName() + ".")
                && !c.getCommandName().equals(command.getCommandName());

    Comparator<Command> comparator =
        Comparator.comparing(cmd -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder());

    return commands.stream().filter(predicate).sorted(comparator).collect(Collectors.toList());
  }

  public static List<CommandFlag> sortFlags(Collection<CommandFlag> flags) {
    return flags.stream().sorted(helpCommandFlagComparator).collect(Collectors.toList());
  }

  public static List<Command> sortCommands(Collection<Command> commands) {
    return commands.stream()
        .sorted(
            Comparator.comparing(
                (Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
        .collect(Collectors.toList());
  }

  public static PaginatedEntities paginate(
      Collection<Command> commands, Collection<CommandFlag> flags, int pageNumber, int pageLimit)
      throws PageNumberOutOfBounds {

    int skipNum = (pageNumber - 1) * pageLimit;
    int total = commands.size() + flags.size();
    int pages = (int) Math.ceil((double) (total) / pageLimit);

    if (pageNumber > pages || pageNumber < 1) throw new PageNumberOutOfBounds(pageNumber, pages);

    List<Object> entities =
        Stream.concat(commands.stream(), flags.stream()).skip(skipNum).collect(Collectors.toList());

    int rest = pageLimit;

    if (rest > entities.size()) {
      rest = entities.size();
    }

    entities = entities.subList(0, rest);

    List<Command> cmds = new ArrayList<>();
    List<CommandFlag> flgs = new ArrayList<>();

    for (Object entity : entities) {
      if (entity instanceof Command) {
        cmds.add((Command) entity);
      } else if (entity instanceof CommandFlag) {
        flgs.add((CommandFlag) entity);
      }
    }

    return new PaginatedEntities(cmds, flgs, pageNumber, pages);
  }

  public static String flagToUserFriendlyString(CommandFlag flag) {
    String flagName;
    if (flag.getShortName() == null) {
      flagName = String.format("--%s", flag.getLongName());
    } else if (flag.getLongName() == null) {
      flagName = String.format("-%s", flag.getShortName());
    } else {
      flagName = String.format("-%s | --%s", flag.getShortName(), flag.getLongName());
    }

    if (flag.isRequired()) {
      flagName = flagName + "*";
    }

    if (Types.allLists().contains(flag.getType())) {
      flagName = flagName + " [+]";
    }

    return flagName;
  }
}
