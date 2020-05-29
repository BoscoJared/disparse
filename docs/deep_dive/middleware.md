# Middleware

Middleware is a collection of functions that are ran before a command is allowed to progress.  This is still very much a work in-progress, but as of right now a Middleware function is:

`BiFunction<E event, String commandName, Boolean result>`

This translates to a method signature of:

`boolean func(E event, String commandName) {}`

To register Middleware call `withMiddleware` on a Dispatcher builder.

```java
Dispatcher dispatcher = new Dispatcher.Builder()
    .withMiddleware((event, command) -> {
        return true;
    });
```

If a middleware function returns false, the command will not continue to execute.  A middlware function could be a good place to define a check for premium features on a per-guild / per-user basis.

```java
public class PremiumMiddleware implements BiFunction<MessageCreateEvent, String, Boolean> {

    private Set<String> premiumCommands = Set.of("ping");
    
    @Override
    public Boolean apply(MessageCreateEvent event, String s) {
        if (this.premiumCommands.contains(s)) {
            event.getChannel().sendMessage("Sorry, that's a premium-only feature!").queue();
            return false;
        }
        return true;
    }
}
```

The above proof-of-concept middleware class allows you to look-up premium commands before allowing the command to execute.  This allows the user to extend the dispatch logic without needing a custom dispatch method.
