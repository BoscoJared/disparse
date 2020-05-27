package disparse.parser.dispatch;

import disparse.parser.Command;

import java.time.Duration;
import java.time.Instant;

public interface Cooldown {

    public Duration timeLeft(IdentityCommandPair identity, Duration amount);

    public Duration timeLeft(Command identity, Duration amount);

    public Duration timeLeft(ChannelCommandPair identity, Duration amount);

    public default Duration timeLeft(Instant then, Duration amount) {
        Instant now = Instant.now();

        if (then == null) return Duration.ZERO;

        Duration left = Duration.between(now, then.plus(amount));

        if (left.isNegative() || left.isZero()) {
            return Duration.ZERO;
        } else {
            return left;
        }
    }

    public void cooldown(IdentityCommandPair identity);

    public void cooldown(Command identity);

    public void cooldown(ChannelCommandPair identity);
}
