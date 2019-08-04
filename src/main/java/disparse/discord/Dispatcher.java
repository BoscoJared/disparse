package disparse.discord;

import java.util.List;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Dispatcher extends ListenerAdapter {

    private String prefix;

    public Dispatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith(this.prefix)) {
            return;
        }
        String cleanedMessage = raw.replace(this.prefix, "");
        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.registrar.dispatch(args, event);
    }

    public static JDABuilder init(JDABuilder builder, String prefix) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix);
        builder.addEventListeners(dispatcher);
        return builder;
    }
}
