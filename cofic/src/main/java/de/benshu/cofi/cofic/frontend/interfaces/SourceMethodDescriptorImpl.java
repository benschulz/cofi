package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.tags.IndividualTags;

final class SourceMethodDescriptorImpl implements SourceMethodDescriptor<Pass> {
    private final String name;
    private final ImmutableList<SourceMethodSignatureDescriptor<Pass>> sigDescs;
    private final IndividualTags tags;

    public SourceMethodDescriptorImpl(String name, ImmutableSet<SourceMethodSignatureDescriptorImpl> sigDescs, IndividualTags tags) {
        this.name = name;
        // TODO FIXME think about the impact of set vs list (should (only?) affect determinism of type inference)
        this.sigDescs = ImmutableList.copyOf(sigDescs);
        this.tags = tags;
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.METHOD;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IndividualTags getTags(Pass context) {
        return tags;
    }

    @Override
    public ImmutableList<SourceMethodSignatureDescriptor<Pass>> getMethodSignatureDescriptors() {
        return sigDescs;
    }
}
