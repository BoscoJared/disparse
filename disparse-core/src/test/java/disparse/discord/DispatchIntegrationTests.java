package disparse.discord;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.reflection.*;
import disparse.test.Dispatch;
import disparse.test.IO;
import disparse.utils.help.Help;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispatchIntegrationTests {

  private static String PREFIX = "!";
  private static String DESCRIPTION = "test description";
  private static int PAGE_LIMIT = 6;

  private TestDispatcher dispatcher;

  @CommandHandler(commandName = "test")
  public static void test(TestDiscordRequest req) {
    req.getDispatcher().sendMessage(null, "test");
    for (String arg : req.getArgs()) {
      req.getDispatcher().sendMessage(null, arg);
    }
  }

  @CommandHandler(commandName = "foo.bar")
  public static void fooBar(TestDiscordRequest req) {
    req.getDispatcher().sendMessage(null, "test");
    for (String arg : req.getArgs()) {
      req.getDispatcher().sendMessage(null, arg);
    }
  }

  @ParsedEntity
  static class FooOpts {
    @Flag(shortName = 't', longName = "toggle")
    Boolean toggle = false;
  }

  @CommandHandler(commandName = "foo")
  public static void foo(TestDiscordRequest req, FooOpts opts) {
    req.getDispatcher().sendMessage(null, "test");
    req.getDispatcher().sendMessage(null, opts.toggle.toString());
  }

  @CommandHandler(commandName = "foo.bar.baz")
  public static void fooBarBaz(TestDiscordRequest req, FooOpts opts) {
    req.getDispatcher().sendMessage(null, "test");
    for (String arg : req.getArgs()) {
      req.getDispatcher().sendMessage(null, arg);
    }
    req.getDispatcher().sendMessage(null, opts.toggle.toString());
  }

  @CommandHandler(commandName = "cooldown")
  @Cooldown(amount = 50, unit = ChronoUnit.MILLIS, sendCooldownMessage = true)
  public static void cooldown(TestDiscordRequest req) {
    req.getDispatcher().sendMessage(null, "test");
  }

  @CommandHandler(commandName = "discordresponse")
  public static TestDiscordResponse discordResponse() {
    return TestDiscordResponse.of("sent");
  }

  @CommandHandler(commandName = "discordresponseembed")
  public static TestDiscordResponse discordResponseEmbed() {
    return TestDiscordResponse.of(new StringBuilder().append("sent"));
  }

  @CommandHandler(commandName = "discordresponsenoop")
  public static TestDiscordResponse discordResponseNoop() {
    return TestDiscordResponse.noop();
  }

  @ParsedEntity
  static class RequiredOpts {
    @Flag(shortName = 'r', longName = "required", required = true)
    String required;
  }

  @CommandHandler(commandName = "required")
  public static TestDiscordResponse required(RequiredOpts opts) {
    return TestDiscordResponse.noop();
  }

  @ParsedEntity
  static class AllOpts {
    @Flag(shortName = 'r', longName = "repeat")
    List<String> repeatable = new ArrayList<>();

    @Flag(shortName = 'P', longName = "print-args")
    Boolean printArgs = false;
  }

  @CommandHandler(commandName = "allopts")
  public static TestDiscordResponse required(TestDiscordRequest req, AllOpts opts) {
    if (opts.repeatable.size() > 0) {
      opts.repeatable.forEach(r -> req.getDispatcher().sendMessage(null, r));
    }

    if (opts.printArgs) {
      req.getArgs().forEach(r -> req.getDispatcher().sendMessage(null, r));
    }

    return TestDiscordResponse.noop();
  }

  @BeforeEach
  public void beforeEach() {
    this.dispatcher =
        new TestDispatcher.Builder(this.getClass())
            .prefix(PREFIX)
            .pageLimit(PAGE_LIMIT)
            .description(DESCRIPTION)
            .build();
  }

  @Test
  public void testSingleCommandIsSent() {
    dispatcher.dispatch("!test");

    assert (dispatcher.messages.size() == 1);
    assert (dispatcher.messages.get(0).equals("test"));
  }

  @Test
  public void testSingleCommandAndArgumentsAreSent() {

    dispatcher.dispatch("!test a b c");

    List<String> inOrderMessages = List.of("test", "a", "b", "c");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testSingleSubcommandIsSent() {
    dispatcher.dispatch("!foo bar");

    assert (dispatcher.messages.size() == 1);
    assert (dispatcher.messages.get(0).equals("test"));
  }

  @Test
  public void testSingleSubcommandAndArgumentsAreSent() {
    dispatcher.dispatch("!foo bar a b c");

    List<String> inOrderMessages = List.of("test", "a", "b", "c");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandWithOneBooleanFlagDefaultIsSent() {
    dispatcher.dispatch("!foo");

    List<String> inOrderMessages = List.of("test", "false");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandWithOneBooleanShortFlagIsSent() {
    dispatcher.dispatch("!foo -t");

    List<String> inOrderMessages = List.of("test", "true");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandWithOneBooleanLongFlagIsSent() {
    dispatcher.dispatch("!foo --toggle");

    List<String> inOrderMessages = List.of("test", "true");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testSubcommandWithBooleanFlagAndArgumentsAreSent() {
    dispatcher.dispatch("!foo bar baz a b --toggle c");

    List<String> inOrderMessages = List.of("test", "a", "b", "c", "true");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testNoCommandFound() {
    dispatcher.dispatch("!noCommand");

    List<String> inOrderMessages =
        List.of(
            "`noCommand` is not a valid command!",
            "Use !help to get a list of all available commands.");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandWithSimpleUserCooldown() throws Exception {
    dispatcher.dispatch("!cooldown");
    dispatcher.dispatch("!cooldown");

    TimeUnit.MILLISECONDS.sleep(50);

    dispatcher.dispatch("!cooldown");

    List<String> inOrderMessages = List.of("test", "This command has a per-user cooldown!", "test");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandReturnDiscordResponseStringVariant() {
    dispatcher.dispatch("!discordresponse");

    List<String> inOrderMessages = List.of("sent");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandReturnDiscordResponseEmbedVariant() {
    dispatcher.dispatch("!discordresponseembed");

    List<String> inOrderMessages = List.of("sent");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testCommandReturnDiscordResponseNoopVariant() {
    dispatcher.dispatch("!discordresponsenoop");

    List<String> inOrderMessages = List.of();

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testFlagRequiresOption() {
    dispatcher.dispatch("!required");

    Command requiredCommand = new Command("required", "");
    CommandFlag requiredFlag = new CommandFlag("required", null, null, true, null, null);

    List<String> inOrderMessages = List.of(Help.optionRequired(requiredCommand, requiredFlag));

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testFlagRequiresValue() {
    dispatcher.dispatch("!required --required");
    CommandFlag requiredFlag = new CommandFlag("required", null, null, true, null, null);

    List<String> inOrderMessages = List.of(Help.optionRequiresValue(requiredFlag));
    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testHelpAllCommands() {
    dispatcher.dispatch("!help");

    String tail = "no description available|false";

    List<String> inOrderMessages =
        List.of(
            String.join("|", "title", dispatcher.getDescription(new Object())),
            "description|All registered commands",
            String.join("|", "**allopts**", tail),
            String.join("|", "**cooldown**", tail),
            String.join("|", "**discordresponse**", tail),
            String.join("|", "**discordresponseembed**", tail),
            String.join("|", "**discordresponsenoop**", tail),
            String.join("|", "**foo**", tail),
            String.join(
                "|",
                "Currently viewing page 1 of 2",
                "Use `-p | --page` to specify a page number",
                "false"));

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testHelpAllCommandsPageTwo() {
    dispatcher.dispatch("!help --page 2");

    String tail = "no description available|false";

    List<String> inOrderMessages =
        List.of(
            String.join("|", "title", dispatcher.getDescription(new Object())),
            "description|All registered commands",
            String.join("|", "**foo.bar**", tail),
            String.join("|", "**foo.bar.baz**", tail),
            String.join(
                "|", "**help**", "show all commands or detailed help of one command", "false"),
            String.join("|", "**required**", tail),
            String.join("|", "**test**", tail),
            String.join(
                "|",
                "Currently viewing page 2 of 2",
                "Use `-p | --page` to specify a page number",
                "false"));

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testHelpAllCommandsPageTooHigh() {
    String pageNum = "15";
    dispatcher.dispatch("!help --page " + pageNum);

    List<String> inOrderMessages =
        List.of(
            "The specified page number **"
                + pageNum
                + "** is not within the range of valid pages.  The valid pages are between **1** and **2**.");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testHelpAllCommandsPageTooLow() {
    String pageNum = "-15";
    dispatcher.dispatch("!help --page " + pageNum);

    List<String> inOrderMessages =
        List.of(
            "The specified page number **"
                + pageNum
                + "** is not within the range of valid pages.  The valid pages are between **1** and **2**.");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testRepeatableFlag() {
    dispatcher.dispatch("!allopts --repeat foo --repeat bar --repeat baz");

    List<String> inOrderMessages = List.of("foo", "bar", "baz");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testRepeatableFlagRequiresOption() {
    dispatcher.dispatch("!allopts --repeat foo --repeat");

    List<String> inOrderMessages = List.of("The flag `--repeat` was not provided a value!");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testRepeatableFlagMixedWithShortAndLong() {
    dispatcher.dispatch("!allopts -r foo --repeat bar -r baz");

    List<String> inOrderMessages = List.of("foo", "bar", "baz");

    Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
  }

  @Test
  public void testStandaloneHyphenDoesNotCrash() {
    TestDispatcher custom =
        Dispatch.create(this.getClass()).require(DispatchIntegrationTests.class).build();

    IO.given("!allopts - foo --print-args").expect("-\n" + "foo").execute(custom);
  }
}
