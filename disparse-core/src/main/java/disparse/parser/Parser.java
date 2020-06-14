package disparse.parser;

import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequiresValue;
import java.util.*;

public class Parser {

  private final Map<Command, ? extends Collection<CommandFlag>> commandToFlags;
  private final Map<String, CommandFlag> longOptionMap = new HashMap<>();
  private final Map<Character, CommandFlag> shortOptionMap = new HashMap<>();

  public Parser(final Map<Command, ? extends Collection<CommandFlag>> commandToFlags) {
    this.commandToFlags = commandToFlags;
  }

  private static void accept(
      Map<CommandFlag, Object> optionMap, CommandFlag flag, Iterator<String> iter) {
    if (flag.getType() == Types.BOOL) {
      optionMap.put(flag, true);
    } else if (Types.allLists().contains(flag.getType())) {
      optionMap.putIfAbsent(flag, new ArrayList<String>());
      if (iter.hasNext()) {
        ((ArrayList<String>) optionMap.get(flag)).add(iter.next());
        iter.remove();
      } else {
        throw new OptionRequiresValue("Option requires value", flag);
      }
    } else {
      if (iter.hasNext()) {
        optionMap.put(flag, iter.next());
        iter.remove();
      } else {
        throw new OptionRequiresValue("Option requires value", flag);
      }
    }
  }

  public ParsedOutput parse(List<String> args) {
    final Command commandName = this.findCommand(args);

    fillShortAndLongOptions(commandName);

    final Map<CommandFlag, Object> options = this.findOptions(args);

    return new ParsedOutput(commandName, args, options);
  }

  private Command findCommand(List<String> args) {
    List<String> currArgs = new ArrayList<>(args);
    Optional<Command> commandName = Optional.empty();
    int skip = 0;
    while (currArgs.size() > 0) {
      Command possibleCommand = new Command(String.join(".", currArgs), "");
      if (this.commandToFlags.containsKey(possibleCommand)) {
        commandName = Optional.of(possibleCommand);
        skip = currArgs.size();
        break;
      } else {
        currArgs.remove(currArgs.size() - 1);
      }
    }
    int i = 0;
    for (Iterator<String> iter = args.iterator(); iter.hasNext(); ) {
      if (i < skip) {
        iter.next();
        iter.remove();
        i++;
      } else {
        break;
      }
    }
    return commandName.orElseThrow(() -> new NoCommandNameFound("A valid command was not found!"));
  }

  private void fillShortAndLongOptions(Command commandName) {
    for (CommandFlag flag : this.commandToFlags.get(commandName)) {
      this.longOptionMap.put(flag.getLongName(), flag);
      this.shortOptionMap.put(flag.getShortName(), flag);
    }
  }

  private Map<CommandFlag, Object> findOptions(List<String> args) {
    final Map<CommandFlag, Object> optionMap = new HashMap<>();
    final Iterator<String> iter = args.iterator();

    while (iter.hasNext()) {
      final String currArg = iter.next();
      CommandFlag flag = null;

      if (currArg.startsWith("--")) {
        final String currOpt = currArg.substring(2);
        flag = this.longOptionMap.getOrDefault(currOpt, null);
      } else if (currArg.startsWith("-")) {
        final String currOpt = currArg.substring(1);
        if (currOpt.isEmpty()) { // this was a standalone "-", perhaps it was just an argument?
          continue;
        }
        flag = this.shortOptionMap.getOrDefault(currOpt.charAt(0), null);
      }

      if (flag != null) {
        iter.remove();
        accept(optionMap, flag, iter);
      }
    }

    return optionMap;
  }
}
