package disparse.discord;

import disparse.parser.Command;
import disparse.parser.Flag;

import java.util.Collection;

public interface Helpable<E> {
    void help(E event, Command command, Collection<Flag> flags);
    void allCommands(E event, Collection<Command> commands);
    void setPrefix(String prefix);
    void commandNotFound(E event, String userInput);
    void roleNotMet(E event, Command command);
}
