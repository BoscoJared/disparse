package disparse.discord.smalld;

import com.google.gson.JsonElement;
import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;

import java.util.List;

public class DiscordRequest extends AbstractDiscordRequest<Event, JsonElement> {
  public DiscordRequest(AbstractDispatcher<Event, JsonElement> dispatcher, Event event, List<String> args) {
    super(dispatcher, event, args);
  }
}
