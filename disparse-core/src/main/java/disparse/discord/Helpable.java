package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.utils.help.Help;

import java.util.Collection;

public interface Helpable<E> {

  void help(E event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber);

  void allCommands(E event, Collection<Command> commands, int pageNumber);

  void setPrefix(String prefix);

  String getPrefix();

  void helpSubcommands(E event, String foundPrefix, Collection<Command> commands);

  boolean commandRolesNotMet(E event, Command command);

  void sendMessage(E event, String message);

  default void sendMessages(E event, Collection<String> messages) {
    for (String message : messages) {
      sendMessage(event, message);
    }
  }

  default void commandNotFound(E event, String userInput) {
    sendMessages(event, Help.commandNotFound(userInput, getPrefix())
    );
  }

  default void roleNotMet(E event, Command command) {
    sendMessage(event, Help.roleNotMet(command));
  }

  default void optionRequired(E event, Command command, CommandFlag flag) {
    sendMessage(event, Help.optionRequired(command, flag));
  }

  default void optionRequiresValue(E event, CommandFlag flag) {
    sendMessage(event, Help.optionRequiresValue(flag));
  }

  default void incorrectOption(E event, String userChoice, String flagName, String options) {
    sendMessages(event, Help.incorrectOption(userChoice, flagName, options));
  }

}
