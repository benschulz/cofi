package de.benshu.cofi.types.impl.test;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.tags.IndividualTags;

public class MethodSignatureDesc implements SourceMethodSignatureDescriptor<TestContext> {
    private final TypeParameterListImpl<TestContext> typeParams;
    private final ImmutableList<ImmutableList<SourceType<TestContext>>> paramTypes;
    private final SourceType<TestContext> returnType;

    public MethodSignatureDesc(TypeParameterListImpl<TestContext> typeParams,
                               ImmutableList<ImmutableList<SourceType<TestContext>>> paramTypes,
                               SourceType<TestContext> returnType) {
        this.typeParams = typeParams;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    @Override
    public SourceType<TestContext> getReturnType() {
        return returnType;
    }

    @Override
    public TypeParameterListImpl<TestContext> getTypeParameters() {
        return typeParams;
    }

    @Override
    public ImmutableList<ImmutableList<SourceType<TestContext>>> getParameterTypes() {
        return paramTypes;
    }

    @Override
    public IndividualTags getTags() {
        return IndividualTags.empty();
    }
}
