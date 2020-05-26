package disparse.parser.dispatch;

import disparse.parser.Command;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class Cooldown {
    private ConcurrentHashMap<IdentityCommandPair, Instant> userCooldown = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Command, Instant> commandCooldown = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ChannelCommandPair, Instant> channelCooldown = new ConcurrentHashMap<>();

    public Duration timeLeft(IdentityCommandPair identity, Duration amount) {
        return timeLeft(userCooldown.get(identity), amount);
    }

    public Duration timeLeft(Command identity, Duration amount) {
        return timeLeft(commandCooldown.get(identity), amount);
    }

    public Duration timeLeft(ChannelCommandPair identity, Duration amount) {
        return timeLeft(channelCooldown.get(identity), amount);
    }

    public Duration timeLeft(Instant then, Duration amount) {
        Instant now = Instant.now();

        if (then == null) return Duration.ZERO;

        Duration left = Duration.between(now, then.plus(amount));

        if (left.isNegative() || left.isZero()) {
            return Duration.ZERO;
        } else {
            return left;
        }
    }

    public void cooldown(IdentityCommandPair identity) {
        this.userCooldown.put(identity, Instant.now());
    }

    public void cooldown(Command identity) {
        this.commandCooldown.put(identity, Instant.now());
    }

    public void cooldown(ChannelCommandPair identity) {
        this.channelCooldown.put(identity, Instant.now());
    }
}
