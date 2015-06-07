package de.benshu.cofi.types.impl.intersections;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import de.benshu.cofi.types.ConstructedIntersectionType;
import de.benshu.cofi.types.ProperTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.bound.IntersectionTypeConstructor;
import de.benshu.cofi.types.bound.Type;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.AbstractUnboundProperTypeConstructor;
import de.benshu.cofi.types.impl.NamedProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.IntersectionTypeDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.tags.IndividualTags;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractIntersectionTypeConstructor<X extends TypeSystemContext<X>>
        extends AbstractTypeConstructor<X, AbstractIntersectionTypeConstructor<X>, ConstructedIntersectionTypeImpl<X>>
        implements NamedProperTypeConstructorMixin<X, AbstractIntersectionTypeConstructor<X>, ConstructedIntersectionTypeImpl<X>>,
                   IntersectionTypeConstructor<X, AbstractIntersectionTypeConstructor<X>, ConstructedIntersectionTypeImpl<X>> {

    public static <X extends TypeSystemContext<X>> UnboundIntersectionTypeConstructor<X> create(IntersectionTypeDeclaration<X> declaration) {
        return new UnboundIntersectionTypeConstructor<>(declaration);
    }

    AbstractIntersectionTypeConstructor(X context) {
        super(context);
    }

    public boolean isSameAs(TypeMixin<X, ?> other) {
        return this == other
                || other instanceof AbstractIntersectionTypeConstructor<?>
                && ((AbstractIntersectionTypeConstructor<?>) other).getOriginal() == getOriginal();
    }

    public abstract AbstractTypeList<X, TypeConstructorMixin<X, ?, ProperTypeMixin<X, ?>>> getElements();

    @Override
    public abstract TypeParameterListImpl<X> getParameters();

    @Override
    public ConstructedIntersectionTypeImpl<X> apply(AbstractTypeList<X, ?> arguments) {
        checkArgument(arguments.size() == getParameters().size());

        return new ConstructedIntersectionTypeImpl<>(
                this,
                arguments,
                getElements().map(e -> e.apply(arguments)),
                TagCombiners.apply(getTags())
        );
    }

    public abstract UnboundIntersectionTypeConstructor<X> getOriginal();

    @Override
    public AbstractIntersectionTypeConstructor<X> setTags(IndividualTags tags) {
        return new DerivedIntersectionTypeConstructor<>(getOriginal(), getContext(), TagCombiners.setAll(this, tags));
    }

    @Override
    public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
        return self();
    }

    @Override
    public de.benshu.cofi.types.IntersectionTypeConstructor unbind() {
        return new Unbound<>(this);
    }

    @Override
    public String toDescriptor() {
        return CharMatcher.WHITESPACE.removeFrom(debug());
    }

    @Override
    public String debug() {
        return "(" + Joiner.on(" & ").join(getElements().mapAny(Type::debug)) + ")";
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundProperTypeConstructor<X, AbstractIntersectionTypeConstructor<X>, ConstructedIntersectionType>
            implements de.benshu.cofi.types.IntersectionTypeConstructor {

        public Unbound(AbstractIntersectionTypeConstructor<X> unbound) {
            super(unbound);
        }

        @Override
        public ConstructedIntersectionType apply(TypeList<?> arguments) {
            return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
        }

        @Override
        public TypeList<ProperTypeConstructor<?>> getElements() {
            return bound.getElements().unbind();
        }
    }
}
