package disparse.test;

import disparse.discord.TestDispatcher;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class Dispatch {
  private final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
  private final List<URL> urls = new ArrayList<>();

  public static Dispatch require(Class<?> clazz) {
    return new Dispatch().and(clazz);
  }

  public Dispatch and(Class<?> clazz) {
    this.urls.add(ClasspathHelper.forClass(clazz));
    return this;
  }

  public TestDispatcher build() {
    return this.build(new TestDispatcher.Builder(this.getClass()));
  }

  public TestDispatcher build(TestDispatcher.Builder builder) {
    return builder
        .withReflections(
            new Reflections(
                this.configurationBuilder
                    .setUrls(this.urls)
                    .setScanners(new MethodAnnotationsScanner())))
        .build();
  }
}
