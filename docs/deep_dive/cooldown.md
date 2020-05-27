# Cooldown

The annotation for cooldown is: `@Cooldown`

The annotation goes on methods that are also marked with `@CommandHandler`

The method should also be void as the return value will not be used anywhere.

The `@Cooldown` annotation accepts the following fields:

| field_name          | type          | description                                                                    |
|---------------------|---------------|--------------------------------------------------------------------------------|
| amount              | int           | The amount of time to wait before allowed invocations.  Default is 0.          |
| unit                | ChronoUnit    | The type of unit to use with amount to create a Duration.  Default is SECONDS. |
| scope               | CooldownScope | The type of scope to handle cooldowns.  Default is USER.                       |
| sendCooldownMessage | boolean       | Should Disparse reply with a cooldown message?  Default is false.              |

<br>
## Scopes

Scopes are a way of supporting different strategies for cooldowns on commands.

The default Scope is `CooldownScope.USER` which means the command has a `per-user` cooldown.  Disparse will track and manage that a user can only invoke a command *at most* one time per cooldown period.

Another Scope is `CooldownScope.CHANNEL` which means the command has a `per-channel` cooldown.  Disparse will track and manage that any users collectively may only invoke a command *at most* one time per channel per cooldown period.

The final Scope is `CooldownScope.GUILD` which means the comand has a `per-guild` cooldown.  Disparse will track and manage that any users collectively may only invoke a command *at most* one time per guild per cooldown period.

## Cooldown Message

The message is quite basic so far, but eventually should be configurable perhaps even with a Formatter passed in.  Still, having any message could be useful.  Set `sendCooldownMessage` to `true` to have the bot reply when a user / channel / guild is on cooldown depending on its scope.

This could be spammy, so if you would use cooldowns to not spam text channels, false being the default is useful.
