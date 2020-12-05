package disparse.discord;

import static disparse.test.Dispatch.require;
import static disparse.test.io.IO.given;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.reflection.*;
import disparse.utils.help.Help;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispatchIntegrationTests {

  private static final String PREFIX = "!";
  private static final String DESCRIPTION = "test description";
  private static final int PAGE_LIMIT = 6;

  private TestDispatcher globalDispatcher;

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
  @Cooldown(amount = 50, unit = ChronoUnit.MILLIS, messageStrategy = MessageStrategy.MESSAGE)
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

  @CommandHandler(commandName = "required")
  public static TestDiscordResponse required(RequiredOpts opts) {
    return TestDiscordResponse.noop();
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
    this.globalDispatcher =
        new TestDispatcher.Builder(this.getClass())
            .prefix(PREFIX)
            .pageLimit(PAGE_LIMIT)
            .description(DESCRIPTION)
            .build();
  }

  @Test
  public void testSingleCommandIsSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!test").expect("test").execute(dispatcher);
  }

  @Test
  public void testSingleCommandAndArgumentsAreSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!test a b c").expect("test", "a", "b", "c").execute(dispatcher);
  }

  @Test
  public void testSingleSubcommandIsSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo bar").expect("test").execute(dispatcher);
  }

  @Test
  public void testSingleSubcommandAndArgumentsAreSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo bar a b c").expect("test", "a", "b", "c").execute(dispatcher);
  }

  @Test
  public void testCommandWithOneBooleanFlagDefaultIsSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo").expect("test", "false").execute(dispatcher);
  }

  @Test
  public void testCommandWithOneBooleanShortFlagIsSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo -t").expect("test", "true").execute(dispatcher);
  }

  @Test
  public void testCommandWithOneBooleanLongFlagIsSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo --toggle").expect("test", "true").execute(dispatcher);
  }

  @Test
  public void testSubcommandWithBooleanFlagAndArgumentsAreSent() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!foo bar baz a b --toggle c").expect("test", "a", "b", "c", "true").execute(dispatcher);
  }

  @Test
  public void testNoCommandFound() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!noCommand")
        .expect(
            "`noCommand` is not a valid command!",
            "Use !help to get a list of all available commands.")
        .execute(dispatcher);
  }

  @Test
  public void testCommandWithSimpleUserCooldown() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!cooldown")
        .thenGiven("!cooldown")
        .thenWait(50, ChronoUnit.MILLIS)
        .thenGiven("!cooldown")
        .expect("test", "This command has a per-user cooldown!", "test")
        .execute(dispatcher);
  }

  @Test
  public void testCommandReturnDiscordResponseStringVariant() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!discordresponse").expect("sent").execute(dispatcher);
  }

  @Test
  public void testCommandReturnDiscordResponseEmbedVariant() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!discordresponseembed").expect("sent").execute(dispatcher);
  }

  @Test
  public void testCommandReturnDiscordResponseNoopVariant() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!discordresponsenoop").expect().execute(dispatcher);
  }

  @Test
  public void testFlagRequiresOption() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    Command requiredCommand = new Command("required", "");
    CommandFlag requiredFlag = new CommandFlag("required", null, null, true, null, null);

    given("!required")
        .expect(Help.optionRequired(requiredCommand, requiredFlag))
        .execute(dispatcher);
  }

  @Test
  public void testFlagRequiresValue() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    CommandFlag requiredFlag = new CommandFlag("required", null, null, true, null, null);

    given("!required --required")
        .expect(Help.optionRequiresValue(requiredFlag))
        .execute(dispatcher);
  }

  @Test
  public void testHelpAllCommands() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();

    String tail = "no description available|false";

    given("!help")
        .expect(
            String.join("|", "title", "All Commands"),
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
                "false"))
        .execute(dispatcher);
  }

  @Test
  public void testHelpAllCommandsPageTwo() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    String tail = "no description available|false";
    given("!help --page 2")
        .expect(
            String.join("|", "title", "All Commands"),
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
                "false"))
        .execute(dispatcher);
  }

  @Test
  public void testHelpAllCommandsPageTooHigh() {
    String pageNum = "15";
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!help --page " + pageNum)
        .expect(
            "The specified page number **"
                + pageNum
                + "** is not within the range of valid pages.  The valid pages are between **1** and **2**.")
        .execute(dispatcher);
  }

  @Test
  public void testHelpAllCommandsPageTooLow() {
    String pageNum = "-15";
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!help --page " + pageNum)
        .expect(
            "The specified page number **"
                + pageNum
                + "** is not within the range of valid pages.  The valid pages are between **1** and **2**.")
        .execute(dispatcher);
  }

  @Test
  public void testRepeatableFlag() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!allopts --repeat foo --repeat bar --repeat baz")
        .expect("foo", "bar", "baz")
        .execute(dispatcher);
  }

  @Test
  public void testRepeatableFlagRequiresOption() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!allopts --repeat foo --repeat")
        .expect("The flag `--repeat` was not provided a value!")
        .execute(dispatcher);
  }

  @Test
  public void testRepeatableFlagMixedWithShortAndLong() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!allopts -r foo --repeat bar -r baz").expect("foo", "bar", "baz").execute(dispatcher);
  }

  @Test
  public void testStandaloneHyphenDoesNotCrash() {
    TestDispatcher dispatcher = require(DispatchIntegrationTests.class).build();
    given("!allopts - foo --print-args").expect("-", "foo").execute(dispatcher);
  }

  @ParsedEntity
  static class FooOpts {
    @Flag(shortName = 't', longName = "toggle")
    Boolean toggle = false;
  }

  @ParsedEntity
  static class RequiredOpts {
    @Flag(shortName = 'r', longName = "required", required = true)
    String required;
  }

  @ParsedEntity
  static class AllOpts {
    @Flag(shortName = 'r', longName = "repeat")
    List<String> repeatable = new ArrayList<>();

    @Flag(shortName = 'P', longName = "print-args")
    Boolean printArgs = false;
  }
}
