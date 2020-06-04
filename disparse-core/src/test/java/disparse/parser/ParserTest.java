package disparse.parser;

import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequiresValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("CanBeFinal")
public class ParserTest {

    private static Parser testParser;
    private static CommandFlag flagOne = new CommandFlag("create", 'c', Types.STR_LIST, false, "", Map.of());
    private static CommandFlag flagTwo = new CommandFlag("update", 'u', Types.BOOL, false, "", Map.of());
    private static CommandFlag flagThree = new CommandFlag("read-only", 'r', Types.BOOL, false, "", Map.of());
    private static CommandFlag flagFour = new CommandFlag("delete", 'd', Types.BOOL, false, "", Map.of());
    private static CommandFlag flagFive = new CommandFlag("user", ' ', Types.STR, false, "", Map.of());
    private static CommandFlag flagSix = new CommandFlag("minutes", 'm', Types.INT, false, "", Map.of());
    private static CommandFlag flagSeven = new CommandFlag("random", ' ', Types.STR_LIST, false, "", Map.of());

    @BeforeAll
    static void initializeTestParser() {

        Map<Command, List<CommandFlag>> commandToFlags =
                Map.of(
                        new Command("init", ""), List.of(flagOne, flagTwo),
                        new Command("make", ""), List.of(flagThree, flagFour),
                        new Command("sync", ""), List.of(flagFive, flagSix),
                        new Command("log", ""), List.of(flagSeven, flagOne),
                        new Command("up", ""), List.of(flagTwo, flagThree),
                        new Command("histedit", ""), List.of(flagFour, flagFive));
        testParser = new Parser(commandToFlags);
    }

    @Test
    void testEmptyArgumentsIsEmpty() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("log")));
        Assertions.assertEquals(output.getOptions().size(), 0);
        Assertions.assertEquals(output.getArguments().size(), 0);
        // assertEquals(output.getCommand(), "log");
    }

    @Test
    void testOneArgument() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("log", "foo")));
        Assertions.assertEquals(output.getOptions().size(), 0);
        Assertions.assertEquals(output.getArguments().size(), 1);
        Assertions.assertEquals(output.getArguments().get(0), "foo");
        // assertEquals(output.getCommand(), "log");
    }

    @Test
    void testOneBoolOpt() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("up", "--update")));
        Assertions.assertEquals(output.getOptions().size(), 1);
        Assertions.assertEquals(output.getArguments().size(), 0);
        Assertions.assertEquals(output.getOptions().get(flagTwo), true);
    }

    @Test
    void testOneStrOpt() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("sync", "--user", "disparse")));
        Assertions.assertEquals(output.getOptions().size(), 1);
        Assertions.assertEquals(output.getArguments().size(), 0);
        Assertions.assertEquals(output.getOptions().get(flagFive), "disparse");
    }

    @Test
    void testNoFlagForCommand() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("init", "--read-only")));
        Assertions.assertEquals(output.getOptions().size(), 0);
        Assertions.assertEquals(output.getArguments().size(), 1);
        Assertions.assertEquals(output.getArguments().get(0), "--read-only");
    }

    @Test
    void testNoCommandFound() {
        assertThrows(
                NoCommandNameFound.class,
                () -> {
                    ParsedOutput output = testParser.parse(new ArrayList<>(List.of("--update")));
                });
    }

    @Test
    void testNoValueProvidedForStrOpt() {
        assertThrows(
                OptionRequiresValue.class,
                () -> {
                    ParsedOutput output = testParser.parse(new ArrayList<>(List.of("sync", "--user")));
                });
    }

    @Test
    void testListOptMultipleValues() {
        ParsedOutput output =
                testParser.parse(
                        new ArrayList<>(
                                List.of("init", "--create", "foo", "--create", "bar", "--create", "baz")));
        Assertions.assertEquals(output.getOptions().size(), 1);
        Assertions.assertEquals(output.getArguments().size(), 0);
        Assertions.assertEquals(output.getOptions().get(flagOne), List.of("foo", "bar", "baz"));
    }

    @Test
    void testListOptWithShortAndLongNames() {
        ParsedOutput output =
                testParser.parse(
                        new ArrayList<>(List.of("init", "-c", "foo", "--create", "bar", "-c", "baz")));
        Assertions.assertEquals(output.getOptions().size(), 1);
        Assertions.assertEquals(output.getArguments().size(), 0);
        assertTrue(
                ((List<String>) output.getOptions().get(flagOne))
                        .containsAll(List.of("foo", "bar", "baz")));
    }
}
