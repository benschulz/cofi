package de.benshu.cofi.types.impl.templates;

import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.interpreters.HierarchyInterpreter;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.Tags;

import static de.benshu.cofi.types.impl.declarations.Interpreter.id;

class DerivedTemplateTypeConstructor<X extends TypeSystemContext<X>> extends AbstractTemplateTypeConstructor<X> {
    private final UnboundTemplateTypeConstructor<X> original;
    private final Tags tags;

    public DerivedTemplateTypeConstructor(UnboundTemplateTypeConstructor<X> original, X context, Tagger tagger) {
        super(context);

        this.original = original;
        this.tags = tagger.tag(this);
    }

    @Override
    public TypeParameterListImpl<X> getParameters() {
        return getDeclaration().supplyParameters(getContext(), this, id());
    }

    @Override
    public AbstractTypeList<X, TemplateTypeConstructorMixin<X>> getSupertypes() {
        final HierarchyInterpreter<X, TemplateTypeImpl<X>> hierarchyInterpreter = HierarchyInterpreter.of(TemplateTypeImpl.class::isInstance, t -> (TemplateTypeImpl<X>) t);
        AbstractTypeList<X, TemplateTypeImpl<X>> checkedSupertypes = getDeclaration().supplyHierarchy(getContext(), this, hierarchyInterpreter);

        if (checkedSupertypes.isEmpty()) {
            TemplateTypeImpl<X> top = getContext().getTypeSystem().getTop();

            if (getOriginal() != top.getConstructor().getOriginal()) {
                checkedSupertypes = AbstractTypeList.of(top);
            }
        }

        return checkedSupertypes.map(s -> AdHoc.templateTypeConstructor(getContext(), getParameters(), s));
    }

    private TemplateTypeDeclaration<X> getDeclaration() {
        return getOriginal().getDeclaration();
    }

    @Override
    public UnboundTemplateTypeConstructor<X> getOriginal() {
        return original;
    }

    @Override
    public Tags getTags() {
        return tags;
    }
}
