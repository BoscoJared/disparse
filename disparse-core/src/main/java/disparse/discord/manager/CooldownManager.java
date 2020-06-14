package disparse.discord.manager;

import disparse.parser.dispatch.CooldownCompositeKey;
import java.time.Duration;
import java.time.Instant;

public interface CooldownManager {

  Duration timeLeft(CooldownCompositeKey<String> cooldownCompositeKey, Duration amount);

  default Duration timeLeft(Instant then, Duration amount) {
    Instant now = Instant.now();

    if (then == null) return Duration.ZERO;

    Duration left = Duration.between(now, then.plus(amount));

    if (left.isNegative() || left.isZero()) {
      return Duration.ZERO;
    } else {
      return left;
    }
  }

  void cooldown(CooldownCompositeKey<String> cooldownCompositeKey);
}
