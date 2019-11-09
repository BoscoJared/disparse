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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
      new Command("help", "show all commands or detailed help of one command");
  private final HashMap<Command, Method> commandTable = new HashMap<>();
  private final HashMap<Command, Set<CommandFlag>> commandToFlags = new HashMap<>();
  private final List<Method> injectables = new ArrayList<>();

  private CommandRegistrar() {
    this.commandToFlags.put(helpCommand, Set.of());
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

  public void dispatch(List<String> args, Helpable<E> helper, E event, Object... injectables) {

    Parser parser = new Parser(this.commandToFlags);
    ParsedOutput output;
    try {
      output = parser.parse(args);
    } catch (NoCommandNameFound exec) {
      helper.commandNotFound(event, args.get(0));
      return;
    }
    Command command = output.getCommand();
    if (!commandTable.containsKey(command)) {
      return;
    }

    for (Command c : commandTable.keySet()) {
      if (c.equals(command)) {
        command = c;
      }
    }

    if (commandRolesNotMet(command, event)) {
      helper.roleNotMet(event, command);
      return;
    }

    boolean help = (boolean) output.getOptions().getOrDefault(helpFlag, false);
    if (command.getCommandName().equals("help")) {
      if (args.size() > 0) {
        String name = args.get(0);
        for (Command c : commandTable.keySet()) {
          if (c.getCommandName().equals(name)) {
            command = c;
          }
        }
        help = true;
      } else {
        helper.allCommands(event, commandTable.keySet());
        return;
      }
    }
    if (help) {
      helper.help(event, command, commandToFlags.get(command), commandTable.keySet());
      return;
    }
    try {
      Method handler = commandTable.get(command);
      Object[] objects = new Object[handler.getParameterTypes().length];

      int i = 0;
      for (Class<?> clazz : handler.getParameterTypes()) {
        if (clazz.isAnnotationPresent(ParsedEntity.class)) {
          Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
          constructor.setAccessible(true);
          Object newObject = constructor.newInstance();

          for (Field field : Detector.allImplicitFields(clazz)) {
            if (field.isAnnotationPresent(Flag.class)) {
              Flag flagAnnotation = field.getAnnotation(Flag.class);

              CommandFlag flag = Utils.createFlagFromAnnotation(field, flagAnnotation);
              if (output.getOptions().containsKey(flag)) {
                Object val = output.getOptions().get(flag);
                field.setAccessible(true);

                if (flag.getType().equals(Types.INT)) {
                  field.set(newObject, Integer.parseInt((String) val));
                } else {
                  field.set(newObject, val);
                }
              } else if (flag.isRequired()) {
                throw new OptionRequired("The flag `--" + flag + "` is required for `"
                    + command.getCommandName() + "` to be ran!");
              }
            }
          }
          objects[i] = newObject;
        } else {
          if (clazz.isAssignableFrom(List.class)) {
            objects[i] = args;
          }
          if (clazz.isAssignableFrom(event.getClass())) {
            objects[i] = event;
          }
          if (clazz.isAssignableFrom(helper.getClass())) {
            objects[i] = helper;
          }
          for (Object injectable : injectables) {
            if (clazz.isAssignableFrom(injectable.getClass())) {
              objects[i] = injectable;
            }
          }

          for (Method injectable : this.injectables) {
            if (clazz.isAssignableFrom(injectable.getReturnType())) {
              objects[i] = injectable.invoke(null);
            }
          }
        }
        i++;
      }
      handler.setAccessible(true);
      Constructor<?> constructor = handler.getDeclaringClass().getDeclaredConstructor();
      constructor.setAccessible(true);
      Object handlerObj = constructor.newInstance();
      handler.invoke(handlerObj, objects);
    } catch (ReflectiveOperationException exec) {
      logger.error("Error occurred", exec);
    } catch (OptionRequired exec) {
      helper.optionRequired(event, exec.getMessage());
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

  public boolean commandRolesNotMet(MessageReceivedEvent event, Command command) {
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
}
