package disparse.discord;

import java.util.List;

public class AbstractDiscordRequest<E, T> {

  private final AbstractDispatcher<E, T> dispatcher;
  private final E event;
  private final List<String> args;

  public AbstractDiscordRequest(AbstractDispatcher<E, T> dispatcher, E event, List<String> args) {
    this.dispatcher = dispatcher;
    this.event = event;
    this.args = args;
  }

  public T defaultBuilder() {
    return this.dispatcher.baseEmbedManager.baseHelpEmbedForGuild(event, this.dispatcher);
  }

  public void reply(String message) {
    this.dispatcher.sendMessage(event, message);
  }

  public void reply(T builder) {
    this.dispatcher.sendEmbed(event, builder);
  }

  public List<String> getArgs() {
    return args;
  }

  public E getEvent() {
    return event;
  }

  public AbstractDispatcher<E, T> getDispatcher() {
    return dispatcher;
  }
}
