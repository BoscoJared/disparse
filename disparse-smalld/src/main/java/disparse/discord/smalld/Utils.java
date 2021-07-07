package disparse.discord.smalld;

import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonObject;
import java.util.Arrays;

public class Utils {

  public static void sendMessage(Event event, String content) {
    SmallD smalld = event.getSmalld();
    JsonF json = event.getJson();
    String channelId = getChannelId(json);

    JsonObject output = new JsonObject();
    output.addProperty("content", content);

    smalld.post("/channels/" + channelId + "/messages", output.toString());
  }

  public static void sendReact(Event event, String value) {
    SmallD smalld = event.getSmalld();
    JsonF json = event.getJson();
    String channelId = getChannelId(json);
    String messageId = getMessageId(json);

    smalld.put("/channels/" + channelId + "/messages/" + messageId + "/reactions/:clock3:/@me", "");
  }

  public static void sendEmbed(Event event, JsonObject embed) {
    SmallD smalld = event.getSmalld();
    JsonF json = event.getJson();
    String channelId = getChannelId(json);

    JsonObject output = new JsonObject();
    output.add("embed", embed);

    smalld.post("/channels/" + channelId + "/messages", output.toString());
  }

  public static String getChannelId(JsonF json) {
    return json.get("d", "channel_id").asString().orElseThrow(IllegalStateException::new);
  }

  private static String getMessageId(JsonF json) {
    return json.get("d", "id").asString().orElseThrow(IllegalStateException::new);
  }

  public static boolean isTextChannel(Event event) {
    return isChannelType(event, 0);
  }

  public static boolean isDm(Event event) {
    return isChannelType(event, 1, 3);
  }

  private static boolean isChannelType(Event event, int... types) {
    String channelId = getChannelId(event.getJson());
    JsonF channelObj = Utils.getChannel(event, channelId);
    JsonF type = channelObj.get("type");

    return Arrays.stream(types).anyMatch(type::isEqualTo);
  }

  public static boolean isMessageCreate(JsonF json) {
    return json.get("op").isEqualTo(0) && json.get("t").isEqualTo("MESSAGE_CREATE");
  }

  public static String getMessageContent(JsonF json) {
    return json.get("d", "content").asString().orElseThrow(IllegalStateException::new);
  }

  private static JsonF getAuthor(JsonF json) {
    return json.get("d", "author");
  }

  public static boolean isAuthorBot(JsonF json) {
    return getAuthor(json).get("bot").asBoolean().orElse(false);
  }

  public static String getAuthorId(Event event) {
    return getAuthor(event.getJson()).get("id").asString().orElseThrow(IllegalStateException::new);
  }

  public static JsonF getChannel(Event event, String channelId) {
    return JsonF.parse(event.getSmalld().get("/channels/" + channelId));
  }
}
