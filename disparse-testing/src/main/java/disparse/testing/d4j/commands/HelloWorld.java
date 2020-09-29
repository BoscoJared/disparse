package disparse.testing.d4j.commands;

import disparse.discord.d4j.DiscordResponse;
import disparse.parser.reflection.CommandHandler;

public class HelloWorld {

  @CommandHandler(commandName = "helloworld")
  public static DiscordResponse helloWorldD4J() {
    return DiscordResponse.of("Hello, World!");
  }
}
