# disparse-jda

The `disparse-jda` module wraps and augments the [JDA](https://github.com/DV8FromTheWorld/JDA) library.  `disparse-jda` builds upon [disparse-core](https://github.com/BoscoJared/disparse/tree/master/disparse-core).

## Getting Started

### Writing Your First Command
At the simplest level, a command can be written as a `static void` method without any parameters.  While this is usually quite boring, understanding the flexibility provided by Disparse is important in beginning to write more complex commands.

One such command could look like:

```java
import disparse.parser.reflection.CommandHandler;

public class Example {

    @CommandHandler(commandName = "heartbeat")
    public static void heartbeat() {
        System.out.println("beat");
    }
}
```

Assuming a prefix of `!`, this command could be called from Discord using `!heartbeat` which would simply print to the console `beat`.

#### Replying To Discord

Bots would be quite uninteresting if they could never interact and respond to Discord users calling them.  We can change the above basic command to reply to the same channel from where the message came.  We do this by accepting a parameter of MessageReceivedEvent which is a type provided by JDA itself.

```java
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Example {
    
    @CommandHandler(commandName = "heartbeat")
    public static void heartbeat(MessageReceivedEvent event) {
        event.getChannel().sendMessage("beat").queue();
    }
}
```

And voila, we have exposed the MessageReceivedEvent from JDA, and responded to the channel with a message of "beat".  This command still would only respond if the message was `!heartbeat` but now we are able to communicate back to the users in Discord!

#### Extending Functionality

Even the above could get a bit boring, after all not many people would like to pull a dependency for what essentially accounts for mapping a command name to a method, right?

Disparse's strength comes in its ability to parse incoming messages in a CLI-like way, exposing them to your command handlers, and allowing you to simply focus on writing the business logic of your particular command.  To do this, we must look at ParsedEntities.

A ParsedEntity is a class marked with the annotation `@ParsedEntity`.  This class can have fields marked as `@Flag` to be used in a similar way as a command-line flag.

```java
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;

public class Example {

    @ParsedEntity
    private static class ExampleOptions {
        @Flag(shortName = 'n', longName = "number")
        Integer number = 1;
    }  

}
```

This ParsedEntity has a single field `number` which will be referred to by a short `-n` or a long `--number`.  Notice the double dash for long names, with a single dash for short names.

We are able to ask for this ParsedEntity to be filled out and passed along in our command handler.  We can use the entity to decide on business logic for our command.  In this case, we will make the bot reply `number` times.

```java
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Example {

    @ParsedEntity
    private static class ExampleOptions {
        @Flag(shortName = 'n', longName = "number")
        Integer number = 1;
    }  

    @CommandHandler(commandName = "heartbeat")
    public static void heartbeat(MessageReceivedEvent event, ExampleOptions options) {
        if (options.number <= 0) {
            options.number = 1;
        }

        for (int i = 0; i < options.number; i++) {
            event.getChannel().sendMessage("beat").queue();
        }
    }
}
```

Now we have a command that has some logic based off of the ParsedEntity.  It will reply `number` times to the channel in Discord with the message `beat`.  

Now to use this command, you could continue using `!heartbeat` where `number` will default to `1`; However, we can now use the flag in our command, which Disparse will handle for us:

`!heartbeat --number 5` # number will be 5, beat sent 5 times

`!heartbeat -n 5` # number will be 5, beat sent 5 times

And, by default disparse will have a `help` command to learn more about a particular command.  It can be accessed by:

```
!help heartbeat
!heartbeat -h
!heartbeat --help
```

You will notice that none of the commands or flags have descriptions.  The annotations have a description field that you can use to have rich help dialogues with users.  

`@Flag(shortName = 'n', longName = "number", description = "specify the number of times a message is sent!")`

`@CommandHandler(commandName = "heartbeat", description = "Reply with a beat so you know the bot is living!")`

### Maven

```xml
<dependency>
    <groupId>com.github.BoscoJared.disparse</groupId>
    <artifactId>disparse-jda</artifactId>
    <version>$LATEST</version>
</dependency>
```

To use Jitpack you must also add a repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Gradle

```
dependencies {
    implementation 'com.github.BoscoJared.disparse:disparse-jda:$LATEST'
}
```

To use Jitpack you must also add a repository:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
