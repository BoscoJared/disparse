package disparse.parser;

import disparse.discord.AbstractPermission;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.dispatch.IncomingScope;

import java.time.Duration;
import java.util.Objects;

public class Command {

  private final String name;
  private final String description;
  private final String[] roles;
  private final boolean canBeDisabled;
  private final Duration cooldownDuration;
  private final CooldownScope scope;
  private final boolean sendCooldownMessage;
  private final IncomingScope acceptFrom;
  private final String[] aliases;
  private final AbstractPermission[] perms;
  private String parentName = null;

  public Command(final String name, final String description) {
    this(name, description, new String[]{}, true, Duration.ZERO, CooldownScope.USER, false, IncomingScope.ALL, new String[]{}, new AbstractPermission[]{});
  }

  public Command(final String name, final String description, final boolean canBeDisabled) {
    this(name, description, new String[]{}, canBeDisabled, Duration.ZERO, CooldownScope.USER, false, IncomingScope.ALL, new String[]{}, new AbstractPermission[]{});
  }

  public Command(final String name,
                 final String description,
                 final String[] roles,
                 final boolean canBeDisabled,
                 final Duration cooldownDuration,
                 final CooldownScope scope,
                 final boolean sendCooldownMessage,
                 final IncomingScope acceptFrom,
                 final String[] aliases,
                 final AbstractPermission[] perms) {
    this.name = name;
    this.description = description;
    this.roles = roles;
    this.canBeDisabled = canBeDisabled;
    this.cooldownDuration = cooldownDuration;
    this.scope = scope;
    this.sendCooldownMessage = sendCooldownMessage;
    this.acceptFrom = acceptFrom;
    this.aliases = aliases;
    this.perms = perms;
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

  public Duration getCooldownDuration() {
    return cooldownDuration;
  }

  public CooldownScope getScope() {
    return scope;
  }

  public boolean isSendCooldownMessage() {
    return sendCooldownMessage;
  }

  public IncomingScope getAcceptFrom() {
    return this.acceptFrom;
  }

  public String[] getAliases() {
    return this.aliases;
  }

  public AbstractPermission[] getPerms() {
    return this.perms;
  }

  public String getParentName() {
    return this.parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
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
