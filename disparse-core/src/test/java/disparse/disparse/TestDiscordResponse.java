package disparse.disparse;

import disparse.discord.AbstractDiscordResponse;

public class TestDiscordResponse extends AbstractDiscordResponse<StringBuilder> {
  public TestDiscordResponse() {super(); }

  public TestDiscordResponse(String message) {
    super(message);
  }

  public TestDiscordResponse(StringBuilder builder) {
    super(builder);
  }

  public static TestDiscordResponse of(String message) {
    return new TestDiscordResponse(message);
  }

  public static TestDiscordResponse of(StringBuilder builder) {
    return new TestDiscordResponse(builder);
  }

  public static TestDiscordResponse noop() {
    return new TestDiscordResponse();
  }
}
