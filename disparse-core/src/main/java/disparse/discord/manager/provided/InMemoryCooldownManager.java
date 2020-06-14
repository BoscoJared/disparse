package disparse.discord.manager.provided;

import disparse.discord.manager.CooldownManager;
import disparse.parser.dispatch.CooldownCompositeKey;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCooldownManager implements CooldownManager {
  private ConcurrentHashMap<CooldownCompositeKey<String>, Instant> cooldowns =
      new ConcurrentHashMap<>();

  public Duration timeLeft(CooldownCompositeKey<String> cooldownCompositeKey, Duration amount) {
    return timeLeft(cooldowns.get(cooldownCompositeKey), amount);
  }

  public void cooldown(CooldownCompositeKey<String> cooldownCompositeKey) {
    this.cooldowns.put(cooldownCompositeKey, Instant.now());
  }
}
