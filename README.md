# Disparse
An ergonomic, simple, and easy-to-use command parsing and dispatching library for Discord bots.


# Features
 - [Easy command creation](#basic-command)
 - [Command arguments](#arguments)
 - [Command flags](#flags)
    - [Flag types](#flag-types)
    - [Repeatable flags](#flag-types)
 - [Role based access](#roles)
 - [Help command generation](#help-commands)
 - [Subcommands](#subcommands)
 - [Injectable command entities](#injectables)
 - [Toggling commands](#toggling-commands)

# Guide

## Bot Entry

```java
public class BotEntry {
    
    private static final String PREFIX = "!";
    
    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = Dispatcher.init(new JDABuilder(System.getenv(TOKEN)), PREFIX).build();
        jda.awaitReady();
    }
}
```

## Basic command

A command can be created by using the [`@CommandHandler`](https://github.com/BoscoJared/disparse/blob/master/src/main/java/disparse/parser/reflection/CommandHandler.java) annotation on a method. At it's most basic, the `CommandHandler` will need a commandName and a description. The method must accept a [`MessageReceivedEvent`](https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/api/events/message/MessageReceivedEvent.java).

Usage: `!ping`

```java
public class PingCommand {

    @CommandHandler(commandName = "ping", description = "A command to send a simple reply.")
    public static void ping(MessageReceivedEvent e) {
        e.getChannel().sendMessage("pong").queue();
    }
}
```
## Arguments

Arguments are anything passed to a command that is not part of the command name itself or attached to any [flag](#flags).

These arguments are passed in-order, and therefore could be used as positional arguments.

To accept arguments in your command handler, simply have a parameter of `List<String>`:

```java
@CommandHandler(commandName = "ping")
public static void ping(List<String> args) {
    args.forEach(System.out::println);
}
```

Now using `!ping 1 2 3 4 5` will get us to call the `ping` method with arguments `["1", "2", "3", "4", "5"]`.  Note that the arguments are always strings, even if we only passed integer types.  Disparse does nothing but collect up the arguments for your command handler to then use as it pleases.

## Flags

Flags can be passed to a command, much like flags on any standard command line program. Flags can have a single character "short name" or a multi-character "long name". Flags can also be provided with a description, this is used in the [help command](help-commands). Additionally flags can be marked as required, by default they are not required.

Usage: `!echo -n 5` or `!echo --number 5 --content "hello world"`

```java
public class EchoCommand {

    @ParsedEntity
    static class EchoRequest {
        @Flag(shortName = 'n', longName = "number", description = "The number of times to repeat the content.")
        Integer number;
        
        @Flag(longName = "content", required = true)
        String content;
    }

    @CommandHandler(commandName = "echo")
    public static void execute(EchoRequest req, MessageReceivedEvent e) {
        for (int i = 0; i < req.number; i++) {
            e.getChannel().sendMessage("echo" + req.content).queue();
        }
    }
}
```

### Flag types

There are currently five flag types:
 - Integer - can hold integer values such as `!test --age 5`
 - Boolean - a boolean flag that if called, is true `!test -u`
 - String - holds a string. If content has spaces, it must be placed in double quotes `!test --content "content"` or `!test --content "my content"`. If your string flag content has double quotes in, then you can use single quotes to contain the content instead. `!test --content '{"my" : "content"}'`.
 - List - can be used for repeatable flags. The List can be of the types `Integer`, `Boolean`, or `String`. e.g. `!test -n 5 -n 6 -n 7 -n 8`
 - Enum - Any Enum value can be used.  The user can pick the enum for the flag based on the full name of the option, or it can be mapped to a more user-friendly string
 
 
 Usage: `!test --age 5 --content "This is some content" -u -n 5 -n 6 -n 7 -n 8`
```java
public class FlagExample {
    @ParsedEntity
    static class FlagRequest {
        @Flag(longName = "age")
        Integer age;
        
        @Flag(longName = "content")
        String content;
        
        @Flag(shortName = 'u', longName = "upperCase")
        Boolean upperCase;
        
        @Flag(shortName ='n', longName ="nums")
        List<Integer> numbers;

        @Flag(shortName = 'c', longName = "choice",
        choices = {
            @ChoiceMapping(userChoice = "yes", mapTo = "YES",
            @ChoiceMapping(userChoice = "no", mapTo = "NO",
            @ChoiceMapping(userChoice = "maybe", mapTo = "MAYBE")))
        })
        Choice choice = Choice.NO;
    }

    enum Choice { YES, NO, MAYBE }
       
    @CommandHandler(commandName = "test")
    public static void execute(FlagRequest req, MessageReceivedEvent e) {
        e.getChannel().sendMessage("Your age is: " + req.age).queue();
        if (Boolean.TRUE.equals(req.upperCase)) {
            req.content = req.content.toUpperCase();
        }
        e.getChannel().sendMessage("Your content is: " + req.content).queue();
        e.getChannel().sendMessage("Your numbers are: " + req.numbers).queue();
        e.getChannel().sendMessage("Your choice was:  " + req.choice).queue();
    }
}
```

## Roles

Roles can be used with commands to limit access. You can provide a single role requirement, or multiple roles as a string array.

A special case exists for the `"owner"` role. If a command is supplied with the `"owner"` role then only the server owner can execute this command.

```java
@CommandHandler(commandName = "ban", description = "Bans mentioned users", roles = "admin")
public static void execute(MessageReceivedEvent e) {
    e.getMessage().getMentionedMembers().forEach(member -> member.ban(7));
}
```

```java
@CommandHandler(commandName = "warn", description = "Warn mentioned users", roles = {"admin", "mod"})
public static void execute(MessageReceivedEvent e) {
    MessageChannel channel = e.getChannel();
    e.getMessage().getMentionedMembers().forEach(
            member -> channel.sendMessage(
                    e.getMember().getEffectiveName() + "You've been warned!"));
}
```

## Help commands

Help commands are automatically generated by disparse. The help commands reserve two flags, `-h` and `--help`. Don't use these in your own commands. The help command also reserves the `help` command name. The help command will only display commands & subcommands that the calling user has access to.

 - `!help` will show a list of all commands along with their description.
 - `!command -h` or `!command --help` will show a list of flags for that command and their descriptions. 
 
 Both the general help command, and command specific help will show any [subcommands](subcommands)

## Subcommands

Subcommands are a way of breaking down commands into modular chunks. Perhaps you want a command that allows everyone basic access, but only moderators should be allowed to edit the content of the command.

Normal users can use `!about` to display the about message.
Moderators can use `!about admin --content "A new about message"` to edit the about message

```java
@ParsedEntity
static class AboutRequest {
    @Flag(longName = "content")
    String content;
}

@CommandHandler(commandName = "about", description = "Shows info about the server")
public static void about(MessageReceivedEvent e) {
    e.getChannel().sendMessage(retrieveAboutData());
}

@CommandHandler(commandName = "about.admin", description = "Edits the content of the about message.", roles = "moderator")
public static void edit(AboutRequest req, MessageReceivedEvent e) {
    editAboutMessage(req.content);
}
```

## Injectables

Injectables are a way of using external resources in your commands. Often bots will have some database to hold useful data. Injectables allow you to pass database connections into command methods automatically. Injectables use the [`@Injectable`](https://github.com/BoscoJared/disparse/blob/master/src/main/java/disparse/parser/reflection/Injectable.java) annotation.

In this example, we inject a database context and use it to log the user's warning into the database.

```java
@Injectable
public static DBContext createDBContext() {
    return new DBContext();
}

@CommandHandler(name = "warn")
public static void warn(DBContext ctx, MessageReceivedEvent e) {
    ctx.logWarningInDatabase(e.getMessage().getMentionedMembers());
}
```

## Toggling Commands

`CommandRegistrar` provides and interface to enable and disable commands. Disabling a command will remove it from the Command Table. It will not be executable, nor be displayed in the help message. Commands have a `canBeDisabled` property which is true by default. 
