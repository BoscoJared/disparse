# Command Handlers

The annotation for a command handler is: `@CommandHandler`

The annotation goes on methods that may either be non-static or static.

The method should be void as the return value will not be used anywhere.

The `@CommandHandler` annotation accepts the following fields:

| field_name    | type     | description                                                                                                     |
|---------------|----------|-----------------------------------------------------------------------------------------------------------------|
| commandName   | String   | Required.  The name of the command that should invoke this command.                                             |
| description   | String   | User-friendly / helpful description of a command to be used in generated help commands.                         |
| roles         | String[] | The roles that are allowed to call this command.  Default is empty array and allows anyone to call the command. |
| canBeDisabled | boolean  | If the command is allowed to be toggled on / off.  Default is true.                                             |

<br />
## Subcommands

Some commands should logically be grouped together, but with split actions.  There is also the possibility that some subset of users have access to some commands in a hierarchy, but elevated permissions are required for others in the same hierarchy.

The `commandName` of a method should just separate commands with a `.` to be able to designate a subcommand.

`audit.delete` would be invoked as `!audit delete`

## Parameters

Command Handlers can accept many parameters:

- Event type ( depends on discord library )
- List<String>: positional arguments
- Injectable: any Injectable defined in the project
- ParsedEntity: any ParsedEntity, this implicitly means the command now requires the flags defined in the ParsedEntity
- Helpable: class that gives you more meta access to Disparse's dispatch.  Help commands, prefixes, etc. can be changed here.

### What event type?

- JDA -> `net.dv8tion.jda.api.events.message.MessageReceivedEvent`
- D4J -> `discord4j.core.event.domain.message.MessageCreateEvent`
- SmallD -> `disparse.discord.smalld.Event`
- Unsupported -> If you implemented your own Dispatcher it is probably obvious which Event type you would be using as it is the same type defined in `Helpable<E, ?>
