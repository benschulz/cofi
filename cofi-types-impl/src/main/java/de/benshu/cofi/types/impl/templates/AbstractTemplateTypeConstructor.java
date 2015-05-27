package de.benshu.cofi.types.impl.templates;

import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.AbstractUnboundTypeConstructor;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.tags.IndividualTags;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class AbstractTemplateTypeConstructor<X extends TypeSystemContext<X>>
        extends AbstractTypeConstructor<X, TemplateTypeConstructorMixin<X>, TemplateTypeImpl<X>>
        implements TemplateTypeConstructorMixin<X> {

    public static <X extends TypeSystemContext<X>> UnboundTemplateTypeConstructor<X> create(TemplateTypeDeclaration<X> declaration) {
        return new UnboundTemplateTypeConstructor<>(declaration);
    }

    protected AbstractTemplateTypeConstructor(X context) {
        super(context);
    }

    @Override
    public TemplateTypeImpl<X> apply(AbstractTypeList<X, ?> arguments) {
        checkArgument(arguments.size() == getParameters().size());

        return new TemplateTypeImpl.Original<>(this).apply(arguments);
    }

    public abstract UnboundTemplateTypeConstructor<X> getOriginal();

    @Override
    public TemplateTypeConstructorMixin<X> setTags(IndividualTags tags) {
        return new DerivedTemplateTypeConstructor<>(getOriginal(), getContext(), TagCombiners.setAll(this, tags));
    }

    @Override
    public abstract AbstractTypeList<X, TemplateTypeConstructorMixin<X>> getSupertypes();

    @Override
    public TypeConstructorMixin<X, ?, ?> substitute(Substitutions<X> substitutions) {
        return self();
    }

    @Override
    public de.benshu.cofi.types.TemplateTypeConstructor unbind() {
        return new Unbound<>(this);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundTypeConstructor<X, AbstractTemplateTypeConstructor<X>, TemplateType>
            implements de.benshu.cofi.types.TemplateTypeConstructor {

        public Unbound(AbstractTemplateTypeConstructor<X> unbound) {
            super(unbound);
        }

        @Override
        public TypeList<TemplateTypeConstructor> getSupertypes() {
            return bound.getSupertypes().unbind();
        }

        @Override
        public de.benshu.cofi.types.TemplateType apply(TypeList<?> arguments) {
            return bound.apply(AbstractTypeList.rebind(arguments)).unbind();
        }
    }
}
