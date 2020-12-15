module disparse.smalld {
  requires com.google.gson;
  requires disparse.core;
  requires org.slf4j;
  requires com.github.princesslana.smalld;

  exports disparse.discord.smalld;
  exports disparse.discord.smalld.guilds;
  exports disparse.discord.smalld.permissions;
}
