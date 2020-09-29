package disparse.testing.jda.commands;

import disparse.discord.jda.DiscordResponse;
import disparse.parser.reflection.CommandHandler;

public class HelloWorld {

  @CommandHandler(commandName = "helloworld")
  public static DiscordResponse helloWorldJDA() {
    return DiscordResponse.of("Hello, World!");
  }
}
