# Prefix Manager

PrefixManager is an interface that creates a contract for getting a prefix for a guild, and setting a prefix for a guild.  PrefixManager is an interface with only one supplied implementation `InMemoryPrefixManager`

Disparse uses `InMemoryPrefixManager` as a default that is configurable and overrideable in the AbstractDispatcher.Builder class i.e.

```java
Dispatcher dispatcher = new Dispatcher.Builder()
    .withPrefixManager(new MyCustomPrefixManager())
    .build();
```

## Why add complexity?

Normally this would be a case of unnecessary complexity, but in-memory prefix strategies, while simple and good enough for many, might not cut it for bots aiming for a large number of Guilds.  It is expected that a bot aiming to be used on many Guilds would supply some implementation that checks configuration ( database, file, etc. ) to get the correct prefix for a guild.

This type of flexibility allows both simple and complex bots to benefit from Disparse, and a simple bot would most likely never need to change this strategy.
