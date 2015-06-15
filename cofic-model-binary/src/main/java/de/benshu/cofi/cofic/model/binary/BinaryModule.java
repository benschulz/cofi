package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.BinaryModuleMixin;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.common.Fqn;

public class BinaryModule extends AbstractBinaryModuleOrPackage implements BinaryModuleMixin {
    public BinaryModule(
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            Fqn fqn,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body,
            ImmutableSet<Constructor<AbstractBinaryTypeDeclaration>> topLevelDeclarations,
            ImmutableSet<Constructor<BinaryPackage>> subpackages) {
        super(Ancestry.empty(), annotations, fqn, typeParameters, supertypes, body, topLevelDeclarations, subpackages);
    }
}
