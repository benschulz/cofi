package de.benshu.cofi.types.impl.members;

import de.benshu.cofi.types.impl.FunctionTypes;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;

public class MethodSignatureComparator<X extends TypeSystemContext<X>> {
    private final X context;

    public MethodSignatureComparator(X context) {
        this.context = context;
    }

    public MethodSignatureRelation compare(MethodSignatureImpl<X> a, MethodSignatureImpl<X> b) {
        final TemplateTypeConstructorMixin<X> typeA = a.getType();
        final TemplateTypeConstructorMixin<X> typeB = b.getType();

        final AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypesA = FunctionTypes.extractParamTypes(context, typeA.applyTrivially());
        final AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypesB = FunctionTypes.extractParamTypes(context, typeB.applyTrivially());

        final TypeParameterListImpl<X> typeParametersA = typeA.getParameters();
        final TypeParameterListImpl<X> typeParametersB = typeB.getParameters();

        final AbstractConstraints<X> constraintsA = typeParametersA.getConstraints();
        final AbstractConstraints<X> constraintsB = typeParametersB.getConstraints();

        Substitutions<X> substitutionsA = Substitutions.ofThrough(typeParametersA, typeParametersA.getVariables());
        Substitutions<X> substitutionsB = Substitutions.ofThrough(typeParametersB, typeParametersB.getVariables());

        final boolean isSubSignature = constraintsB.checkBounds(context, constraintsA, substitutionsA)
                && constraintsA.areSubtypes(parameterTypesA, parameterTypesB);

        final boolean isSuperSignature = constraintsA.checkBounds(context, constraintsB, substitutionsB)
                && constraintsB.areSubtypes(parameterTypesB, parameterTypesA);

        return isSubSignature && isSuperSignature ? MethodSignatureRelation.EQUAL
                : !isSubSignature && !isSuperSignature ? MethodSignatureRelation.UNRELATED
                : isSubSignature ? MethodSignatureRelation.SUBSIGNATURE : MethodSignatureRelation.SUPERSIGNATURE;
    }
}
