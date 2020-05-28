package disparse.parser.dispatch;

import java.time.Duration;
import java.time.Instant;

public interface Cooldown {

    Duration timeLeft(Pair<String> pair, Duration amount);

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

    void cooldown(Pair<String> pair);
}
