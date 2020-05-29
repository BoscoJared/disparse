package disparse.discord.manager;

import disparse.discord.AbstractDispatcher;

public interface DescriptionManager<E, T> {

    String descriptionForGuild(E event, AbstractDispatcher<E, T> dispatcher);

}
