package com.github.boscojared.disparse.discord;

public class D4JDispatcher {

    /*public static DiscordClient init(DiscordClient client, final String prefix){
        Detector.detect();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .subscribe(event -> {
                    if (event.getMember().isEmpty() || event.getMember().get().isBot()) {
                        return;
                    }

                    String raw = event.getMessage().getContent().orElse("");

                    if (!raw.startsWith(prefix)) {
                        return;
                    }
                    String cleanedMessage = raw.replace(prefix, "");
                    List<String> args = Shlex.shlex(cleanedMessage);
                    CommandRegistrar.registrar.dispatch(args, event);
                });
        return client;
    }*/
}
