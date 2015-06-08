package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;

public class BinaryPackage extends AbstractBinaryModuleOrPackage {
    public BinaryPackage(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body,
            ImmutableSet<Constructor<AbstractBinaryTypeDeclaration>> topLevelDeclarations,
            ImmutableSet<Constructor<BinaryPackage>> subpackages) {
        super(ancestry, annotations, ancestry.closest(AbstractBinaryModuleOrPackage.class).get().getFqn().getChild(name), typeParameters, supertypes, body, topLevelDeclarations, subpackages);
    }
}
