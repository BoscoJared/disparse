package disparse.testing.jda;

import disparse.discord.jda.Dispatcher;
import disparse.testing.DisparseRunner;
import java.awt.*;
import java.util.concurrent.Executors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class JDAApplication implements DisparseRunner {
  @Override
  public void start(String token) throws Exception {
    Dispatcher.Builder builder =
        new Dispatcher.Builder(JDAApplication.class)
            .prefix("&")
            .pageLimit(10)
            .withExecutorService(Executors.newFixedThreadPool(10))
            .disallowIncomingBotMessages()
            .withReflections(
                new Reflections(
                    new ConfigurationBuilder()
                        .setUrls(
                            ClasspathHelper.forPackage(JDAApplication.class.getPackage().getName()))
                        .filterInputsBy(new FilterBuilder().include("disparse.testing.jda.*"))
                        .setScanners(new MethodAnnotationsScanner())))
            .withHelpBaseEmbed(
                () ->
                    new EmbedBuilder()
                        .setColor(Color.decode("#eb7701"))
                        .setFooter("cool canary bot"))
            .description("A canary testing bot for disparse!");

    JDA jda = Dispatcher.init(new JDABuilder(), builder.build()).setToken(token).build();

    jda.awaitReady();
  }
}
