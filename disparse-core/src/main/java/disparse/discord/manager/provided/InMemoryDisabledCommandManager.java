package disparse.discord.manager.provided;

import disparse.discord.manager.DisabledCommandManager;
import disparse.parser.Command;
import java.util.*;

public class InMemoryDisabledCommandManager implements DisabledCommandManager {

  private final Map<String, Set<Command>> guildToCommand = new HashMap<>();

  @Override
  public boolean commandAllowedInGuild(String guildId, Command command) {
    return !guildToCommand.getOrDefault(guildId, Collections.emptySet()).contains(command);
  }

  @Override
  public void disableCommandForGuild(String guildId, Command command) {
    guildToCommand.putIfAbsent(guildId, new HashSet<>());
    guildToCommand.get(guildId).add(command);
  }

  @Override
  public void enableCommandForGuild(String guildId, Command command) {
    guildToCommand.getOrDefault(guildId, Collections.emptySet()).remove(command);
  }
}
