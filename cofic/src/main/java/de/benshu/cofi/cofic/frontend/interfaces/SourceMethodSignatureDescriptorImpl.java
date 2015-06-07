package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutable;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.tags.IndividualTags;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;

final class SourceMethodSignatureDescriptorImpl implements SourceMethodSignatureDescriptor<Pass> {
    private final TypeParameterListImpl<Pass> typeParams;
    final ImmutableList<ImmutableList<SourceType<Pass>>> params;
    private final SourceType<Pass> returnType;
    private final IndividualTags tags;

    public SourceMethodSignatureDescriptorImpl(TypeParameterListImpl<Pass> typeParams, ImmutableList<ImmutableList<SourceType<Pass>>> params, SourceType<Pass> returnType, IndividualTags tags) {
        this.typeParams = typeParams;
        this.params = params;
        this.returnType = returnType;
        this.tags = tags;
    }

    @Override
    public SourceType<Pass> getReturnType() {
        return returnType;
    }

    @Override
    public TypeParameterListImpl<Pass> getTypeParameters() {
        return typeParams;
    }

    @Override
    public ImmutableList<ImmutableList<SourceType<Pass>>> getParameterTypes() {
        return params;
    }

    public TypeConstructorMixin<Pass, ?, ProperTypeMixin<Pass, ?>> getType(Pass pass) {
        TypeMixin<Pass, ?> t = returnType.getType();

        for (ImmutableList<SourceType<Pass>> ps : getParameterTypes().reverse()) {
            t = pass.getTypeSystem().constructFunction(ps.stream().map(SourceType::getType).collect(typeList()), t);
        }

        return AdHoc.typeConstructor(pass, typeParams, Substitutable.unchecked(t));
    }

    @Override
    public IndividualTags getTags() {
        return tags;
    }
}
