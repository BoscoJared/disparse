package disparse.parser;

import java.util.Objects;

public class Command {

  private final String name;
  private final String description;
  private final String[] roles;
  private final boolean canBeDisabled;

  public Command(final String name, final String description) {
    this(name, description, new String[0], true);
  }
  
  public Command(final String name, final String description, final boolean canBeDisabled) {
      this(name, description, new String[0], canBeDisabled);
  }

  public Command(final String name, final String description, final String[] roles, final boolean canBeDisabled) {
    this.name = name;
    this.description = description;
    this.roles = roles;
    this.canBeDisabled = canBeDisabled;
  }

  public String getCommandName() {
    return this.name;
  }

  public String getDescription() {
    return this.description;
  }

  public String[] getRoles() {
    return this.roles;
  }
  
  public boolean canBeDisabled() {
      return this.canBeDisabled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Command command = (Command) o;
    return name.equals(command.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
