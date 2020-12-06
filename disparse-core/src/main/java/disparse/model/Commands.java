package disparse.model;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.Types;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Class for holding and answering information about all commands that disparse currently knows
 * about without having to pass around individual data structures
 */
public class Commands {
  private final HashMap<Command, Method> commandToMethod = new HashMap<>();
  private final HashMap<Command, Set<CommandFlag>> commandToFlags = new HashMap<>();
  private final CommandFlag helpFlag =
      new CommandFlag(
          "help", 'h', Types.BOOL, false, "show usage of a particular command", Map.of());
  private final Command helpCommand =
      new Command("help", "show all commands or detailed help of one command", false);
  private final CommandFlag helpPageFlag =
      new CommandFlag(
          "page", 'p', Types.INT, false, "select a specific page to showcase", Map.of());

  public Commands() {
    this.registerCommandToMethod(helpCommand, null);
    this.registerFlagToCommand(helpCommand, helpPageFlag);
  }

  public void registerCommandToMethod(Command command, Method method) {
    this.registerFlagToCommand(command, helpFlag);
    this.commandToMethod.put(command, method);
  }

  public void registerFlagToCommand(Command command, CommandFlag flag) {
    this.commandToFlags.putIfAbsent(command, new HashSet<>());
    this.commandToFlags.get(command).add(flag);
  }

  public Set<Command> allCommands() {
    return this.commandToMethod.keySet();
  }

  public boolean commandExists(Command command) {
    return this.commandToMethod.containsKey(command);
  }

  public Set<CommandFlag> getFlagsForCommand(Command command) {
    return this.commandToFlags.getOrDefault(command, new HashSet<>());
  }

  public Optional<Method> getMethodForCommand(Command command) {
    return Optional.ofNullable(this.commandToMethod.getOrDefault(command, null));
  }

  public Map<Command, Set<CommandFlag>> getCommandToFlags() {
    return this.commandToFlags;
  }
}
