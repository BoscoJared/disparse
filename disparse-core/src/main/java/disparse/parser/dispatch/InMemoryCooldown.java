package disparse.parser.dispatch;

import disparse.parser.Command;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCooldown implements Cooldown {
    private ConcurrentHashMap<Pair<String>, Instant> cooldowns = new ConcurrentHashMap<>();

    public Duration timeLeft(Pair<String> pair, Duration amount) {
        return timeLeft(cooldowns.get(pair), amount);
    }

    public void cooldown(Pair<String> pair) { this.cooldowns.put(pair, Instant.now()); }

}
