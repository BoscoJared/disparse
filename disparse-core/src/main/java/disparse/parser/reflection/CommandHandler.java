package disparse.parser.reflection;

import disparse.parser.dispatch.IncomingScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {

    String commandName();

    String description() default "no description available";

    String[] roles() default {};

    boolean canBeDisabled() default true;

    IncomingScope acceptFrom() default IncomingScope.ALL;

    String[] aliases() default {};
}
