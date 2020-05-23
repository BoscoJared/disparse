package disparse.discord.jda;

import disparse.discord.Helpable;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.help.Help;
import disparse.utils.Shlex;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dispatcher extends ListenerAdapter implements Helpable<MessageReceivedEvent> {

  private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

  private String prefix;
  private int pageLimit;

  public Dispatcher(String prefix) {
    this(prefix, 5);
  }

  public Dispatcher(String prefix, int pageLimit) {
    this.prefix = prefix;
    this.pageLimit = pageLimit;
  }

  public static JDABuilder init(JDABuilder builder, String prefix) {
    return init(builder, prefix, 5);
  }

  public static JDABuilder init(JDABuilder builder, String prefix, int pageLimit) {
    Detector.detect();
    Dispatcher dispatcher = new Dispatcher(prefix, pageLimit);
    builder.addEventListeners(dispatcher);
    return builder;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    String raw = event.getMessage().getContentRaw();
    if (!raw.startsWith(this.prefix)) {
      return;
    }

    String cleanedMessage = raw.substring(this.prefix.length());

    if (cleanedMessage.isEmpty()) {
      logger.info("After removing the prefix, the message was empty.  Not continuing.");
      return;
    }

    List<String> args = Shlex.shlex(cleanedMessage);
    CommandRegistrar.REGISTRAR.dispatch(args, this, event);
  }

  @Override
  public void commandNotFound(MessageReceivedEvent event, String userInput) {
    Help.commandNotFound(userInput).forEach(line -> event.getChannel().sendMessage(line).queue());
  }

  @Override
  public void roleNotMet(MessageReceivedEvent event, Command command) {
    event.getChannel()
        .sendMessage(Help.roleNotMet(command))
        .queue();
  }

  @Override
  public void optionRequired(MessageReceivedEvent event, String message) {
    event.getChannel().sendMessage(message).queue();
  }

  @Override
  public void help(MessageReceivedEvent event, Command command, Collection<CommandFlag> flags,
      Collection<Command> commands, int pageNumber) {
    if (this.commandRolesNotMet(event, command)) {
      return;
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle(Help.getTitle(command))
        .setDescription(Help.getDescriptionUsage(command));

    List<Command> subcommands = Help.findSubcommands(command, commands);
    String currentlyViewing;
    try {
      PaginatedEntities paginatedEntities = Help.paginate(subcommands, flags, pageNumber, pageLimit);
      subcommands = paginatedEntities.getCommands();
      flags = paginatedEntities.getFlags();
      currentlyViewing = paginatedEntities.getCurrentlyViewing();
    } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
      event.getChannel().sendMessage(pageNumberOutOfBounds.getMessage()).queue();
      return;
    }

    List<CommandFlag> sortedFlags = Help.sortFlags(flags);

    if (subcommands.size() > 0) {
      builder.addField("SUBCOMMANDS", "---------------------", false);
    }

    addCommandsToEmbed(builder, subcommands, event);

    if (sortedFlags.size() > 0) {
      builder.addField("FLAGS", "--------", true);
    }

    for (CommandFlag flag : sortedFlags) {
      String flagName = Help.flagToUserFriendlyString(flag);
      builder.addField(flagName, flag.getDescription(), false);
    }

    builder.addField(currentlyViewing, "Use --page to specify a page number", false);
    event.getChannel().sendMessage(builder.build()).queue();
  }

  @Override
  public void helpSubcommands(MessageReceivedEvent event, String foundPrefix, Collection<Command> commands) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle(foundPrefix + " | Subcommands").setDescription("All registered subcommands for " + foundPrefix);

    List<Command> sortedCommands = commands.stream().sorted(Comparator
            .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
            .filter((Command cmd) -> !this.commandRolesNotMet(event, cmd))
            .collect(Collectors.toList());

    addCommandsToEmbed(builder, sortedCommands, event);

    event.getChannel().sendMessage(builder.build()).queue();
  }

  @Override
  public void allCommands(MessageReceivedEvent event, Collection<Command> commands, int pageNumber) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle("All Commands").setDescription("All registered commands");

    String currentlyViewing;
    try {
      PaginatedEntities paginatedEntities = Help.paginate(commands, List.of(), pageNumber, pageLimit);
      commands = paginatedEntities.getCommands();
      currentlyViewing = paginatedEntities.getCurrentlyViewing();
    } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
      event.getChannel().sendMessage(pageNumberOutOfBounds.getMessage()).queue();
      return;
    }

    List<Command> sortedCommands = Help.sortCommands(commands);

    addCommandsToEmbed(builder, sortedCommands, event);
    builder.addField(currentlyViewing, "Use --page to specify a page number", false);

    event.getChannel().sendMessage(builder.build()).queue();
  }

  @Override
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setPageLimit(int pageLimit) {
    this.pageLimit = pageLimit;
  }

  public int getPageLimit() {
    return this.pageLimit;
  }

  private void addCommandsToEmbed(EmbedBuilder builder, List<Command> commands,
      MessageReceivedEvent event) {
    for (Command command : commands) {
      if (this.commandRolesNotMet(event, command)) {
        continue;
      }
      builder.addField(command.getCommandName(), command.getDescription(), false);
    }
  }

  @Override
  public boolean commandRolesNotMet(MessageReceivedEvent event, Command command) {
    if (command.getRoles().length == 0) {
      return false;
    }
    Member member = event.getMember();
    if (member != null) {
      for (String commandRole : command.getRoles()) {
        if (commandRole.equalsIgnoreCase("owner") && event.getMember().isOwner()) {
          return false;
        }
        for (Role role : member.getRoles()) {
          if (role.getName().equalsIgnoreCase(commandRole)) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
