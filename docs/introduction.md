# Introduction

## What is Disparse?

Disparse is an ergonomic, simple, and easy-to-use command parsing and dispatching library for Discord bots.  Disparse aims to lower the barrier to entry of making complex and useful Discord bots.  Using Disparse allows a developer to focus more on actual business logic of the bot, and less on how a command flows through their system end-to-end.

**Disclaimer:  Disparse is currently less than v1.0 and therefore the API is not stabilized yet as [SemVer](https://semver.org/) is followed.  There are no guarantees of backwards compatibility during the early stages of development.**

## Features

Disparse offers the ability to easily create commands, as well as handle their invocation in a CLI-like way.  Disparses features include:

- [Easy command creation](#creating-a-command)
- Command aliases
- [Command arguments](#arguments)
- [Command flags](#flags)
    - [Flag types](#flag-types)
    - [Repeatable flags](#flag-types)
- [Role based access](#roles)
- [Perm based access](#perms)
- [Help command generation](#help-commands)
- [Subcommands](#subcommands)
- Cooldowns / rate limiting commands
- [Injectable command entities](#injectables)

Disparse does not make a choice for which Discord library to use.  The API is designed in such a way that it is possible to plug new libraries in easily.  Currently, three libraries are supported:

- [JDA](https://github.com/DV8FromTheWorld/JDA)
- [D4J](https://github.com/Discord4J/Discord4J)
- [SmallD](https://github.com/princesslana/smalld)

It is possible that more libraries will be added; However, it is quite simple to allow Disparse to be used with a new library which will be explained in EXTENDING DISPARSE.

### Creating a command

A Command can be made quite simply by annotating a method with [`@CommandHandler`](https://github.com/BoscoJared/disparse/blob/master/src/main/java/disparse/parser/reflection/CommandHandler.java).  At the most basic, CommandHandler requires only a command name.  The method itself does not have to accept any parameters:

```java
@CommandHandler(commandName = "heartbeat")
public static void heartbeat() {
    System.out.println("Got a heartbeat!");
}
```

Assuming a prefix of `!`, this command would be invoked with `!heartbeat`.

Unfortunately, a command that cannot respond to the user is quite boring.  To allow us to respond to a command invocation, we are able to accept an event in our handler.  The specific type we accept depends on which library you are choosing to use. 

- JDA -> `net.dv8tion.jda.api.events.message.MessageReceivedEvent`
- D4J -> `discord4j.core.event.domain.message.MessageCreateEvent`
- SmallD -> `disparse.discord.smalld.Event`

The rest of the introduction will assume JDA, so the command handler will now look like:

```java
@CommandHandler(commandName = "heartbeat")
public static void heartbeat(MessageReceivedEvent event) {
    event.getChannel().sendMessage("Got a heartbeat!").queue();
}
```

Now, `!heartbeat` will respond to the channel where the command was sent with `Got a heartbeat!`. 

### Arguments

Arguments are anything passed to a command that is not associated with a command flag, or the command name itself.  In our heartbeat example, the commandName is `heartbeat` and we have defined no flags to be used; Therefore, any extra information passed along with the command would be considered as an argument.

To have access to these positional arguments, your command handler simply needs to accept a `List<String>` parameter.  The Strings will be in the List in the same order that the user put them in during the command.

```java
@CommandHandler(commandName = "heartbeat")
public static void heartbeat(List<String> args) {
    args.forEach(System.out::println);
}
```

Now using `!heartbeat 1 2 3 4 5` will call this heartbeat method.

```
1
2
3
4
5
```

**Note:** Even though all of these arguments are integers, they are still passed as Strings.  Disparse does no fancy parsing and casting; Therefore it is up to you to handle the arguments correctly for your specific use-case.

### Flags

Flags can be passed to a command, much like flags on any standard command line program.  To mark a field as a Flag, use the [`@Flag`](https://github.com/BoscoJared/disparse/blob/master/disparse-core/src/main/java/disparse/parser/reflection/Flag.java) annotation.  The class the flag is in must be marked as a [`@ParsedEntity`](https://github.com/BoscoJared/disparse/blob/master/disparse-core/src/main/java/disparse/parser/reflection/ParsedEntity.java).

```java
public class EchoCommand {

    @ParsedEntity
    static class EchoRequest {
        @Flag(shortName = 'n', longName = "number", description = "The number of times to repeat the content.")
        Integer number = 1;
        
        @Flag(longName = "content", required = true)
        String content;
    }
    
    @CommandHandler(commandName = "echo")
    public static void echo(EchoRequest req, MessageReceivedEvent event) {
        for (int i = 0; i < req.number; i++) {
            event.getChannel().sendMessage("echo" + req.content).queue();
        }
    }
}
```

This command now accepts two flags `--number` and `--content` where content is a required flag.  Accepting the `ParsedEntity` class in the command handler is enough to make disparse parse and handle these flags in an incoming command.

#### Flag Types

There are currently five flag types:
 - Integer: Can hold integer values such as `!test --age 5`.
 - Boolean: A boolean flag that if present is true.  This flag does not accept a value -> `!test --update`
 - String - Holds a String.  If content has spaces, it must be placed in double quotes -> `!test --content "my content"`.  If your content has double quotes in it, you can use single quotes to contain it instead -> `!test --content '{"my": "content"}'`.
 - List: Can be used for repeatable flags.  The List can also be of the types `Integer`, `Boolean`, `String` e.g. `!test -n 5 -n 6 -n 7 -n 8` would give a List of `[5, 6, 7, 8]`.
 - Enum: Any custom Enum value can be used.  The user can pick the enum for the flag based on the full name of the option, or it can be mapped to a more user-friendly string.
 
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

Usage:

`!test --age 5 --content "This is some content" -u -n 5 -n 6 -n 7 -n 8 --choice maybe`

### Roles

Roles can be used with commands to limit access.  You can provide a single role requirement, or multiple roles as a string array.

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

### Perms

Permissions are more general than role names.  This is a great way to limit commands to users that have certain general permissions within the executing guild.  Roles tie the command to a specific role name, which would not scale to multiple guilds not using the same role names for permissions.

```java
@CommandHandler(commandName = "ban", description = "Ban mentioned users", perms = AbstractPemission.BAN_MEMBERS)
public static void execute(MessageReceivedEvent e) {
    MessageChannel channel = e.getChannel();
    e.getMessage().getMentionedMembers().forEach(
            member -> channel.sendMessage(
                    e.getMember().getEffectiveName() + "You've been warned!"));
}
```

Now any user must possess the BAN_MEMBERS permission to be able to call and execute this command!

### Help commands

Help commands are automatically generated by disparse. The help commands reserve two flags, `-h/--help` and `-p/--page`. Don't use these in your own commands. The help command also reserves the `help` command name. The help command will only display commands & subcommands that the calling user has access to.

 - `!help` will show a list of all commands along with their description.
 - `!command -h` or `!command --help` or `!help command` will show a list of flags for that command and their descriptions. 
 
 Both the general help command, and command specific help will show any [subcommands](subcommands).
 
### Subcommands

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

### Injectables

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
