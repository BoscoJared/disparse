package disparse.discord.smalld.guilds;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import disparse.discord.smalld.Event;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class Guilds {

    public static JsonObject getGuildMember(SmallD smalld, String guildId, String userId) {
        return JsonParser.parseString(smalld.get("/guilds/" + guildId + "/members/" + userId)).getAsJsonObject();
    }

    public static Set<String> getSnowflakeRolesForGuildMember(SmallD smalld, String guildId, String userId) {
        JsonObject guildMember = getGuildMember(smalld, guildId, userId);
        Type type = new TypeToken<HashSet<String>>() {
        }.getType();
        return new Gson().fromJson(guildMember.get("roles"), type);
    }

    public static Set<Role> getGuildRoles(SmallD smalld, String guildId) {
        Type type = new TypeToken<HashSet<Role>>() {
        }.getType();
        return new Gson().fromJson(smalld.get("/guilds/" + guildId + "/roles"), type);
    }

    public static String getGuildId(Event event) {
        return event.getJson().get("d").getAsJsonObject().get("guild_id").getAsString();
    }

    public static Set<Role> getRolesForGuildMember(Event event, String userId) {
        String guildId = getGuildId(event);
        Set<String> snowflakes = getSnowflakeRolesForGuildMember(event.getSmalld(), guildId, userId);
        Set<Role> guildRoles = getGuildRoles(event.getSmalld(), guildId);
        guildRoles.removeIf(role -> !snowflakes.contains(role.getId()));
        return guildRoles;
    }
}
