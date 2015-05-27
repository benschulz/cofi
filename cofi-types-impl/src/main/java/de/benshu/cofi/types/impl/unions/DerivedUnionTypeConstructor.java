package de.benshu.cofi.types.impl.unions;

import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.interpreters.HierarchyInterpreter;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.Tags;

import static de.benshu.cofi.types.impl.declarations.Interpreter.id;

class DerivedUnionTypeConstructor<X extends TypeSystemContext<X>> extends AbstractUnionTypeConstructor<X> {
    private UnboundUnionTypeConstructor<X> original;
    private final Tags tags;

    DerivedUnionTypeConstructor(UnboundUnionTypeConstructor<X> original, X context, Tagger tagger) {
        super(context);

        this.original = original;
        this.tags = tagger.tag(this);
    }

    @Override
    public AbstractTypeList<X, TypeConstructorMixin<X, ?, ProperTypeMixin<X, ?>>> getElements() {
        final HierarchyInterpreter<X, ProperTypeMixin<X, ?>> hierarchyInterpreter = HierarchyInterpreter.of(ProperTypeMixin.class::isInstance, t -> (ProperTypeMixin<X, ?>) t);
        return getDeclaration().supplyHierarchy(getContext(), hierarchyInterpreter)
                .map(e -> AdHoc.typeConstructor(getContext(), getParameters(), e.substitutable()));
    }

    @Override
    public TypeParameterListImpl<X> getParameters() {
        return getDeclaration().supplyParameters(getContext(), id());
    }

    private UnionTypeDeclaration<X> getDeclaration() {
        return getOriginal().getDeclaration();
    }

    @Override
    public UnboundUnionTypeConstructor<X> getOriginal() {
        return original;
    }

    @Override
    public Tags getTags() {
        return tags;
    }
}
