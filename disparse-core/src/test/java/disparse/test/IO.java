package disparse.test;

import disparse.discord.TestDispatcher;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;

public class IO {
  private String input;
  private String[] lines;

  private IO(final String input) {
    this.input = input;
  }

  public static IO given(String input) {
    return new IO(input);
  }

  public IO expect(String... lines) {
    this.lines = lines;
    return this;
  }

  public void execute(TestDispatcher dispatcher) {
    new IORunner(input, Arrays.asList(lines), dispatcher).run();
  }

  static class IORunner {
    private final String given;
    private final List<String> expect;
    private final TestDispatcher dispatcher;

    IORunner(final String given, final List<String> expect, final TestDispatcher dispatcher) {
      this.given = given;
      this.expect = expect;
      this.dispatcher = dispatcher;
    }

    void run() {
      dispatcher.dispatch(given);
      Assertions.assertEquals(expect, dispatcher.getMessages());
    }
  }
}
