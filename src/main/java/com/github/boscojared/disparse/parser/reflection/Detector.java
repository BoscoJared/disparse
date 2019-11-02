package com.github.boscojared.disparse.parser.reflection;

import com.github.boscojared.disparse.parser.Command;
import com.github.boscojared.disparse.parser.CommandFlag;
import com.github.boscojared.disparse.parser.dispatch.CommandRegistrar;
import eu.infomas.annotation.AnnotationDetector;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                                    Command command = new Command(handler.commandName(),
                                                                  handler.description(), handler.roles());
                                    for (Class<?> paramClazz : method.getParameterTypes()) {
                                        if (paramClazz.isAnnotationPresent(ParsedEntity.class)) {
                                            Field[] fields = allImplicitFields(paramClazz);
                                            for (Field field : fields) {
                                                if (field.isAnnotationPresent(Flag.class)) {
                                                    CommandFlag flag = Utils
                                                            .createFlagFromAnnotation(field, field
                                                                    .getAnnotation(Flag.class));
                                                    CommandRegistrar.REGISTRAR
                                                        .register(command,
                                                                  flag);
                                                }
                                            }
                                        }
                                    }
                                    CommandRegistrar.REGISTRAR
                                        .register(command, method);
                                } else if (method.isAnnotationPresent(Injectable.class)) {
                                    CommandRegistrar.REGISTRAR.register(method);
                                }

                            }
                        }
                    } catch (ClassNotFoundException exec) {
                        logger.error("An error occurred", exec);
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
