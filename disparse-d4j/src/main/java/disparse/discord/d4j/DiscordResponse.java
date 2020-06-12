package disparse.discord.d4j;

import discord4j.core.spec.EmbedCreateSpec;
import disparse.discord.AbstractDiscordResponse;

public class DiscordResponse extends AbstractDiscordResponse<EmbedCreateSpec> {

  public DiscordResponse() {
    super();
  }

  public DiscordResponse(String message) {
    super(message);
  }

  public DiscordResponse(EmbedCreateSpec builder) {
    super(builder);
  }

  public static DiscordResponse of(String message) {
    return new DiscordResponse(message);
  }

  public static DiscordResponse of(EmbedCreateSpec builder) {
    return new DiscordResponse(builder);
  }

  public static DiscordResponse noop() {
    return new DiscordResponse();
  }
}
