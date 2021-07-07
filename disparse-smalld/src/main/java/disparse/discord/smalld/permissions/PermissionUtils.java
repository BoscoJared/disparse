package disparse.discord.smalld.permissions;

import com.github.princesslana.jsonf.JsonF;
import disparse.discord.smalld.Event;
import disparse.discord.smalld.Utils;
import disparse.discord.smalld.guilds.Guilds;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PermissionUtils {
  public static PermissionBase computeBasePermissions(Event event) {
    String userId = Utils.getAuthorId(event);

    return Guilds.getRolesForGuildMember(event, userId).stream()
        .map(r -> new PermissionBase(r.getPermissions()))
        .reduce(new PermissionBase(0L), PermissionBase::plus);
  }

  public static PermissionBase computeOverwrites(PermissionBase permissionBase, Event event) {
    if (!Utils.isTextChannel(event)) {
      return null;
    }

    if (permissionBase.contains(Permission.ADMINISTRATOR)) {
      return PermissionBase.of(Permission.ADMINISTRATOR);
    }

    String channelId = Utils.getChannelId(event.getJson());
    String guildId = Guilds.getGuildId(event);
    String userId = Utils.getAuthorId(event);
    JsonF channelObj = Utils.getChannel(event, channelId);
    long perms = permissionBase.getValue();

    Map<String, Overwrite> overwriteMap =
        channelObj.get("permission_overwrites").stream()
            .map(Overwrite::fromJson)
            .collect(Collectors.toMap(o -> o.id, Function.identity()));

    Overwrite everyoneOverwrite = overwriteMap.remove(guildId);

    if (everyoneOverwrite != null) {
      perms &= ~everyoneOverwrite.deny;
      perms |= everyoneOverwrite.allow;
    }

    AtomicLong allow = new AtomicLong();
    AtomicLong deny = new AtomicLong();

    Guilds.getRolesForGuildMember(event, userId).stream()
        .map(r -> r.getId())
        .map(i -> overwriteMap.getOrDefault(i, null))
        .filter(Objects::nonNull)
        .forEach(
            overwrite -> {
              allow.updateAndGet(v -> v | overwrite.allow);
              deny.updateAndGet(v -> v | overwrite.deny);
            });

    perms &= ~deny.get();
    perms |= allow.get();

    Overwrite memberSpecific = overwriteMap.getOrDefault(userId, null);

    if (memberSpecific != null) {
      perms &= ~memberSpecific.deny;
      perms |= memberSpecific.allow;
    }

    return new PermissionBase(perms);
  }

  public static PermissionBase computeAllPerms(Event event) {
    PermissionBase base = computeBasePermissions(event);
    return computeOverwrites(base, event);
  }

  private static class Overwrite {
    private String id;
    private String type;
    private Long allow;
    private Long deny;

    private Overwrite() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Overwrite overwrite = (Overwrite) o;
      return id.equals(overwrite.id)
          && type.equals(overwrite.type)
          && allow.equals(overwrite.allow)
          && deny.equals(overwrite.deny);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, type, allow, deny);
    }

    public static Overwrite fromJson(JsonF json) {
      Overwrite o = new Overwrite();
      o.id = json.get("id").asString().orElseThrow(IllegalStateException::new);
      o.type = json.get("type").asString().orElseThrow(IllegalStateException::new);
      o.allow = json.get("allow").asLong().orElseThrow(IllegalStateException::new);
      o.deny = json.get("deny").asLong().orElseThrow(IllegalStateException::new);
      return o;
    }
  }
}
