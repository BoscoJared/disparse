package disparse.test.io;

import java.time.temporal.ChronoUnit;

public interface SetupIOStep {
  ExecuteIOStep expect(String... lines);

  SetupIOStep thenGiven(String input);

  SetupIOStep thenWait(long amount);

  SetupIOStep thenWait(long amount, ChronoUnit unit);
}
