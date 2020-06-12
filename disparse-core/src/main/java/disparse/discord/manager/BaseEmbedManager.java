package disparse.discord.manager;

import disparse.discord.AbstractDispatcher;

public interface BaseEmbedManager<E, T> {
  T baseHelpEmbedForGuild(E event, AbstractDispatcher<E, T> dispatcher);
}
