package disparse.parser.dispatch;

public enum CooldownScope {
    /**
     * User scope should limit commands to being ran per user only once per cooldown period.
     */
    USER,
    /**
     * Channel scope should limit commands to being ran per channel only once per cooldown period.
     */
    CHANNEL,
    /**
     * Guild scope should limit commands to being ran per guild only once per cooldown period.
     */
    GUILD
}
