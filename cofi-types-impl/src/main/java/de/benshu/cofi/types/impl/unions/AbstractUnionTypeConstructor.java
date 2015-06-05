package de.benshu.cofi.types.impl.unions;

import de.benshu.cofi.types.ConstructedUnionType;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.bound.UnionTypeConstructor;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.AbstractUnboundProperTypeConstructor;
import de.benshu.cofi.types.impl.NamedProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.tags.IndividualTags;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractUnionTypeConstructor<X extends TypeSystemContext<X>>
        extends AbstractTypeConstructor<X, AbstractUnionTypeConstructor<X>, ConstructedUnionTypeImpl<X>>
        implements NamedProperTypeConstructorMixin<X, AbstractUnionTypeConstructor<X>, ConstructedUnionTypeImpl<X>>,
                   UnionTypeConstructor<X, AbstractUnionTypeConstructor<X>, ConstructedUnionTypeImpl<X>> {

    public static <X extends TypeSystemContext<X>> UnboundUnionTypeConstructor<X> create(UnionTypeDeclaration<X> declaration) {
        return new UnboundUnionTypeConstructor<>(declaration);
    }

    AbstractUnionTypeConstructor(X context) {
        super(context);
    }

    public boolean isSameAs(TypeMixin<X, ?> other) {
        return this == other
                || other instanceof AbstractUnionTypeConstructor<?>
                && ((AbstractUnionTypeConstructor<?>) other).getOriginal() == getOriginal();
    }

    public abstract AbstractTypeList<X, TypeConstructorMixin<X, ?, ProperTypeMixin<X, ?>>> getElements();

    @Override
    public abstract TypeParameterListImpl<X> getParameters();

    @Override
    public ConstructedUnionTypeImpl<X> apply(AbstractTypeList<X, ?> arguments) {
        checkArgument(arguments.size() == getParameters().size());

        return new ConstructedUnionTypeImpl<>(
                this,
                arguments,
                getElements().map(e -> e.apply(arguments)),
                TagCombiners.apply(getTags())
        );
    }

    public abstract UnboundUnionTypeConstructor<X> getOriginal();

    @Override
    public AbstractUnionTypeConstructor<X> setTags(IndividualTags tags) {
        return new DerivedUnionTypeConstructor<>(getOriginal(), getContext(), TagCombiners.setAll(this, tags));
    }

    @Override
    public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
        return self();
    }

    @Override
    public de.benshu.cofi.types.UnionTypeConstructor unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperTypeConstructor<X, AbstractUnionTypeConstructor<X>, ConstructedUnionType>
            implements de.benshu.cofi.types.UnionTypeConstructor {
        public Unbound(AbstractUnionTypeConstructor<X> unbound) {
            super(unbound);
        }

        @Override
        public ConstructedUnionType apply(TypeList<?> arguments) {
            return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
        }

        @Override
        public TypeList<ProperTypeConstructor<?>> getElements() {
            return bound.getElements().unbind();
        }
    }
}
