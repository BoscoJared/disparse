package disparse.parser.reflection;

import java.lang.annotation.*;

@Repeatable(Usages.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Usage {
  String usage();

  String description();
}
