package disparse.discord.smalld;

import com.github.princesslana.jsonf.JsonF;
import com.github.princesslana.smalld.SmallD;

public class Event {
  private final SmallD smalld;
  private final JsonF json;

  public Event(SmallD smalld, JsonF json) {
    this.smalld = smalld;
    this.json = json;
  }

  public SmallD getSmalld() {
    return smalld;
  }

  public JsonF getJson() {
    return json;
  }
}
