package disparse.discord.manager.provided;

import disparse.discord.manager.CooldownManager;
import disparse.parser.dispatch.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCooldownManager implements CooldownManager {
    private ConcurrentHashMap<Pair<String>, Instant> cooldowns = new ConcurrentHashMap<>();

    public Duration timeLeft(Pair<String> pair, Duration amount) {
        return timeLeft(cooldowns.get(pair), amount);
    }

    public void cooldown(Pair<String> pair) { this.cooldowns.put(pair, Instant.now()); }

}
