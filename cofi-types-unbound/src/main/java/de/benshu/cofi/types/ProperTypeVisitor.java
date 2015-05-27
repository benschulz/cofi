package de.benshu.cofi.types;

public interface ProperTypeVisitor<R> {

    R visitBottomType(Bottom bottom);

    R visitErrorType(Error error);

    R visitIntersectionType(IntersectionType intersectionType);

    R visitTemplateType(TemplateType templateType);

    R visitUnionType(UnionType unionType);

    R visitTypeVariable(ProperTypeVariable properTypeVariable);

}
