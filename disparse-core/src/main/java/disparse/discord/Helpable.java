package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import java.util.Collection;

public interface Helpable<E> {

  void help(E event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber);

  void allCommands(E event, Collection<Command> commands, int pageNumber);

  void setPrefix(String prefix);

  void commandNotFound(E event, String userInput);

  void helpSubcommands(E event, String foundPrefix, Collection<Command> commands);

  void roleNotMet(E event, Command command);

  void optionRequired(E event, String message);

  boolean commandRolesNotMet(E event, Command command);
}
