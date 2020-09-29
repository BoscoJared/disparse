package disparse.testing.d4j;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import disparse.discord.d4j.Dispatcher;
import disparse.testing.DisparseRunner;
import java.util.concurrent.Executors;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class D4JApplication implements DisparseRunner {

  @Override
  public void start(String token) throws Exception {
    Dispatcher.Builder builder =
        new Dispatcher.Builder(D4JApplication.class)
            .prefix("&")
            .pageLimit(10)
            .withReflections(
                new Reflections(
                    new ConfigurationBuilder()
                        .setUrls(
                            ClasspathHelper.forPackage(D4JApplication.class.getPackage().getName()))
                        .filterInputsBy(new FilterBuilder().include("disparse.testing.d4j.*"))
                        .setScanners(new MethodAnnotationsScanner())))
            .withExecutorService(Executors.newFixedThreadPool(10))
            .disallowIncomingBotMessages()
            .description("A canary testing bot for disparse!");

    DiscordClient client = DiscordClient.create(token);
    GatewayDiscordClient gateway = client.login().block();

    Dispatcher.init(gateway, builder.build());

    gateway.onDisconnect().block();
  }
}
