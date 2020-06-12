package disparse.disparse;

import disparse.discord.AbstractDiscordRequest;
import disparse.discord.AbstractDispatcher;
import disparse.parser.Command;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.utils.Shlex;

import java.util.ArrayList;
import java.util.List;

public class TestDispatcher extends AbstractDispatcher<Object, Object> {

    List<String> messages = new ArrayList<>();

    public TestDispatcher() {
        super("!");
    }

    @Override
    public boolean commandRolesNotMet(Object event, Command command) {
        return false;
    }

    @Override
    public boolean commandIntentsNotMet(Object event, Command command) { return false; }

    @Override
    public void sendMessage(Object event, String message) {
        this.messages.add(message);
    }

    @Override
    public void setBuilderTitle(Object builder, String title) {

    }

    @Override
    public void setBuilderDescription(Object builder, String description) {

    }

    @Override
    public void addField(Object builder, String name, String value, boolean inline) {

    }

    @Override
    public Object createBuilder() {
        return new StringBuilder();
    }

    @Override
    public void sendEmbed(Object event, Object builder) {

    }

    @Override
    public String identityFromEvent(Object event) {
        return "USER";
    }

    @Override
    public String channelFromEvent(Object event) {
        return "CHANNEL";
    }

    @Override
    public String guildFromEvent(Object event) { return "GUILD"; }

    @Override
    public boolean isSentFromChannel(Object event) {
        return false;
    }

    @Override
    public boolean isSentFromDM(Object event) {
        return false;
    }

    @Override
    public boolean isAuthorABot(Object event) {
        return false;
    }

    public void dispatch(String raw) {
        String currentPrefix = this.prefixManager.prefixForGuild(null, this);
        if (!raw.startsWith(currentPrefix)) {
            return;
        }

        String cleanedMessage = raw.substring(currentPrefix.length());

        if (cleanedMessage.isEmpty()) {
            return;
        }

        List<String> args = Shlex.shlex(cleanedMessage);
        CommandRegistrar.REGISTRAR.dispatch(args, this, new Object());
    }

    @Override
    public AbstractDiscordRequest<Object, Object> createRequest(Object event, List<String> args) {
        return null;
    }

    public static class Builder extends BaseBuilder<Object, Object, TestDispatcher, Builder> {

        @Override
        protected TestDispatcher getActual() {
            return new TestDispatcher();
        }

        @Override
        protected Builder getActualBuilder() {
            return this;
        }

        public TestDispatcher build() {
            return actualClass;
        }
    }
}
