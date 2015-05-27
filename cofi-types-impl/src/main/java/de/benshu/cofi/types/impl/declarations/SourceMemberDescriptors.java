package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class SourceMemberDescriptors<X extends TypeSystemContext<X>> {
    public static <X extends TypeSystemContext<X>> SourceMemberDescriptors<X> empty() {
        return new SourceMemberDescriptors<X>(AbstractConstraints.none(), ImmutableSet.of());
    }

    public static <X extends TypeSystemContext<X>> SourceMemberDescriptors<X> create(ImmutableSet<SourceMemberDescriptor<X>> descriptors) {
        return new SourceMemberDescriptors<>(AbstractConstraints.<X>none(), descriptors);
    }

    public static <X extends TypeSystemContext<X>> SourceMemberDescriptors<X> create(AbstractConstraints<X> contextualConstraints, ImmutableSet<SourceMemberDescriptor<X>> descriptors) {
        return new SourceMemberDescriptors<>(contextualConstraints, descriptors);
    }

    private final AbstractConstraints<X> contextualConstraints;
    private final ImmutableSet<SourceMemberDescriptor<X>> descriptors;

    public SourceMemberDescriptors(AbstractConstraints<X> contextualConstraints, ImmutableSet<SourceMemberDescriptor<X>> descriptors) {
        this.contextualConstraints = contextualConstraints;
        this.descriptors = descriptors;
    }

    public AbstractConstraints<X> getContextualConstraints() {
        return contextualConstraints;
    }

    public ImmutableSet<SourceMemberDescriptor<X>> getDescriptors() {
        return descriptors;
    }
}
