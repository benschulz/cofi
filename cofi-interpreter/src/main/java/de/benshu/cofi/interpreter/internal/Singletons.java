package de.benshu.cofi.interpreter.internal;

import com.google.common.base.Equivalence;
import com.google.common.collect.Maps;
import de.benshu.cofi.runtime.Multiton;
import de.benshu.cofi.runtime.Singleton;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

// TODO rename to Multitons
public class Singletons {
    private final ConcurrentMap<Multiton, CofiObject> singletons = Maps.newConcurrentMap();
    private final ConcurrentMap<Multiton, ConcurrentMap<Equivalence.Wrapper<TypeList<?>>, CofiObject>> multitons = Maps.newConcurrentMap();
    private final TypeArgumentEquivalence typeArgumentEquivalence = new TypeArgumentEquivalence();

    public CofiObject lookUpOrCreate(Singleton singleton) {
        return lookUpOrCreateSingletonMultiton(singleton);
    }

    public CofiObject lookUpOrCreate(Multiton multiton, TypeList<?> typeArguments) {
        if (typeArguments.isEmpty())
            return lookUpOrCreateSingletonMultiton(multiton);

        final ConcurrentMap<Equivalence.Wrapper<TypeList<?>>, CofiObject> byTypeArguments = multitons.computeIfAbsent(multiton, m -> new ConcurrentHashMap<>());

        return byTypeArguments.computeIfAbsent(typeArgumentEquivalence.wrap(typeArguments),
                w -> new CofiObject(multiton.getType().apply(typeArguments)));
    }

    // TODO Consider getting rid of SingletonCompanion.
    private CofiObject lookUpOrCreateSingletonMultiton(Multiton multiton) {
        checkArgument(multiton.getTypeParameters().isEmpty());

        return singletons.computeIfAbsent(multiton, s -> new CofiObject(s.getType().applyTrivially()));
    }

    private static class TypeArgumentEquivalence extends Equivalence<TypeList<?>> {
        @Override
        protected boolean doEquivalent(TypeList<?> a, TypeList<?> b) {
            return AbstractConstraints.<RuntimeContext>none()
                    .areEqualTypes(AbstractTypeList.rebind(a), AbstractTypeList.rebind(b));
        }

        @Override
        protected int doHash(TypeList<?> types) {
            // *sadface*
            // TODO Come up with a decently distributed hash.
            return 0;
        }
    }
}
