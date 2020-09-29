package disparse.testing.smalld.commands;

import disparse.discord.smalld.DiscordResponse;
import disparse.parser.reflection.CommandHandler;

public class HelloWorld {
  @CommandHandler(commandName = "helloworld")
  public static DiscordResponse helloWorldSmallD() {
    return DiscordResponse.of("Hello, World!");
  }
}
