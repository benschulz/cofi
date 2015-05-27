package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;

public class InterpretedMemberDescriptors<X extends TypeSystemContext<X>> {
    public static <X extends TypeSystemContext<X>> InterpretedMemberDescriptors<X> empty() {
        return new InterpretedMemberDescriptors<X>(AbstractConstraints.none(), ImmutableSet.of());
    }

    private final AbstractConstraints<X> context;
    private final ImmutableSet<InterpretedMemberDescriptor<X>> descriptors;

    public InterpretedMemberDescriptors(AbstractConstraints<X> context, ImmutableSet<InterpretedMemberDescriptor<X>> descriptors) {
        this.context = context;
        this.descriptors = descriptors;
    }

    public AbstractConstraints<X> getContext() {
        return context;
    }

    public ImmutableSet<InterpretedMemberDescriptor<X>> getDescriptors() {
        return descriptors;
    }
}
