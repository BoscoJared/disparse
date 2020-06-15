module disparse.core {
  requires org.slf4j;
  requires reflections;

  exports disparse.discord;
  exports disparse.discord.manager;
  exports disparse.discord.manager.provided;
  exports disparse.parser;
  exports disparse.parser.dispatch;
  exports disparse.parser.exceptions;
  exports disparse.parser.reflection;
  exports disparse.utils;
  exports disparse.utils.help;
}
