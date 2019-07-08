package disparse.parser;

import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequiresValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private static Parser testParser;
    private static Flag flagOne = new Flag("create", 'c', Types.LIST);
    private static Flag flagTwo = new Flag("update", 'u', Types.BOOL);
    private static Flag flagThree = new Flag("read-only", 'r', Types.BOOL);
    private static Flag flagFour = new Flag("delete", 'd', Types.BOOL);
    private static Flag flagFive = new Flag("user", ' ', Types.STR);
    private static Flag flagSix = new Flag("minutes", 'm', Types.INT);
    private static Flag flagSeven = new Flag("random", ' ', Types.LIST);

    @BeforeAll
    static void initializeTestParser() {

        Map<String, List<Flag>> commandToFlags  = Map.of(
                "init", List.of(flagOne, flagTwo),
                "make", List.of(flagThree, flagFour),
                "sync", List.of(flagFive, flagSix),
                "log", List.of(flagSeven, flagOne),
                "up", List.of(flagTwo, flagThree),
                "histedit", List.of(flagFour, flagFive)
        );
        testParser = new Parser(commandToFlags);
    }

    @Test
    void testEmptyArgumentsIsEmpty() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("log")));
        assertEquals(output.getOptions().size(), 0);
        assertEquals(output.getArguments().size(), 0);
        assertEquals(output.getCommand(), "log");
    }

    @Test
    void testOneArgument() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("log", "foo")));
        assertEquals(output.getOptions().size(), 0);
        assertEquals(output.getArguments().size(), 1);
        assertEquals(output.getArguments().get(0), "foo");
        assertEquals(output.getCommand(), "log");
    }

    @Test
    void testOneBoolOpt() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("up", "--update")));
        assertEquals(output.getOptions().size(), 1);
        assertEquals(output.getArguments().size(), 0);
        assertEquals(output.getOptions().get(flagTwo), true);
    }

    @Test
    void testOneStrOpt() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("sync", "--user", "disparse")));
        assertEquals(output.getOptions().size(), 1);
        assertEquals(output.getArguments().size(), 0);
        assertEquals(output.getOptions().get(flagFive), "disparse");
    }

    @Test
    void testNoFlagForCommand() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of("init", "--read-only")));
        assertEquals(output.getOptions().size(), 0);
        assertEquals(output.getArguments().size(), 1);
        assertEquals(output.getArguments().get(0), "--read-only");
    }

    @Test
    void testNoCommandFound() {
        assertThrows(NoCommandNameFound.class, () -> {
            ParsedOutput output = testParser.parse(new ArrayList<>(List.of("--update")));
        });
    }

    @Test
    void testNoValueProvidedForStrOpt() {
        assertThrows(OptionRequiresValue.class, () -> {
            ParsedOutput output = testParser.parse(new ArrayList<>(List.of("sync", "--user")));
        });
    }

    @Test
    void testListOptMultipleValues() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of(
                "init",
                "--create",
                "foo",
                "--create",
                "bar",
                "--create",
                "baz")));
        assertEquals(output.getOptions().size(), 1);
        assertEquals(output.getArguments().size(), 0);
        assertEquals(output.getOptions().get(flagOne), List.of("foo", "bar", "baz"));
    }

    @Test
    void testListOptWithShortAndLongNames() {
        ParsedOutput output = testParser.parse(new ArrayList<>(List.of(
                "init",
                "-c",
                "foo",
                "--create",
                "bar",
                "-c",
                "baz")));
        assertEquals(output.getOptions().size(), 1);
        assertEquals(output.getArguments().size(), 0);
        assertTrue(((List<String>)output.getOptions().get(flagOne)).containsAll(List.of("foo", "bar", "baz")));
    }
}
