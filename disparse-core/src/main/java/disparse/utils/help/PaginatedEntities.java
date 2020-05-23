package disparse.utils.help;

import disparse.parser.Command;
import disparse.parser.CommandFlag;

import java.util.List;

public class PaginatedEntities {
    private List<Command> commands;
    private List<CommandFlag> flags;
    private String currentlyViewing;

    public PaginatedEntities(List<Command> commands, List<CommandFlag> flags, int currPage, int totalPages) {
        this.commands = commands;
        this.flags = flags;
        this.currentlyViewing = String.format("Currently viewing page %d of %d", currPage, totalPages);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public List<CommandFlag> getFlags() {
        return flags;
    }

    public String getCurrentlyViewing() {
        return currentlyViewing;
    }
}
