package disparse.parser.reflection;

import eu.infomas.annotation.AnnotationDetector;
import disparse.parser.dispatch.CommandRegistrar;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detector {

    private static final Logger logger = LoggerFactory.getLogger(Detector.class);

    private static final AnnotationDetector.MethodReporter handlerReporter =
            new AnnotationDetector.MethodReporter() {
                private final Logger logger = LoggerFactory.getLogger(Detector.class);

                @SuppressWarnings("unchecked")
                @Override
                public Class<? extends Annotation>[] annotations() {
                    return new Class[] {CommandHandler.class, Injectable.class};
                }

                @Override
                public void reportMethodAnnotation(Class<? extends Annotation> annotation,
                        String className, String methodName) {
                    try {
                        Class<?> clazz = Class.forName(className);
                        for (Method method : clazz.getMethods()) {
                            if (method.getName().equals(methodName)) {
                                if (method.isAnnotationPresent(CommandHandler.class)) {
                                    CommandHandler handler =
                                            method.getAnnotation(CommandHandler.class);
                                    String commandName = handler.commandName();
                                    for (Class<?> paramClazz : method.getParameterTypes()) {
                                        if (paramClazz.isAnnotationPresent(ParsedEntity.class)) {
                                            for (Field field : paramClazz.getDeclaredFields()) {
                                                if (field.isAnnotationPresent(Flag.class)) {
                                                    disparse.parser.Flag parseableFlag = Utils
                                                            .createFlagFromAnnotation(field, field
                                                                    .getAnnotation(Flag.class));
                                                    CommandRegistrar.registrar.register(commandName,
                                                            parseableFlag);
                                                }
                                            }
                                        }
                                    }
                                    CommandRegistrar.registrar.register(commandName, method);
                                } else if (method.isAnnotationPresent(Injectable.class)) {
                                    CommandRegistrar.registrar.register(method);
                                }

                            }
                        }
                    } catch (ClassNotFoundException exec) {
                        logger.error("An error occured", exec);
                    }
                }
            };

    private static final AnnotationDetector handlerDetector =
            new AnnotationDetector(handlerReporter);

    public static void detect() {
        try {
            handlerDetector.detect();
        } catch (IOException exec) {
            logger.error("Error in detecting annotations", exec);
        }
    }
}
