package disparse.discord.manager;

import disparse.discord.AbstractDispatcher;

public interface PageLimitManager<E, T> {

  int pageLimitForGuild(E event, AbstractDispatcher<E, T> dispatcher);

  void setPageLimitForGuild(E event, AbstractDispatcher<E, T> dispatcher, int pageLimit);
}
