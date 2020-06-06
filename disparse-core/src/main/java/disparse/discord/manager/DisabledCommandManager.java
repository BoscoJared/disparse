package disparse.discord.manager;

import disparse.parser.Command;

public interface DisabledCommandManager {
  boolean commandAllowedInGuild(String guildId, Command command);

  void disableCommandForGuild(String guildId, Command command);

  void enableCommandForGuild(String guildId, Command command);
}
