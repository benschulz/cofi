package de.benshu.cofi.parser;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public interface AstNode {
    static <X, T extends ModelNode<X>> Factory<T> factory(Class<T> klazz) {
        return Factory.get(klazz);
    }

    static Factory<ImmutableList<?>> listFactory() {
        return Factory.getList();
    }

    class Factory<T> implements de.benshu.cofi.parser.Factory {
        private static final Logger logger = LoggerFactory.getLogger(de.benshu.cofi.parser.Factory.class);

        private static ImmutableList<Method> getConstructorMethods(Class<?> nodeType) {
            final ImmutableList.Builder<Method> builder = ImmutableList.builder();

            for (Method m : nodeType.getMethods()) {
                if (!Modifier.isStatic(m.getModifiers()) || m.getAnnotation(AstNodeConstructorMethod.class) == null)
                    continue;

                if (nodeType.isAssignableFrom(m.getReturnType()))
                    builder.add(m);
                else
                    logger.debug("Ignoring constructor method: " + m);
            }

            return builder.build();
        }

        public static <T> Factory<T> get(Class<T> nodeClass) {
            return new Factory<>(nodeClass);
        }

        public static Factory<ImmutableList<?>> getList() {
            return new Factory<>((Class<ImmutableList<?>>) (Object) ImmutableList.class);
        }

        private final Class<T> nodeType;
        private final ImmutableList<Method> methods;

        private Factory(Class<T> nodeType) {
            this.nodeType = nodeType;
            this.methods = getConstructorMethods(nodeType);
        }

        @Override
        public T create(Object... args) {
            if (nodeType.equals(ImmutableList.class)) {
                return nodeType.cast(createList(args));
            }

            for (Method m : methods) {
                if (isApplicable(m, args)) {
                    try {
                        return nodeType.cast(m.invoke(null, args));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }

            throw noConstructor(args);
        }

        private boolean isApplicable(Method m, Object[] args) {
            if (m.getParameterTypes().length != args.length)
                return false;

            for (int i = 0; i < args.length; ++i) {
                if (args[i] != null && !m.getParameterTypes()[i].isAssignableFrom(args[i].getClass()))
                    return false;
            }

            return true;
        }

        private ImmutableList<T> createList(Object[] args) {
            Preconditions.checkArgument(args.length == 1);
            Preconditions.checkArgument(args[0] instanceof ImmutableList<?>);

            return (ImmutableList<T>) args[0];
        }

        private IllegalArgumentException noConstructor(Object[] args) {
            final StringBuilder message = new StringBuilder("No " + nodeType
                    + " constructor method for the given arguments:");

            if (args.length == 0) {
                message.append(" []");
            } else {
                for (Object a : args) {
                    message.append("\n\t").append(a == null ? "null" : a.getClass().getName());
                }
            }

            message.append("\n\nOptions:");

            for (Method m : methods) {
                message.append("\n\t").append(m);
            }

            message.append("\n");

            throw new IllegalArgumentException(message.toString());
        }
    }
}
