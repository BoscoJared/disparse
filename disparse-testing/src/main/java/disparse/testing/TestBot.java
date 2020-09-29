package disparse.testing;

import disparse.testing.d4j.D4JApplication;
import disparse.testing.jda.JDAApplication;
import disparse.testing.smalld.SmallDApplication;
import java.util.Map;

public class TestBot {

  private static final String TOKEN_ENV_NAME = "DISPARSE_TEST_TOKEN";
  private static final String VARIANT_ENV_NAME = "DISPARSE_VARIANT";

  public static void main(String[] args) throws Exception {
    String token = System.getenv(TOKEN_ENV_NAME);
    String stringVariant = System.getenv(VARIANT_ENV_NAME);
    DiscordVariant variant = DiscordVariant.valueOf(stringVariant);

    var map =
        Map.of(
            DiscordVariant.JDA, new JDAApplication(),
            DiscordVariant.D4J, new D4JApplication(),
            DiscordVariant.SMALLD, new SmallDApplication());

    map.get(variant).start(token);
  }

  private enum DiscordVariant {
    JDA,
    D4J,
    SMALLD;
  }
}
