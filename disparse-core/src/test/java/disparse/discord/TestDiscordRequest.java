package disparse.discord;

import java.util.List;

public class TestDiscordRequest extends AbstractDiscordRequest<Object, StringBuilder> {
  public TestDiscordRequest(
      AbstractDispatcher<Object, StringBuilder> dispatcher, Object event, List<String> args) {
    super(dispatcher, event, args);
  }
}
