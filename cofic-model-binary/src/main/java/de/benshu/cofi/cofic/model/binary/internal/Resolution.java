package de.benshu.cofi.cofic.model.binary.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.cofic.model.binary.BinaryMemberDeclaration;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.streams.Collectors.list;

public class Resolution {
    public static UnboundTypeParameterList resolve(Ancestry ancestry, TypeParameterListReference reference) {
        return reference.resolve(extractTypeReferenceContextFrom(ancestry));
    }

    public static ImmutableList<UnboundType> resolveAll(Ancestry ancestry, ImmutableList<TypeReference> references) {
        return references.stream()
                .map(r -> resolve(ancestry, r))
                .collect(list());
    }

    public static UnboundType resolve(Ancestry ancestry, TypeReference reference) {
        return reference.resolve(extractTypeReferenceContextFrom(ancestry));
    }

    private static TypeReferenceContext extractTypeReferenceContextFrom(Ancestry ancestry) {
        final Ancestry.Head<BinaryMemberDeclaration> memberDeclarationAncestry = ancestry.beginningWith(BinaryMemberDeclaration.class).get();
        final BinaryMemberDeclaration memberDeclaration = memberDeclarationAncestry.getParent();
        final Optional<BinaryMemberDeclaration> outerMemberDeclaration = memberDeclarationAncestry.getGrandAncestry().closest(BinaryMemberDeclaration.class);

        return new TypeReferenceContext() {
            @Override
            public <X extends BinaryModelContext<X>> Optional<AbstractConstraints<X>> getOuterConstraints(X context) {
                return outerMemberDeclaration.map(d -> d.bindTypeParameters(context).getConstraints());
            }

            @Override
            public <X extends BinaryModelContext<X>> AbstractConstraints<X> getConstraints(X context) {
                return memberDeclaration.bindTypeParameters(context).getConstraints();
            }
        };
    }
}
