package disparse.parser.dispatch;

public enum CooldownScope {
    /**
     * User scope should limit commands to being ran per user only once per cooldown period.
     */
    USER("This command has a per-user cooldown!"),
    /**
     * Channel scope should limit commands to being ran per channel only once per cooldown period.
     */
    CHANNEL("This command has a per-channel cooldown!"),
    /**
     * Guild scope should limit commands to being ran per guild only once per cooldown period.
     */
    GUILD("This command has a per-guild cooldown");

    private String cooldownMessage;

    CooldownScope(final String cooldownMessage) {
        this.cooldownMessage = cooldownMessage;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }
}
