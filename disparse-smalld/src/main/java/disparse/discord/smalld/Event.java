package disparse.discord.smalld;

import com.github.princesslana.smalld.SmallD;
import com.google.gson.JsonObject;

public class Event {
  private SmallD smalld;
  private JsonObject json;

  public Event(SmallD smalld, JsonObject json) {
    this.smalld = smalld;
    this.json = json;
  }

  public SmallD getSmalld() {
    return smalld;
  }

  public JsonObject getJson() {
    return json;
  }
}
