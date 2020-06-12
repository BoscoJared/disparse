package disparse.parser;

public class CommandUsage {
  private final String usage;
  private final String description;

  public CommandUsage(String usage, String description) {
    this.usage = usage;
    this.description = description;
  }

  public String getUsage() {
    return usage;
  }

  public String getDescription() {
    return description;
  }
}
