package disparse.discord.smalld.permissions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import disparse.discord.smalld.Event;
import disparse.discord.smalld.Utils;
import disparse.discord.smalld.guilds.Guilds;
import disparse.discord.smalld.guilds.Role;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PermissionUtils {
  public static PermissionBase computeBasePermissions(Event event) {
    String userId = Utils.getAuthorId(event);

    long perm = 0x0;
    for (Role role : Guilds.getRolesForGuildMember(event, userId)) {
      perm |= role.getPermissions();
    }

    return new PermissionBase(perm);
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
    JsonObject channelObj = Utils.getChannel(event, channelId);
    long perms = permissionBase.getValue();

    Type type = new TypeToken<List<Overwrite>>() {
    }.getType();

    List<Overwrite> overwrites = new Gson().fromJson(channelObj.get("permission_overwrites"), type);

    Map<String, Overwrite> overwriteMap = overwrites.stream()
            .collect(Collectors.toMap(o -> o.id, Function.identity()));

    Overwrite everyoneOverwrite = overwriteMap.remove(guildId);

    if (everyoneOverwrite != null) {
      perms &= ~everyoneOverwrite.deny;
      perms |= everyoneOverwrite.allow;
    }

    AtomicLong allow = new AtomicLong();
    AtomicLong deny = new AtomicLong();

    Guilds.getRolesForGuildMember(event, userId)
            .stream()
            .map(r -> r.getId())
            .map(i -> overwriteMap.getOrDefault(i, null))
            .filter(Objects::nonNull)
            .forEach(overwrite -> {
              allow.updateAndGet(v -> v | overwrite.allow);
              deny.updateAndGet(v -> v | overwrite.allow);
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

  public static class Overwrite {
    private String id;
    private String type;
    private Integer allow;
    private Integer deny;

    public Overwrite(String id, String type, Integer allow, Integer deny) {
      this.id = id;
      this.type = type;
      this.allow = allow;
      this.deny = deny;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Overwrite overwrite = (Overwrite) o;
      return id.equals(overwrite.id) &&
              type.equals(overwrite.type) &&
              allow.equals(overwrite.allow) &&
              deny.equals(overwrite.deny);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, type, allow, deny);
    }
  }
}
