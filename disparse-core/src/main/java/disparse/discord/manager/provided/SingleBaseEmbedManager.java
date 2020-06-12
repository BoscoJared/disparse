package disparse.discord.manager.provided;

import disparse.discord.AbstractDispatcher;
import disparse.discord.manager.BaseEmbedManager;

import java.util.function.Supplier;

public class SingleBaseEmbedManager<E, T> implements BaseEmbedManager<E, T> {
  private final Supplier<T> builderSupplier;

  public SingleBaseEmbedManager(Supplier<T> builderSupplier) {
    this.builderSupplier = builderSupplier;
  }

  @Override
  public T baseHelpEmbedForGuild(E event, AbstractDispatcher<E, T> dispatcher) {
    return this.builderSupplier.get();
  }
}
