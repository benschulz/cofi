package de.benshu.cofi.runtime;

import de.benshu.commons.core.exception.UnexpectedBranchException;

public interface MemberDeclarationVisitor<R> {
    default R defaultAction(MemberDeclaration memberDeclaration) {
        throw new UnexpectedBranchException(memberDeclaration.getClass().toString());
    }

    default R visit(MemberDeclaration memberDeclaration) {
        return memberDeclaration.accept(this);
    }

    default R visitMethodDeclaration(MethodDeclaration methodDeclaration) {
        return defaultAction(methodDeclaration);
    }

    default R visitPropertyDeclaration(PropertyDeclaration propertyDeclaration) {
        return defaultAction(propertyDeclaration);
    }

    default R visitTypeDeclaration(TypeDeclaration typeDeclaration) {
        return defaultAction(typeDeclaration);
    }
}
