package disparse.parser.reflection;

import disparse.parser.Command;
import disparse.parser.CommandFlag;
import disparse.parser.CommandUsage;
import disparse.parser.dispatch.CommandRegistrar;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.dispatch.IncomingScope;
import eu.infomas.annotation.AnnotationDetector;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Detector {

    private static final Logger logger = LoggerFactory.getLogger(Detector.class);

    public static <E, T> CommandRegistrar<E, T> detect(Reflections reflections) {
        CommandRegistrar<E, T> registrar = new CommandRegistrar<>();
        Set<Method> methods = reflections.getMethodsAnnotatedWith(CommandHandler.class);

        for (Method method : methods) {
            CommandHandler handler = method.getAnnotation(CommandHandler.class);
            Duration cooldownDuration = Duration.ZERO;
            CooldownScope scope = CooldownScope.USER;
            boolean sendCooldownMessage = false;
            IncomingScope acceptFrom = handler.acceptFrom();
            if (method.isAnnotationPresent(Cooldown.class)) {
                Cooldown cooldown = method.getAnnotation(Cooldown.class);
                cooldownDuration = Duration.of(cooldown.amount(), cooldown.unit());
                scope = cooldown.scope();
                sendCooldownMessage = cooldown.sendCooldownMessage();
            }
            List<CommandUsage> commandUsages = Arrays.stream(method.getAnnotationsByType(Usage.class))
                    .map(usageMapping -> new CommandUsage(usageMapping.usage(), usageMapping.description()))
                    .collect(Collectors.toList());

            Command command =
                    new Command(handler.commandName(), handler.description(), handler.roles(), handler.canBeDisabled(), cooldownDuration, scope, sendCooldownMessage, acceptFrom, handler.aliases(), handler.perms(), commandUsages);
            for (Class<?> paramClazz : method.getParameterTypes()) {
                if (paramClazz.isAnnotationPresent(ParsedEntity.class)) {
                    Field[] fields = allImplicitFields(paramClazz);
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Flag.class)) {
                            CommandFlag flag =
                                    Utils.createFlagFromAnnotation(
                                            field, field.getAnnotation(Flag.class));
                            registrar.register(command, flag);
                        }
                    }
                }
            }
            registrar.register(command, method);
        }

        Set<Method> injectables = reflections.getMethodsAnnotatedWith(Injectable.class);
        injectables.forEach(registrar::register);

        return registrar;
    }

    public static Field[] allImplicitFields(Class<?> clazz) {
        List<Field> fields;
        if (clazz == null) {
            return new Field[0];
        } else {
            fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        }
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }

        return fields.toArray(Field[]::new);
    }
}
