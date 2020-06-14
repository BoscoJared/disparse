package disparse.parser.dispatch;

import java.util.Objects;

public class CooldownCompositeKey<T> {
  private final T guildId;
  private final T val;
  private final T commandName;

  public CooldownCompositeKey(final T guildId, final T val, final T commandName) {
    this.guildId = guildId;
    this.val = val;
    this.commandName = commandName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CooldownCompositeKey<?> that = (CooldownCompositeKey<?>) o;
    return Objects.equals(guildId, that.guildId)
        && Objects.equals(val, that.val)
        && commandName.equals(that.commandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guildId, val, commandName);
  }

  public static <T> CooldownCompositeKey<T> of(final T guildId, final T val, final T commandName) {
    return new CooldownCompositeKey<>(guildId, val, commandName);
  }
}
