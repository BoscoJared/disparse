package disparse.discord.jda;

import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class DiscordRequest extends AbstractDiscordRequest<MessageReceivedEvent, EmbedBuilder> {
  public DiscordRequest(AbstractDispatcher<MessageReceivedEvent, EmbedBuilder> dispatcher, MessageReceivedEvent event, List<String> args) {
    super(dispatcher, event, args);
  }
}
