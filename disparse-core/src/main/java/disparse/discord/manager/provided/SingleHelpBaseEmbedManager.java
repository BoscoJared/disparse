package disparse.discord.manager.provided;

import disparse.discord.AbstractDispatcher;
import disparse.discord.manager.HelpBaseEmbedManager;

import java.util.function.Supplier;

public class SingleHelpBaseEmbedManager<E, T> implements HelpBaseEmbedManager<E, T> {
  private final Supplier<T> builderSupplier;

  public SingleHelpBaseEmbedManager(Supplier<T> builderSupplier) {
    this.builderSupplier = builderSupplier;
  }

  @Override
  public T baseHelpEmbedForGuild(E event, AbstractDispatcher<E, T> dispatcher) {
    return this.builderSupplier.get();
  }
}
