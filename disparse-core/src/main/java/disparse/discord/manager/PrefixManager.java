package disparse.discord.manager;

import disparse.discord.AbstractDispatcher;

public interface PrefixManager<E, T> {

    String prefixForGuild(E event, AbstractDispatcher<E, T> dispatcher);

    void setPrefixForGuild(E event, AbstractDispatcher<E, T> dispatcher, String prefix);
}
