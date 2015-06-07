package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

public class BinaryClass extends AbstractBinaryTypeDeclaration {
    BinaryClass(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            Constructor<BinaryTypeBody> body) {
        super(ancestry, annotations, name, typeParameters, body);
    }

    @Override
    public <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        throw null;
    }
}
