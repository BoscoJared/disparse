package disparse.disparse;

import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;

import java.util.List;

public class TestDiscordRequest extends AbstractDiscordRequest<Object, StringBuilder> {
  public TestDiscordRequest(AbstractDispatcher<Object, StringBuilder> dispatcher, Object event, List<String> args) {
    super(dispatcher, event, args);
  }
}
