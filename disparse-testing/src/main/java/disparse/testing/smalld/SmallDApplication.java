package disparse.testing.smalld;

import com.github.princesslana.smalld.SmallD;
import disparse.discord.smalld.Dispatcher;
import disparse.testing.DisparseRunner;
import java.util.concurrent.Executors;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class SmallDApplication implements DisparseRunner {
  @Override
  public void start(String token) throws Exception {
    Dispatcher.Builder builder =
        new Dispatcher.Builder(SmallDApplication.class)
            .prefix("&")
            .pageLimit(10)
            .withReflections(
                new Reflections(
                    new ConfigurationBuilder()
                        .setUrls(
                            ClasspathHelper.forPackage(
                                SmallDApplication.class.getPackage().getName()))
                        .filterInputsBy(new FilterBuilder().include("disparse.testing.smalld.*"))
                        .setScanners(new MethodAnnotationsScanner())))
            .withExecutorService(Executors.newFixedThreadPool(10))
            .disallowIncomingBotMessages()
            .description("A canary testing bot for disparse!");

    try (SmallD smalld = SmallD.create(token)) {
      builder.withSmalldClient(smalld);
      Dispatcher.init(builder.build());
      smalld.run();
    }
  }
}
