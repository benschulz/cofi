package de.benshu.cofi.types.bound;

public interface ProperTypeVisitor<X, T> {

    T visitBottomType(Bottom<X, ?> bottom);

    T visitErrorType(ErrorType<X, ?> errorType);

    T visitIntersectionType(IntersectionType<X, ?> intersectionType);

    T visitTemplateType(TemplateType<X, ?, ?> templateType);

    T visitUnionType(UnionType<X, ?> unionType);

    T visitTypeVariable(ProperTypeVariable<X, ?> properTypeVariable);

}
