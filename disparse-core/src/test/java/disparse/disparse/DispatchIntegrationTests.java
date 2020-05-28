package disparse.disparse;

import disparse.discord.Helpable;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Detector;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import org.junit.jupiter.api.Assertions;

public class DispatchIntegrationTests {

    private static String PREFIX = "!";
    private static String DESCRIPTION = "test description";
    private static int PAGE_LIMIT = 5;

    private TestDispatcher dispatcher;

    @CommandHandler(commandName = "test")
    public static void test(Helpable<Object, Object> helper, List<String> args) {
        helper.sendMessage(null, "test");
        for (String arg: args) {
            helper.sendMessage(null, arg);
        }
    }

    @CommandHandler(commandName = "foo.bar")
    public static void fooBar(Helpable<Object, Object> helper, List<String> args) {
        helper.sendMessage(null, "test");
        for (String arg: args) {
            helper.sendMessage(null, arg);
        }
    }

    @ParsedEntity
    static class FooOpts {
        @Flag(shortName = 't', longName = "toggle")
        Boolean toggle = false;
    }

    @CommandHandler(commandName = "foo")
    public static void foo(Helpable<Object, Object> helper, FooOpts opts) {
        helper.sendMessage(null, "test");
        helper.sendMessage(null, opts.toggle.toString());
    }

    @CommandHandler(commandName = "foo.bar.baz")
    public static void fooBarBaz(Helpable<Object, Object> helper, FooOpts opts, List<String> args) {
        helper.sendMessage(null, "test");
        for (String arg: args) {
            helper.sendMessage(null, arg);
        }
        helper.sendMessage(null, opts.toggle.toString());
    }

    @BeforeAll
    public static void beforeAll() {
        Detector.detect();
    }

    @BeforeEach
    public void beforeEach() {
        this.dispatcher = new TestDispatcher.Builder()
                .prefix(PREFIX)
                .pageLimit(PAGE_LIMIT)
                .description(DESCRIPTION)
                .build();
    }

    @Test
    public void testSingleCommandIsSent() {
        dispatcher.dispatch("!test");

        assert(dispatcher.messages.size() == 1);
        assert(dispatcher.messages.get(0).equals("test"));
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

        assert(dispatcher.messages.size() == 1);
        assert(dispatcher.messages.get(0).equals("test"));
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

        List<String> inOrderMessages = List.of("`noCommand` is not a valid command!",
                "Use !help to get a list of all available commands.");

        Assertions.assertLinesMatch(inOrderMessages, dispatcher.messages);
    }
}
