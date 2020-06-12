# Command Handlers

The annotation for a command handler is: `@CommandHandler`

The annotation goes on methods that may either be non-static or static.

The method may return nothing ( void ) or a `DiscordResponse`

The `@CommandHandler` annotation accepts the following fields:

| field_name    | type          | description                                                                                                     |
|---------------|---------------|-----------------------------------------------------------------------------------------------------------------|
| commandName   | String        | Required.  The name of the command that should invoke this command.                                             |
| description   | String        | User-friendly / helpful description of a command to be used in generated help commands.                         |
| roles         | String[]      | The roles that are allowed to call this command.  Default is empty array and allows anyone to call the command. |
| canBeDisabled | boolean       | If the command is allowed to be toggled on / off.  Default is true.                                             |
| canAccept     | IncomingScope | If the command should only respond to commands in specific types of channels.  Default is ALL.                  |
| aliases       | String[]      | Aliases that also point to the same command.  Default is empty array.                                           |

<br />
## Subcommands

Some commands should logically be grouped together, but with split actions.  There is also the possibility that some subset of users have access to some commands in a hierarchy, but elevated permissions are required for others in the same hierarchy.

The `commandName` of a method should just separate commands with a `.` to be able to designate a subcommand.

`audit.delete` would be invoked as `!audit delete`

## Parameters

Command Handlers can accept many parameters:

- DiscordRequest: Request holds the arguments, event, and dispatcher
- Injectable: any Injectable defined in the project
- ParsedEntity: any ParsedEntity, this implicitly means the command now requires the flags defined in the ParsedEntity

### What event type?

- JDA -> `net.dv8tion.jda.api.events.message.MessageReceivedEvent`
- D4J -> `discord4j.core.event.domain.message.MessageCreateEvent`
- SmallD -> `disparse.discord.smalld.Event`
- Unsupported -> If you implemented your own Dispatcher it is probably obvious which Event type you would be using.


### Return Values

For convenience, command handlers can simply return a DiscordResponse.  These will be automatically sent to the same channel as the event came in on.  If there needs to be more control, a void method accepting the DiscordRequest will be the easiest way to decide exactly where a response should be sent.

Consider the following:

```java
@CommandHandler(commandName = "ping")
public static void pingVerbose(DiscordRequest req) {
    req.getEvent().getChannel().sendMessage("pong").queue();
}
```

could be rewritten as:

```java
@CommandHandler(commandName = "ping")
public static DiscordResponse pingShort() {
    return DiscordResponse.of("pong");
}
```

Sending Embeds depends on the specific module being used, but they are all similar and it is the specific type that will change.

```java
@CommandHandler(commandName = "embed")
public static void sendEmbedVerbose(DiscordRequest req) {
    Embed embed = new EmbedBuilder()
        .setTitle("hello")
        .setDescription("world!").build();

    req.getEvent().getChannel().sendEmbed(embed).queue();
}
```

could be rewritten as:

```java
public static DiscordResponse sendEmbedShort() {
    return DiscordResponse.of(new EmbedBuilder()
        .setTitle("hello")
        .setDescription("world!"));
}
```

If a handler decides to do nothing for some reason, such as an argument
was incorrectly formatted and the developer does not want to return an error
message as it was an obvious typo, it is possible to return a noop DiscordResponse.

```java
public static DiscordResponse sendEmbed() {
  return DiscordResponse.noop();
}
```

This will not send any message or embed to the user even when they call the command
successfully.

If more complex behavior is desired, switch to the void return type and work with the event directly by calling `DiscordRequest#getEvent`.

### What builder type?

- JDA -> `net.dv8tion.jda.api.EmbedBuilder`
- D4J -> `discord4j.core.spec.EmbedCreateSpec`
- SmallD -> `com.google.gson.JsonElement`
