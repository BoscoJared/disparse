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

  public static <T> AbstractDiscordResponse<T> of(String message) {
    return new AbstractDiscordResponse<>(message);
  }

  public static <T> AbstractDiscordResponse<T> of(T builder) {
    return new AbstractDiscordResponse<>(builder);
  }

  public static <T> AbstractDiscordResponse<T> noop() {
    return new AbstractDiscordResponse<>();
  }
}
