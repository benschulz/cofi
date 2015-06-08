package de.benshu.cofi.runtime.internal;

import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.MemoizingSupplier;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.types.Constraints;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.commons.core.Optional;

import java.util.function.Supplier;

public class Resolution {
    public static Supplier<TypeParameterList> resolve(Ancestry ancestry, TypeParameterListReference reference) {
        return MemoizingSupplier.of(() -> reference.resolve(extractTypeReferenceContextFrom(ancestry)));
    }

    public static <T extends Type> Supplier<T> resolve(Ancestry ancestry, TypeReference<T> reference) {
        return MemoizingSupplier.of(() -> resolveImmediately(ancestry, reference));
    }

    public static <T extends Type> T resolveImmediately(Ancestry ancestry, TypeReference<? extends T> reference) {
        return reference.resolve(extractTypeReferenceContextFrom(ancestry));
    }

    public static TypeReferenceContext extractTypeReferenceContextFrom(Ancestry ancestry) {
        final Ancestry.Head<MemberDeclaration> memberDeclarationAncestry = ancestry.beginningWith(MemberDeclaration.class).get();
        final MemberDeclaration memberDeclaration = memberDeclarationAncestry.getParent();
        final Optional<MemberDeclaration> outerMemberDeclaration = memberDeclarationAncestry.getGrandAncestry().closest(MemberDeclaration.class);

        return new TypeReferenceContext() {
            @Override
            public Optional<Constraints> getOuterConstraints() {
                return outerMemberDeclaration.map(d -> d.getTypeParameters().getConstraints());
            }

            @Override
            public Constraints getConstraints() {
                return memberDeclaration.getTypeParameters().getConstraints();
            }
        };
    }
}
