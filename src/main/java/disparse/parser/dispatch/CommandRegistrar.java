package disparse.parser.dispatch;

import disparse.discord.Helpable;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.ParsedOutput;
import disparse.parser.Parser;
import disparse.parser.Types;
import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequired;
import disparse.parser.reflection.Detector;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegistrar<E> {

  public static final CommandRegistrar REGISTRAR = new CommandRegistrar();
  private static final Logger logger = LoggerFactory.getLogger(CommandRegistrar.class);
  private final CommandFlag helpFlag =
      new CommandFlag("help", 'h', Types.BOOL, false, "show usage of a particular command");
  private final Command helpCommand =
      new Command("help", "show all commands or detailed help of one command", false);
  private final CommandFlag helpPageFlag =
          new CommandFlag("page", 'p', Types.INT, false, "select a specific page to showcase");
  private final HashMap<Command, Method> commandTable = new HashMap<>();
  private final HashMap<Command, Set<CommandFlag>> commandToFlags = new HashMap<>();
  private final List<Method> injectables = new ArrayList<>();
  private final Map<Command, CommandContainer> disabledCommands = new HashMap<>();

  private CommandRegistrar() {
    this.commandToFlags.put(helpCommand, Set.of(helpPageFlag));
    this.commandTable.put(helpCommand, null);
  }

  public void register(Command command, Method method) {
    this.commandToFlags.putIfAbsent(command, new HashSet<>());
    this.commandToFlags.get(command).add(helpFlag);
    this.commandTable.put(command, method);
  }

  public void register(Command command, CommandFlag flag) {
    this.commandToFlags.putIfAbsent(command, new HashSet<>());
    this.commandToFlags.get(command).add(flag);
  }

  public void register(Method method) {
    this.injectables.add(method);
  }

public void dispatch(List<String> args, Helpable<E> helper, E event) {
  ParsedOutput parsedOutput = this.parse(new ArrayList<>(args), helper, event);
  if (parsedOutput == null) return;

  Command command = parsedOutput.getCommand();
  if (!commandTable.containsKey(command)) return;

  if (commandRolesNotMet(command, event)) {
    helper.roleNotMet(event, command);
    return;
  }

  if (this.help(args, helper, event, parsedOutput, command)) return;

  try {
    this.emitCommand(args, helper, event, parsedOutput, command);
  } catch (ReflectiveOperationException exec) {
    logger.error("Error occurred", exec);
  } catch (OptionRequired exec) {
    helper.optionRequired(event, exec.getMessage());
  }
}

  private boolean help(List<String> args, Helpable<E> helper, E event, ParsedOutput parsedOutput, Command command) {
    if (command.getCommandName().equalsIgnoreCase("help")) {
      this.helpCommand(args, helper, event, parsedOutput);
      return true;
    } else if ((boolean) parsedOutput.getOptions().getOrDefault(helpFlag, false)){
      this.emitHelp(args, helper ,event, command);
      return true;
    }

    return false;
  }

  private void helpCommand(List<String> args, Helpable<E> helper, E event, ParsedOutput parsedOutput) {
    args = new ArrayList<>(args);

    if (args.size() <= 1) {
      int pageLimit = Integer.parseInt((String) parsedOutput.getOptions().getOrDefault(helpPageFlag, "1"));
      helper.allCommands(event, commandTable.keySet(), pageLimit);
      return;
    }

    Command foundCommand = null;
    args.remove(0); // remove "help" command name
    String name = args.get(0);
    name = String.join(".", name.split(" "));

    for (Command c : commandTable.keySet()) {
      if (c.getCommandName().equals(name)) {
        foundCommand = c;
      }
    }

    if (foundCommand == null) { // foundCommand is null so we need to prefix match to see if we can provide more help
      foundCommand = this.prefixHelp(args, helper, event, name);
    }

    if (foundCommand == null) return; // no prefix was matched so we can't really help

    this.emitHelp(args, helper, event, foundCommand);
  }

  private Command prefixHelp(List<String> args, Helpable<E> helper, E event, String prefix) {
    PrefixContainer prefixContainer = findCommandPrefixes(commandTable.keySet(), args);
    List<Command> prefixes = prefixContainer.getPrefixes();
    String foundPrefix = prefixContainer.getFoundPrefix();
    if (prefixes.size() == 0) {
      helper.commandNotFound(event, prefix.replace(".", " "));
    } else if (prefixes.size() == 1 && !commandRolesNotMet(prefixes.get(0), event)) {
      return prefixes.get(0);
    } else {
      helper.helpSubcommands(event, foundPrefix, prefixes);
    }
    return null;
  }

  private void emitHelp(List<String> args, Helpable<E> helper, E event, Command command) {
    List<String> translatedArgs = new ArrayList<>();
    Parser parser = new Parser(this.commandToFlags);
    translatedArgs.add("help");
    translatedArgs.addAll(args);
    ParsedOutput parsedOutput = parser.parse(translatedArgs);
    // This should only fail due to programmer error, so the cast *should* be safe... famous last words
    int pageLimit = Integer.parseInt((String) parsedOutput.getOptions().getOrDefault(helpPageFlag, "1"));
    helper.help(event, command, commandToFlags.get(command), commandTable.keySet(), pageLimit);
  }

  private void emitCommand(List<String> args, Helpable<E> helper, E event, ParsedOutput parsedOutput, Command foundCommand)
    throws ReflectiveOperationException, OptionRequired {
      Method commandHandler = commandTable.get(foundCommand);
      Object[] objects = new Object[commandHandler.getParameterTypes().length];

      int i = 0;
      for (Class<?> clazz : commandHandler.getParameterTypes()) {
        if (clazz.isAnnotationPresent(ParsedEntity.class)) {
          Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
          constructor.setAccessible(true);
          Object newObject = constructor.newInstance();

          for (Field field : Detector.allImplicitFields(clazz)) {
            if (field.isAnnotationPresent(Flag.class)) {
              Flag flagAnnotation = field.getAnnotation(Flag.class);

              CommandFlag flag = Utils.createFlagFromAnnotation(field, flagAnnotation);
              if (parsedOutput.getOptions().containsKey(flag)) {
                Object val = parsedOutput.getOptions().get(flag);
                field.setAccessible(true);

                if (flag.getType().equals(Types.INT)) {
                  field.set(newObject, Integer.parseInt((String) val));
                } else {
                  field.set(newObject, val);
                }
              } else if (flag.isRequired()) {
                throw new OptionRequired("The flag `--" + flag + "` is required for `"
                        + foundCommand.getCommandName() + "` to be ran!");
              }
            }
          }
          objects[i] = newObject;
        } else {
          this.fillObjectArr(objects, i, clazz, args, event, helper);
        }
        i++;
      }
      commandHandler.setAccessible(true);
      Object handlerObj = null; // null can work for static methods invocation
      Constructor<?>[] ctors = commandHandler.getDeclaringClass().getDeclaredConstructors();
      Arrays.sort(ctors, Comparator.comparing(Constructor::getParameterCount, Comparator.reverseOrder()));

      Object[] bestCtorParams = null;
      Constructor<?> bestCtor = null;
      int bestNonNull = Integer.MAX_VALUE;

      for (Constructor<?> ctor : ctors) {
        Object[] ctorParams = new Object[ctor.getParameterCount()];
        int idx = 0;

        for (Class<?> clazz : ctor.getParameterTypes()) {
          this.fillObjectArr(ctorParams, idx, clazz, args, event, helper);
          idx++;
        }

        boolean noneNull = Arrays.stream(ctorParams).noneMatch(Objects::isNull);

        if (noneNull) {
          bestCtor = ctor;
          bestCtorParams = ctorParams;
          break;
        } else {
          int amountNotNull = (int) Arrays.stream(ctorParams).filter(Objects::isNull).count();
          if (ctor.getParameterCount() > 0 && amountNotNull < bestNonNull) {
            bestCtor = ctor;
            bestCtorParams = ctorParams;
          }
        }
      }

      if (bestCtor != null) {
        handlerObj = bestCtor.newInstance(bestCtorParams);
      }

      commandHandler.invoke(handlerObj, objects);
  }

  private void fillObjectArr(Object[] objects, int index, Class<?> clazz, List<String> args, E event, Helpable<E> helper)
          throws ReflectiveOperationException {
    if (clazz.isAssignableFrom(List.class)) {
      objects[index] = args;
    }

    if (clazz.isAssignableFrom(event.getClass())) {
      objects[index] = event;
    }

    if (clazz.isAssignableFrom(helper.getClass())) {
      objects[index] = helper;
    }

    for (Method injectable : this.injectables) {
      if (clazz.isAssignableFrom(injectable.getReturnType())) {
        objects[index] = injectable.invoke(null);
      }
    }
  }

  private ParsedOutput parse(List<String> args, Helpable<E> helper, E event) {
    Parser parser = new Parser(this.commandToFlags);

    try {
      return parser.parse(args);
    } catch (NoCommandNameFound noCommandNameFound) {
      noCommandNameFound(args, helper, event);
    }

    return null;
  }

  private void noCommandNameFound(List<String> args, Helpable<E> helper, E event) {
    PrefixContainer prefixContainer = findCommandPrefixes(commandTable.keySet(), args);
    List<Command> prefixMatchedCommands = prefixContainer.getPrefixes();
    String foundPrefix = prefixContainer.getFoundPrefix();

    if (prefixMatchedCommands.size() == 0) {
      // no prefixes matched means no valid command was found
      helper.commandNotFound(event, args.get(0));
    } else if (prefixMatchedCommands.size() == 1) {
      // one prefix matched means we can directly help for this command
      Command matchedCommand = prefixMatchedCommands.get(0);
      helper.help(event, matchedCommand, commandToFlags.get(matchedCommand), commandTable.keySet(), 1);
    } else {
      helper.helpSubcommands(event, foundPrefix, prefixMatchedCommands);
    }
  }

  /**
   * Checks the specified roles of a command against the roles of the user attempting to call the
   * command
   *
   * @param command the command that has been parsed
   * @param event   the event from the message listener
   * @return true if the user does not have sufficient privilege
   */
  private boolean commandRolesNotMet(Command command, E event) {
    if (event instanceof MessageReceivedEvent) {
      return commandRolesNotMet((MessageReceivedEvent) event, command);
    }
    return false;
  }

  public static boolean commandRolesNotMet(MessageReceivedEvent event, Command command) {
    if (command.getRoles().length == 0) {
      return false;
    }
    if (event instanceof MessageReceivedEvent) {
      MessageReceivedEvent e = (MessageReceivedEvent) event;
      Member member = e.getMember();
      if (member != null) {
        for (String commandRole : command.getRoles()) {
          if (commandRole.equalsIgnoreCase("owner") && e.getMember().isOwner()) {
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
    return false;
  }

  public void disableCommand(String commandName) throws IllegalArgumentException {
    Command command = findCommand(commandTable.keySet(), commandName)
        .orElseThrow(() -> new IllegalArgumentException("Command could not be found"));
    if (!command.canBeDisabled()) {
      throw new IllegalArgumentException("Command cannot be disabled");
    }
    Method method = commandTable.get(command);
    Set<CommandFlag> flags = commandToFlags.get(command);
    disabledCommands.put(command, new CommandContainer(method, flags));
    commandTable.remove(command);
    commandToFlags.remove(command);
  }

  public void enableCommand(String commandName) throws IllegalArgumentException {
    Command command = findCommand(disabledCommands.keySet(), commandName)
        .orElseThrow(() -> new IllegalArgumentException("Command could not be found"));
    CommandContainer fullCommand = disabledCommands.get(command);
    if (fullCommand == null) {
      throw new IllegalArgumentException("Command is not disabled");
    }
    commandTable.put(command, fullCommand.getMethod());
    commandToFlags.put(command, fullCommand.getFlags());
    disabledCommands.remove(command);
  }

  private Optional<Command> findCommand(Set<Command> commands, String commandName) {
    return commands.stream().filter(command -> command.getCommandName().equals(commandName))
        .findFirst();
  }

  private PrefixContainer findCommandPrefixes(Set<Command> commands, List<String> args) {

    List<Command> prefixes = new ArrayList<>();

    for (int i = args.size(); i > 0; i--) {
      List<String> trimmed = args.subList(0, i);
      String possibleCommandName = String.join(".", trimmed);

      prefixes = commands.stream()
              .filter(command -> command.getCommandName().startsWith(possibleCommandName))
              .collect(Collectors.toList());

      if (prefixes.size() > 0) {
        return new PrefixContainer(possibleCommandName, prefixes);
      }
    }

    return new PrefixContainer("", prefixes);
  }

  class CommandContainer {
    private Method method;
    private Set<CommandFlag> flags;

    CommandContainer(Method method, Set<CommandFlag> flags) {
      this.method = method;
      this.flags = flags;
    }

    public Method getMethod() {
      return method;
    }

    public Set<CommandFlag> getFlags() {
      return flags;
    }
  }

  class PrefixContainer {
    private String foundPrefix;
    private List<Command> prefixes;

    PrefixContainer(String foundPrefix, List<Command> prefixes) {
      this.foundPrefix = foundPrefix;
      this.prefixes = prefixes;
    }

    public String getFoundPrefix() { return this.foundPrefix; }

    public List<Command> getPrefixes() { return this.prefixes; }
  }
}
