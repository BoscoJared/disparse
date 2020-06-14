package disparse.discord.d4j;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import java.util.List;

public class DiscordRequest extends AbstractDiscordRequest<MessageCreateEvent, EmbedCreateSpec> {
  public DiscordRequest(
      AbstractDispatcher<MessageCreateEvent, EmbedCreateSpec> dispatcher,
      MessageCreateEvent event,
      List<String> args) {
    super(dispatcher, event, args);
  }
}
