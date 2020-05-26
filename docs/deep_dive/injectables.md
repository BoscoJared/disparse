# Injectables

Injectables are really simple methods that accept no arguments and return some type T.  In this way, they are essentially marked `Supplier<T>` as they are `<T> () -> T`

These are helpful when you need your command to have an object instantiated and passed along into the command handler.  For example, there could be some database context to setup.

```java
@Injectable
public static void createDatabaseContext() {
    return new DatabaseContext();
}

@CommandHandler(commandName = "log")
public static void log(DatabaseContext ctx) {
    ctx.insert(1).commit();
}
```

When dispatching to your command handler, disparse scans all parameters and tries to find an Injectable with a return type that would be a match.  Then, it invokes the Injectable Supplier method, and passes it along to your handler.
