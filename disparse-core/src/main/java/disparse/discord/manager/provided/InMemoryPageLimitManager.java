package disparse.discord.manager.provided;

import disparse.discord.AbstractDispatcher;
import disparse.discord.manager.PageLimitManager;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPageLimitManager<E, T> implements PageLimitManager<E, T> {

    private final ConcurrentHashMap<String, Integer> pageLimitMap = new ConcurrentHashMap<>();
    private final int defaultPageLimit;

    public InMemoryPageLimitManager(int pageLimit) {
        this.defaultPageLimit = pageLimit;
    }

    @Override
    public int pageLimitForGuild(E event, AbstractDispatcher<E, T> dispatcher) {
        String guildId = dispatcher.guildFromEvent(event);

        if (guildId == null) return this.defaultPageLimit;

        return this.pageLimitMap.getOrDefault(guildId, this.defaultPageLimit);
    }

    @Override
    public void setPageLimitForGuild(E event, AbstractDispatcher<E, T> dispatcher, int pageLimit) {
        String guildId = dispatcher.guildFromEvent(event);

        if (guildId == null) return;

        this.pageLimitMap.put(guildId, pageLimit);
    }
}
