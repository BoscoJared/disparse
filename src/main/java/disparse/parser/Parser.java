package disparse.parser;

import disparse.parser.exceptions.NoCommandNameFound;
import disparse.parser.exceptions.OptionRequiresValue;

import java.util.*;
import java.util.stream.Collectors;

public class Parser {
    private Map<String, List<Flag>> commandToFlags = new HashMap<>();
    private Map<String, Flag> longOptionMap = new HashMap<>();
    private Map<Character, Flag> shortOptionMap = new HashMap<>();

    public Parser(final Map<String, List<Flag>> commandToFlags) {
        this.commandToFlags = commandToFlags;
    }

    public ParsedOutput parse(List<String> args) {
        final String commandName = this.findCommand(args);

        this.createShortAndLongOptions(commandName);

        final Map<Flag, Object> longOptions = this.findLongOptions(args);
        final Map<Flag, Object> shortOptions = this.findShortOptions(args);

        final Map<Flag, Object> mergedOptions = new HashMap<>(longOptions);
        shortOptions.forEach((k, v) -> mergedOptions.merge(k, v, Parser::mergeOptions));

        return new ParsedOutput(commandName, args, mergedOptions);
    }

    private String findCommand(List<String> args) {
        List<String> currArgs = new ArrayList<>(args);
        Optional<String> commandName = Optional.empty();
        int skip = 0;
        while (currArgs.size() > 0) {
            String possibleCommand = currArgs.stream().collect(Collectors.joining("."));
            if (this.commandToFlags.containsKey(possibleCommand)) {
                commandName = Optional.of(possibleCommand);
                skip = currArgs.size();
                break;
            } else {
                currArgs.remove(currArgs.size() - 1);
            }
        }
        int i = 0;
        for (Iterator<String> iter = args.iterator(); iter.hasNext();)  {
            if (i < skip) {
                iter.next();
                iter.remove();
                i++;
            } else {
                break;
            }
        }
        return commandName.orElseThrow(() -> new NoCommandNameFound("A valid command was not found!"));
    }

    private void createShortAndLongOptions(String commandName) {
        this.shortOptionMap = new HashMap<>();
        this.longOptionMap = new HashMap<>();
        for (Flag flag : this.commandToFlags.get(commandName)) {
            this.longOptionMap.put(flag.getLongName(), flag);
            this.shortOptionMap.put(flag.getShortName(), flag);
        }
    }

    private Map<Flag, Object> findLongOptions(List<String> args) {
        final Map<Flag, Object> optionMap = new HashMap<>();
        final Iterator<String> iter = args.iterator();
        while (iter.hasNext()) {
            final String currArg = iter.next();
            if (currArg.startsWith("--")) {
                final String currOpt = currArg.replaceFirst("--", "");
                if (this.longOptionMap.containsKey(currOpt)) {
                    Flag currFlag = this.longOptionMap.get(currOpt);
                    iter.remove();
                    accept(optionMap, currFlag, iter);
                }
            }
        }
        return optionMap;
    }

    private Map<Flag, Object> findShortOptions(List<String> args) {
        final Map<Flag, Object> optionMap = new HashMap<>();
        final Iterator<String> iter = args.iterator();
        while (iter.hasNext()) {
            final String currArg = iter.next();
            if (currArg.startsWith("-")) {
                final String currOpt = currArg.replaceFirst("-", "");
                final Character currChar = currOpt.charAt(0);
                if (this.shortOptionMap.containsKey(currChar)) {
                    Flag currFlag = this.shortOptionMap.get(currChar);
                    iter.remove();
                    accept(optionMap, currFlag, iter);
                }
            }
        }
        return optionMap;
    }

    private static void accept(Map<Flag, Object> optionMap, Flag currFlag, Iterator<String> iter) {
        if (currFlag.getType() == Types.BOOL) {
            optionMap.put(currFlag, true);
        } else if (currFlag.getType() == Types.LIST) {
            optionMap.putIfAbsent(currFlag, new ArrayList<String>());
            if (iter.hasNext()) {
                ((ArrayList<String>) optionMap.get(currFlag)).add(iter.next());
                iter.remove();
            } else {
                throw new OptionRequiresValue("Option requires value");
            }
        } else {
            if (iter.hasNext()) {
                optionMap.put(currFlag, iter.next());
                iter.remove();
            } else {
                throw new OptionRequiresValue("Option requires value");
            }
        }
    }

    private static Object mergeOptions(Object obj1, Object obj2) {
        if (obj1 instanceof List) {
            List list = (List) obj1;
            if (obj2 instanceof List) {
                list.addAll((List) obj2);
            } else {
                list.add(obj2);
            }
            return list;
        } else {
            return obj1;
        }
    }
}
