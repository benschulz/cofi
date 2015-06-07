package de.benshu.cofi.cofic.model.binary.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.binary.deserialization.internal.TypeReferenceContext;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.cofic.model.binary.BinaryMemberDeclaration;
import de.benshu.cofi.types.Constraints;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;

public class Resolution {
    public static UnboundTypeParameterList resolve(Ancestry ancestry, TypeParameterListReference reference) {
        return reference.resolve(extractTypeReferenceContextFrom(ancestry));
    }

    public static UnboundTypeList resolveList(Ancestry ancestry, ImmutableList<TypeReference> references) {
        return new UnboundTypeList() {
            @Override
            public <X extends TypeSystemContext<X>> AbstractTypeList<X, ?> bind(X context) {
                return references.stream()
                        .map(r -> resolve(ancestry, r).bind(context))
                        .collect(typeList());
            }
        };
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
            public <X extends TypeSystemContext<X>> Optional<AbstractConstraints<X>> getOuterConstraints(X context) {
                return outerMemberDeclaration.map(d -> d.getTypeParameters(context).getConstraints());
            }

            @Override
            public <X extends TypeSystemContext<X>> AbstractConstraints<X> getConstraints(X context) {
                return memberDeclaration.getTypeParameters(context).getConstraints();
            }
        };
    }
}
