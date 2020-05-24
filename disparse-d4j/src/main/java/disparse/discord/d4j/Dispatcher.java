package disparse.discord.d4j;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;
import disparse.discord.Helpable;
import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.reflection.Detector;
import disparse.utils.Shlex;
import disparse.utils.help.Help;
import disparse.utils.help.PageNumberOutOfBounds;
import disparse.utils.help.PaginatedEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Dispatcher implements Helpable<MessageCreateEvent> {

    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private String prefix;
    private int pageLimit;

    public Dispatcher(String prefix) { this(prefix, 5); }

    public Dispatcher(String prefix, int pageLimit) {
        this.prefix = prefix;
        this.pageLimit = pageLimit;
    }

    public static void init(GatewayDiscordClient gateway, String prefix) {
        init(gateway, prefix, 5);
    }

    public static void init(GatewayDiscordClient gateway, String prefix, int pageLimit) {
        Detector.detect();
        Dispatcher dispatcher = new Dispatcher(prefix, pageLimit);
        gateway.on(MessageCreateEvent.class).subscribe(dispatcher::onMessageReceived);
    }

    public void onMessageReceived(MessageCreateEvent event) {
        if (event.getMessage().getAuthor().isEmpty()) return;
        if (event.getMessage().getAuthor().get().isBot()) return;

        String raw = event.getMessage().getContent();
        if (!raw.startsWith(this.prefix)) return;

        String cleanedMessage = raw.substring(this.prefix.length());

        if (cleanedMessage.isEmpty()) {
            logger.info("After removing the prefix, the message was empty.  Not continuing.");
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.REGISTRAR.dispatch(args, this, event);
    }

    @Override
    public void help(MessageCreateEvent event, Command command, Collection<CommandFlag> flags, Collection<Command> commands, int pageNumber) {
        if (this.commandRolesNotMet(event, command)) return;

        EmbedCreateSpec builder = new EmbedCreateSpec();
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
            event.getMessage().getChannel().block().createMessage(pageNumberOutOfBounds.getMessage()).block();
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

        sendEmbed(event, builder);
    }

    @Override
    public void allCommands(MessageCreateEvent event, Collection<Command> commands, int pageNumber) {
        EmbedCreateSpec builder = new EmbedCreateSpec();
        builder.setTitle("All Commands").setDescription("All registered commands");

        String currentlyViewing;
        try {
            PaginatedEntities paginatedEntities = Help.paginate(commands, List.of(), pageNumber, pageLimit);
            commands = paginatedEntities.getCommands();
            currentlyViewing = paginatedEntities.getCurrentlyViewing();
        } catch (PageNumberOutOfBounds pageNumberOutOfBounds) {
            event.getMessage().getChannel().block().createMessage(pageNumberOutOfBounds.getMessage()).block();
            return;
        }

        List<Command> sortedCommands = Help.sortCommands(commands);

        addCommandsToEmbed(builder, sortedCommands, event);
        builder.addField(currentlyViewing, "Use --page to specify a page number", false);

        sendEmbed(event, builder);
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void commandNotFound(MessageCreateEvent event, String userInput) {
        Help.commandNotFound(userInput, this.prefix)
                .forEach(line -> event.getMessage().getChannel().block().createMessage(line).block());
    }

    @Override
    public void helpSubcommands(MessageCreateEvent event, String foundPrefix, Collection<Command> commands) {
        EmbedCreateSpec builder = new EmbedCreateSpec();
        builder.setTitle(foundPrefix + " | Subcommands")
                .setDescription("All registered subcommands for " + foundPrefix);

        List<Command> sortedCommands = commands.stream().sorted(Comparator
                .comparing((Command cmd) -> cmd.getCommandName().toLowerCase(), Comparator.naturalOrder()))
                .filter((Command cmd) -> !this.commandRolesNotMet(event, cmd))
                .collect(Collectors.toList());

        addCommandsToEmbed(builder, sortedCommands, event);

        sendEmbed(event, builder);
    }

    @Override
    public void roleNotMet(MessageCreateEvent event, Command command) {
        event.getMessage()
                .getChannel()
                .block()
                .createMessage(Help.roleNotMet(command))
                .block();
    }

    @Override
    public void optionRequired(MessageCreateEvent event, String message) {
        event.getMessage().getChannel().block().createMessage(message).block();
    }

    @Override
    public void incorrectOption(MessageCreateEvent event, String message) {
        event.getMessage().getChannel().block().createMessage(message).block();
    }

    @Override
    public boolean commandRolesNotMet(MessageCreateEvent event, Command command) {
        if (command.getRoles().length == 0) {
            return false;
        }
        Member member = event.getMember().orElse(null);
        if (member != null) {
            for (String commandRole : command.getRoles()) {
                if (commandRole.equalsIgnoreCase("owner") &&
                        event.getGuild().block().getOwnerId().equals(member.getId())) {
                    return false;
                }
                for (Role role : member.getRoles().toIterable()) {
                    if (role.getName().equalsIgnoreCase(commandRole)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void addCommandsToEmbed(EmbedCreateSpec builder, List<Command> commands, MessageCreateEvent event) {

        for (Command command : commands) {
            if (this.commandRolesNotMet(event, command)) {
                continue;
            }
            builder.addField(command.getCommandName(), command.getDescription(), false);
        }
    }

    private static void sendEmbed(MessageCreateEvent event, EmbedCreateSpec builder) {
        event.getMessage()
                .getChannel()
                .block()
                .createMessage(messageSpec -> {
                    messageSpec.setEmbed(embedSpec -> {
                        embedSpec.setTitle(builder.asRequest().title().toOptional().orElse(""));
                        embedSpec.setDescription(builder.asRequest().description().toOptional().orElse(""));

                        builder.asRequest().fields().toOptional().orElse(new ArrayList<>()).forEach(f -> {
                            embedSpec.addField(f.name(), f.value(), f.inline().toOptional().orElse(false));
                        });
                    });
                }).block();
    }
}
