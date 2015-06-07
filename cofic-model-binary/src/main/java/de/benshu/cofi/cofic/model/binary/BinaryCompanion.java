package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.AbstractBinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.types.tags.IndividualTags;

public abstract class BinaryCompanion extends AbstractBinaryObject {
    private final BinaryTypeDeclaration accompanied;

    BinaryCompanion(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body) {
        super(ancestry, annotations, name, "\u262F", typeParameters, supertypes, body);

        accompanied = ancestry.closest(BinaryTypeDeclaration.class).get();
    }

    @Override
    protected <X extends BinaryModelContext<X>> IndividualTags individualTags(X context) {
        return IndividualTags.of(AbstractBinaryModelContext.ACCOMPANIED_TAG, accompanied.<X>bind(context));
    }
}
