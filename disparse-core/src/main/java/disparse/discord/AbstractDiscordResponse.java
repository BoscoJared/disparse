package disparse.discord;

import java.util.Optional;

public class AbstractDiscordResponse<T> {
  private final Optional<String> optionalMessage;
  private final Optional<T> optionalBuilder;

  public AbstractDiscordResponse() {
    this.optionalMessage = Optional.empty();
    this.optionalBuilder = Optional.empty();
  }

  public AbstractDiscordResponse(String message) {
    this.optionalMessage = Optional.of(message);
    this.optionalBuilder = Optional.empty();
  }

  public AbstractDiscordResponse(T builder) {
    this.optionalMessage = Optional.empty();
    this.optionalBuilder = Optional.of(builder);
  }

  public Optional<String> getOptionalMessage() {
    return optionalMessage;
  }

  public Optional<T> getOptionalBuilder() {
    return optionalBuilder;
  }
}
