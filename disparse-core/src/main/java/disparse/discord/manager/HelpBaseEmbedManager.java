package disparse.discord.manager;

import disparse.discord.AbstractDispatcher;

public interface HelpBaseEmbedManager<E, T> {
  T baseHelpEmbedForGuild(E event, AbstractDispatcher<E, T> dispatcher);
}
