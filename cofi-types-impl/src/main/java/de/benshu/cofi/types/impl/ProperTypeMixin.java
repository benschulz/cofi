package de.benshu.cofi.types.impl;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.types.Kind;
import de.benshu.cofi.types.bound.ProperType;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.commons.core.Optional;

public interface ProperTypeMixin<X extends TypeSystemContext<X>, S extends ProperTypeMixin<X, S>>
        extends TypeMixin<X, S>, ProperType<X, S> {

    @Override
    ProperTypeMixin<X, ?> substitute(Substitutions<X> substitutions);

    default Substitutable<X, ? extends ProperTypeMixin<X, ?>> substitutable() {
        return new Substitutable<X, ProperTypeMixin<X, ?>>() {
            @Override
            public ProperTypeMixin<X, ?> substitute(Substitutions<X> substitutions) {
                return ProperTypeMixin.this;
            }

            @Override
            public String toDescriptor() {
                return ProperTypeMixin.this.toDescriptor();
            }
        };
    }

    @Override
    default Optional<AbstractMember<X>> lookupMember(String name) {
        return Optional.from(getMembers().get(name));
    }

    @Override
    default Kind getKind() {
        return KindImpl.PROPER_ORDER;
    }

    ImmutableMap<String, AbstractMember<X>> getMembers();

    @Override
    de.benshu.cofi.types.ProperType unbind();

    java.util.Optional<TypeConstructorInvocation<X>> tryGetInvocationOf(TypeConstructorMixin<X, ?, ?> typeConstructor);
}
