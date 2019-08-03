# Disparse
An ergonomic, simple, and easy-to-use command parsing and dispatching library for Discord bots.


## Examples

### Bot Entry

```java
public class BotEntry {
    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = Dispatcher.init(new JDABuilder(System.getenv(TOKEN)), PREFIX).build();
        jda.awaitReady();
    }
}
```

### Simple command

Usage: `!ping`

```java
public class PingCommand {

    @CommandHandler(commandName = "ping")
    public static void ping(MessageReceivedEvent e) {
        e.getChannel().sendMessage("pong").queue();
    }
}
```

### Command with flags

Usage: `!echo -n 5` or `!echo --number 5`

```java
public class EchoCommand {

    @ParsedEntity
    static class EchoRequest {
        @Flag(shortName = 'n', longName = "number")
        Integer number;
    }

    @CommandHandler(commandName = "echo")
    public static void echo(EchoRequest req, MessageReceivedEvent e) {
        for (int i = 0; i < req.number; i++) {
            e.getChannel().sendMessage("echo").queue();
        }
    }
}
```
