package disparse.parser.exceptions;

import disparse.parser.Command;
import disparse.parser.CommandFlag;

public class OptionRequired extends RuntimeException {

  private Command command;
  private CommandFlag flag;

  public OptionRequired(String message, Command command, CommandFlag flag) {
    super(message);
    this.command = command;
    this.flag = flag;
  }

  public Command getCommand() {
    return command;
  }

  public CommandFlag getFlag() {
    return flag;
  }
}
