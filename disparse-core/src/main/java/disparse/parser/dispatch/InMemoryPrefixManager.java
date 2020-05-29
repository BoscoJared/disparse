package disparse.parser.dispatch;

import disparse.discord.AbstractDispatcher;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPrefixManager<E, T> implements PrefixManager<E, T> {

    private final String defaultPrefix;
    private final ConcurrentHashMap<String, String> prefixMap = new ConcurrentHashMap<>();

    public InMemoryPrefixManager(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public String prefixForGuild(E event, AbstractDispatcher<E, T> dispatcher) {
        String guildId = dispatcher.guildFromEvent(event);

        if (guildId == null) return defaultPrefix;

        return this.prefixMap.getOrDefault(guildId, defaultPrefix);
    }

    @Override
    public void setPrefixForGuild(E event, AbstractDispatcher<E, T> dispatcher, String prefix) {
        String guildId = dispatcher.guildFromEvent(event);

        if (guildId == null) return;

        this.prefixMap.put(guildId, prefix);
    }
}
