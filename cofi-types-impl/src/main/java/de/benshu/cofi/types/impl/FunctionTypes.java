package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import static com.google.common.base.Preconditions.*;

public class FunctionTypes {
    public static <X extends TypeSystemContext<X>> AbstractTypeList<X, ProperTypeMixin<X, ?>> extractParamTypes(X context, ConstructedTypeMixin<X, ?, ?> functionType) {
        int arity = checkArgumentToBeFunctionType(context, functionType);
        @SuppressWarnings("unchecked")
        AbstractTypeList<X, ProperTypeMixin<X, ?>> parameters = (AbstractTypeList<X, ProperTypeMixin<X, ?>>) functionType.getArguments().subList(0, arity);
        return parameters;
    }

    public static <X extends TypeSystemContext<X>> AbstractTypeList<X, ProperTypeMixin<X, ?>> extractParamTypes(X context, ProperTypeMixin<X, ?> functionType) {
        return extractParamTypes(context, (ConstructedTypeMixin<X, ?, ?>) (Object) functionType);
    }

    private static <X extends TypeSystemContext<X>> ProperTypeMixin<X, ?> extractReturnType(X context, ConstructedTypeMixin<X, ?, ?> functionType) {
        int arity = checkArgumentToBeFunctionType(context, functionType);
        return (ProperTypeMixin<X, ?>) functionType.getArguments().get(arity);
    }

    public static <X extends TypeSystemContext<X>> ProperTypeMixin<X, ?> extractReturnType(X context, ProperTypeMixin<X, ?> functionType) {
        ConstructedTypeMixin<X, ?, ?> ft = (ConstructedTypeMixin<X, ?, ?>) (Object) functionType;
        return FunctionTypes.extractReturnType(context, ft);
    }

    public static <X extends TypeSystemContext<X>> TemplateTypeImpl<X> construct(X context, AbstractTypeList<X, ProperTypeMixin<X, ?>> paramTypes, ProperTypeMixin<X, ?> returnType) {
        return context.getTypeSystem()
                .getFunction(paramTypes.size())
                .apply(paramTypes.append(returnType));
    }

    private static <X extends TypeSystemContext<X>> int checkArgumentToBeFunctionType(X context, ConstructedTypeMixin<X, ?, ?> functionType) {
        int arity = functionType.getArguments().size() - 1;
        checkArgument(functionType.getConstructor() instanceof AbstractTemplateTypeConstructor<?>
                && ((AbstractTemplateTypeConstructor<?>)functionType.getConstructor()).getOriginal() == context.getTypeSystem().getFunction(arity).getOriginal(),
                functionType.getConstructor());
        return arity;
    }
}
