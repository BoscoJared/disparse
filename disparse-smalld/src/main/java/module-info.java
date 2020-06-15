module disparse.smalld {
  requires com.google.gson;
  requires disparse.core;
  requires slf4j.api;
  requires smalld;

  exports disparse.discord.smalld;
  exports disparse.discord.smalld.guilds;
  exports disparse.discord.smalld.permissions;
}
