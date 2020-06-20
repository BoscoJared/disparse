package disparse.test.io;

import disparse.discord.TestDispatcher;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;

public class IO implements ExecuteIOStep, SetupIOStep {
  private List<Consumer<TestDispatcher>> inputs = new ArrayList<>();
  private String[] lines;

  private IO(final String input) {
    this.inputs.add(new DispatchStringAction(input));
  }

  public static SetupIOStep given(String input) {
    return new IO(input);
  }

  public SetupIOStep thenGiven(String input) {
    this.inputs.add(new DispatchStringAction(input));
    return this;
  }

  public SetupIOStep thenWait(long amount) {
    this.inputs.add(new DispatchWaitAction(amount));
    return this;
  }

  public SetupIOStep thenWait(long amount, ChronoUnit unit) {
    this.inputs.add(new DispatchWaitAction(amount, unit));
    return this;
  }

  @Override
  public ExecuteIOStep expect(String... lines) {
    this.lines = lines;
    return this;
  }

  @Override
  public void execute(TestDispatcher dispatcher) {
    new IORunner(inputs, Arrays.asList(lines), dispatcher).run();
  }

  static class IORunner {
    private final List<Consumer<TestDispatcher>> inputs;
    private final List<String> expect;
    private final TestDispatcher dispatcher;

    IORunner(
        final List<Consumer<TestDispatcher>> inputs,
        final List<String> expect,
        final TestDispatcher dispatcher) {
      this.inputs = inputs;
      this.expect = expect;
      this.dispatcher = dispatcher;
    }

    void run() {
      this.inputs.forEach(action -> action.accept(dispatcher));
      Assertions.assertEquals(expect, dispatcher.getMessages());
    }
  }

  private static class DispatchStringAction implements Consumer<TestDispatcher> {
    private final String given;

    public DispatchStringAction(String given) {
      this.given = given;
    }

    @Override
    public void accept(TestDispatcher testDispatcher) {
      testDispatcher.dispatch(this.given);
    }
  }

  private static class DispatchWaitAction implements Consumer<TestDispatcher> {
    private final ChronoUnit unit;
    private final long amount;

    public DispatchWaitAction(long amount) {
      this(amount, ChronoUnit.SECONDS);
    }

    public DispatchWaitAction(long amount, ChronoUnit unit) {
      this.amount = amount;
      this.unit = unit;
    }

    @Override
    public void accept(TestDispatcher testDispatcher) {
      try {
        TimeUnit.of(this.unit).sleep(this.amount);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
