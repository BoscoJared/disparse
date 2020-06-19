package disparse.test;

import disparse.discord.TestDispatcher;
import org.junit.jupiter.api.Assertions;

public class IO {
  private String input;
  private String expect;

  private IO(final String input) {
    this.input = input;
  }

  public static IO given(String input) {
    return new IO(input);
  }

  public IO expect(String expect) {
    this.expect = expect;
    return this;
  }

  public void execute(TestDispatcher dispatcher) {
    new IORunner(input, expect, dispatcher).run();
  }

  static class IORunner {
    private final String given;
    private final String expect;
    private final TestDispatcher dispatcher;

    IORunner(final String given, final String expect, final TestDispatcher dispatcher) {
      this.given = given;
      this.expect = expect;
      this.dispatcher = dispatcher;
    }

    void run() {
      dispatcher.dispatch(given);
      String out = String.join("\n", dispatcher.getMessages());
      Assertions.assertEquals(expect, out);
    }
  }
}
