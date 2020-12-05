package disparse.parser.reflection;

import disparse.parser.dispatch.CooldownScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown {

  int amount() default 0;

  ChronoUnit unit() default ChronoUnit.SECONDS;

  CooldownScope scope() default CooldownScope.USER;

  MessageStrategy messageStrategy() default MessageStrategy.SILENT;
}
