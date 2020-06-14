package disparse.discord.smalld;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Utils {

  public static void sendMessage(Event event, String content) {
    SmallD smalld = event.getSmalld();
    JsonObject json = event.getJson();
    String channelId = getChannelId(json);

    JsonObject output = new JsonObject();
    output.addProperty("content", content);

    smalld.post("/channels/" + channelId + "/messages", output.toString());
  }

  public static void sendEmbed(Event event, JsonObject embed) {
    SmallD smalld = event.getSmalld();
    JsonObject json = event.getJson();
    String channelId = getChannelId(json);

    JsonObject output = new JsonObject();
    output.add("embed", embed);

    smalld.post("/channels/" + channelId + "/messages", output.toString());
  }

  public static String getChannelId(JsonObject json) {
    return json.get("d").getAsJsonObject().get("channel_id").getAsString();
  }

  public static boolean isTextChannel(Event event) {
    String channelId = getChannelId(event.getJson());

    JsonObject channelObj = Utils.getChannel(event, channelId);

    return channelObj.get("type").getAsInt() == 0;
  }

  public static boolean isDm(Event event) {
    String channelId = getChannelId(event.getJson());

    JsonObject channelObj =
        new Gson().fromJson(event.getSmalld().get("/channels/" + channelId), JsonObject.class);

    return channelObj.get("type").getAsInt() == 1 || channelObj.get("type").getAsInt() == 3;
  }

  public static boolean isMessageCreate(JsonObject json) {
    JsonElement opField = json.get("op");
    boolean op = false;
    if (opField != null && opField.isJsonPrimitive() && opField.getAsJsonPrimitive().isNumber()) {

      op = opField.getAsInt() == 0;
    }

    JsonElement tField = json.get("t");
    boolean isMessageCreate = false;
    if (tField != null && tField.isJsonPrimitive() && tField.getAsJsonPrimitive().isString()) {

      isMessageCreate = tField.getAsString().equals("MESSAGE_CREATE");
    }

    return op && isMessageCreate;
  }

  public static String getMessageContent(JsonObject json) {
    return json.get("d").getAsJsonObject().get("content").getAsString();
  }

  public static JsonObject getAuthor(JsonObject json) {
    return json.get("d").getAsJsonObject().get("author").getAsJsonObject();
  }

  public static boolean isAuthorBot(JsonObject json) {
    JsonObject author = getAuthor(json);
    boolean isBot = false;
    JsonElement element = author.get("bot");

    if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
      isBot = element.getAsBoolean();
    }

    return isBot;
  }

  public static String getAuthorId(Event event) {
    JsonObject author = getAuthor(event.getJson());
    return author.get("id").getAsString();
  }

  public static JsonObject getChannel(Event event, String channelId) {
    return new Gson().fromJson(event.getSmalld().get("/channels/" + channelId), JsonObject.class);
  }
}
