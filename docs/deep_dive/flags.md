# Flags

The annotation for a flag is: `@Flag`

The annotation goes on fields of a `@ParsedEntity`

The types of fields that `@Flag` supports are:

- Integer
- Boolean
- String
- List
- Enum

The `@Flag` annotation accepts the following fields:

| field_name  | type            | description                                                                                                                   |
|-------------|-----------------|-------------------------------------------------------------------------------------------------------------------------------|
| shortName   | char            | The shortName of a flag would be referred to as `-` + shortName i.e. `-h`.  Leave out if you do not want a shortName.         |
| longName    | String          | The longName of a flag would be referred to as `--` + longName i.e. `--help`.  Leave out if you do not want a longName.       |
| required    | boolean         | If the flag is required for the command to be ran or not.  Default is false.                                                  |
| description | String          | User-friendly / helpful description of a flag to be used in generated help commands.                                          |
| choices     | ChoiceMapping[] | An array of ChoiceMappings that have a userChoice and mapTo.  This allows you to map user-friendly strings to Enum variants.  |

## ChoiceMapping

ChoiceMapping is an annotation ( `@ChoiceMapping` ) that is used in the `choices` array of a Flag.  The mapping is because complex types like `Map` are not able to be put into an annotation in Java.  The mapping simply maps a user-friendly string to an Enum variant so that a user does not need to use the exact Java Enum String to invoke a command.

For example, given the following enum:

```java
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
```

and ParsedEntity:

```java
@ParsedEntity
public class DirectionOpts {
    @Flag(shortName = 'd')
    Direction direction = Direction.UP;

    @CommandHandler(commandName = "walk")
    public static void walk(DirectionOpts opts) {
        System.out.println(opts.direction);
    }
}
```

A user would need to use this command like `!walk -d DOWN`.  This gets less diserable as Enum standards can have UPPER_SNAKE_CASE.

We can mitigate this with a ChoiceMapping:

```java
@ParsedEntity
public class DirectionOpts {
    @Flag(shortName = 'd',
        choices = {
            @ChoiceMapping(userChoice = "up", mapTo = "UP"),
            @ChoiceMapping(userChoice = "down", mapTo = "DOWN"),
            @ChoiceMapping(userChoice = "left", mapTo = "LEFT"),
            @ChoiceMapping(userChoice = "right", mapTo = "RIGHT")
    })
    Direction direction = Direction.UP;

    @CommandHandler(commandName = "walk")
    public static void walk(DirectionOpts opts) {
        System.out.println(opts.direction);
    }
}
```

Now the user is able to simply use the command like `!walk -d up`

## Restrictions

There is one global flag used for help command generation.  The flag is `-h / --help` and  There cannot be duplicates of flag shortName / longName therefore these reserved flags are not able to be used in user-defined commands.  

`--help` is able to be used on any command for help. `!walk -h` 
