package disparse.utils.readme;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.CommandUsage;
import disparse.utils.help.Help;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadmeGeneration {
  private static final String README_EXTENSION = ".md";
  private static final Logger logger = LoggerFactory.getLogger(ReadmeGeneration.class);

  public static void writeReadme(
      String path,
      String filename,
      Collection<Command> commands,
      Map<Command, Set<CommandFlag>> commandToFlags) {
    filename = appendExtensionIfAbsent(filename);
    Path readmePath = Paths.get(path, filename);
    try {
      Files.writeString(readmePath, generateReadme(commands, commandToFlags));
    } catch (IOException ioException) {
      logger.warn("Readme file could not be written to disk automatically!");
      logger.warn("This will not fail the program, but the file will not be up-to-date!");
      logger.warn("Root cause: {}", ioException.getMessage());
    }
  }

  private static String generateReadme(
      Collection<Command> commands, Map<Command, Set<CommandFlag>> commandToFlags) {
    StringBuilder sb = new StringBuilder();
    sb.append("# Commands").append("\n");

    for (Command command : commands) {
      sb.append("## ").append(command.getCommandName()).append("\n\n");
      sb.append(command.getDescription()).append("\n");
      String cooldown = "N/A";
      if (!command.getCooldownDuration().isZero()) {
        cooldown = command.getCooldownDuration().getSeconds() + " seconds";
        switch (command.getScope()) {
          case USER:
            cooldown = cooldown + " per user";
            break;
          case CHANNEL:
            cooldown = cooldown + " per channel";
            break;
          case GUILD:
            cooldown = cooldown + " per guild";
            break;
        }
      }
      sb.append("* ").append("**Cooldown:** ").append(cooldown).append("\n");
      String perms;
      if (command.getPerms().length == 0) {
        perms = "N/A";
      } else {
        perms =
            Arrays.stream(command.getPerms())
                .map(Objects::toString)
                .sorted()
                .collect(Collectors.joining(", "));
      }
      sb.append("* ").append("**Required Permission:** ").append(perms).append("\n");
      String roles;
      if (command.getRoles().length == 0) {
        roles = "N/A";
      } else {
        roles = Arrays.stream(command.getRoles()).sorted().collect(Collectors.joining(", "));
      }
      sb.append("* ").append("**Required Role:** ").append(roles).append("\n");
      sb.append("#### Arguments\n");

      Collection<CommandFlag> flags =
          Help.sortFlags(commandToFlags.getOrDefault(command, new HashSet<>()));

      for (CommandFlag commandFlag : flags) {
        sb.append("* `-")
            .append(commandFlag.getShortName())
            .append("`, `--")
            .append(commandFlag.getLongName())
            .append("` : ")
            .append(commandFlag.getDescription())
            .append("\n");
      }

      sb.append("\n").append("#### Usage\n");

      Collection<CommandUsage> usages =
          command.getUsageExamples().stream()
              .sorted(Comparator.comparingInt(u -> u.getUsage().length()))
              .collect(Collectors.toList());

      for (CommandUsage usage : usages) {
        sb.append("* ")
            .append(usage.getUsage())
            .append(" -> ")
            .append(usage.getDescription())
            .append("\n");
      }
    }
    return sb.toString();
  }

  private static String appendExtensionIfAbsent(String filename) {
    if (!filename.endsWith(README_EXTENSION)) {
      filename = filename + README_EXTENSION;
    }
    return filename;
  }
}
