package de.benshu.cofi.types.impl.declarations;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.tags.IndividualTags;

public interface SourceMethodSignatureDescriptor<X extends TypeSystemContext<X>> {
    TypeParameterListImpl<X> getTypeParameters();

    ImmutableList<ImmutableList<SourceType<X>>> getParameterTypes();

    SourceType<X> getReturnType();

    default IndividualTags getTags() {
        return IndividualTags.empty();
    }
}
