package disparse.test;

import disparse.discord.TestDispatcher;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Dispatch {
  private final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
  private final List<URL> urls = new ArrayList<>();
  private final Class<?> caller;

  public Dispatch(final Class<?> caller) {
    this.caller = caller;
  }

  public static Dispatch create(Class<?> caller) {
    return new Dispatch(caller);
  }

  public Dispatch require(Class<?> clazz) {
    this.urls.add(ClasspathHelper.forClass(clazz));
    return this;
  }

  public TestDispatcher build() {
    return new TestDispatcher.Builder(this.caller)
            .withReflections(new Reflections(
                    this.configurationBuilder
                            .setUrls(this.urls)
                            .setScanners(new MethodAnnotationsScanner())))
            .build();
  }
}
