package disparse.discord.smalld;

import com.google.gson.JsonElement;
import disparse.discord.AbstractDiscordResponse;

public class DiscordResponse extends AbstractDiscordResponse<JsonElement> {

  public DiscordResponse() {
    super();
  }

  public DiscordResponse(String message) {
    super(message);
  }

  public DiscordResponse(JsonElement builder) {
    super(builder);
  }

  public static DiscordResponse of(String message) {
    return new DiscordResponse(message);
  }

  public static DiscordResponse of(JsonElement builder) {
    return new DiscordResponse(builder);
  }

  public static DiscordResponse noop() {
    return new DiscordResponse();
  }
}
