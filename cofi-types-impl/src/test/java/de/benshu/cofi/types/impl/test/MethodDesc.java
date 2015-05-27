package de.benshu.cofi.types.impl.test;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.declarations.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.tags.IndividualTags;

final class MethodDesc implements SourceMethodDescriptor<TestContext> {
    private final ImmutableList<SourceMethodSignatureDescriptor<TestContext>> signatureDescriptors;
    private final String name;

    public MethodDesc(String name,
                      ImmutableList<SourceMethodSignatureDescriptor<TestContext>> signatureDescriptors) {
        this.name = name;
        this.signatureDescriptors = signatureDescriptors;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IndividualTags getTags(TestContext context) {
        return IndividualTags.empty();
    }

    @Override
    public ImmutableList<SourceMethodSignatureDescriptor<TestContext>> getMethodSignatureDescriptors() {
        return signatureDescriptors;
    }
}