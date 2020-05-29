package disparse.discord.manager.provided;

import disparse.discord.AbstractDispatcher;
import disparse.discord.manager.DescriptionManager;

public class SingleDescriptionManager<E, T> implements DescriptionManager<E, T> {

    private final String description;

    public SingleDescriptionManager(String description) {
        this.description = description;
    }

    @Override
    public String descriptionForGuild(E event, AbstractDispatcher<E, T> dispatcher) {
        return description;
    }
}
