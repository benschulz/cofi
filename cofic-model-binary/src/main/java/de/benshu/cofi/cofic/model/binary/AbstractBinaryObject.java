package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.binary.internal.UnboundTypeList;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveList;

public abstract class AbstractBinaryObject extends AbstractBinaryTypeDeclaration {
    private final UnboundTypeList supertypes;

    AbstractBinaryObject(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body) {
        super(ancestry, annotations, name, typeParameters, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.supertypes = resolveList(ancestryIncludingMe, supertypes);
    }

    @Override
    public <X extends TypeSystemContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        throw null;
    }
}
