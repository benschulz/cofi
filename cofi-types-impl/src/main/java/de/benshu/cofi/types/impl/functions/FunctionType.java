package de.benshu.cofi.types.impl.functions;

import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.commons.core.Optional;

public class FunctionType<X extends TypeSystemContext<X>> {
    public static <X extends TypeSystemContext<X>> FunctionType<X> construct(
            X context,
            AbstractTypeList<X, ProperTypeMixin<X, ?>> paramTypes,
            ProperTypeMixin<X, ?> returnType) {

        final TemplateTypeImpl<X> constructed = context.getTypeSystem()
                .getFunction(paramTypes.size())
                .apply(paramTypes.append(returnType));

        return new FunctionType<>(constructed, paramTypes, returnType);
    }


    public static <X extends TypeSystemContext<X>> FunctionType<X> from(X context, TemplateTypeImpl<X> templateType) {
        return tryFrom(context, templateType).get();
    }

    public static <X extends TypeSystemContext<X>> FunctionType<X> forceFrom(X context, TypeMixin<X, ?> templateType) {
        return tryFrom(context, templateType).get();
    }

    public static <X extends TypeSystemContext<X>> Optional<FunctionType<X>> tryFrom(X context, TypeMixin<X, ?> type) {
        return interpretAsFunctionType(context, type)
                .map(t -> {
                    final int arity = t.getArguments().size() - 1;

                    AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes = (AbstractTypeList<X, ProperTypeMixin<X, ?>>) t.getArguments().subList(0, arity);
                    ProperTypeMixin<X, ?> returnType = (ProperTypeMixin<X, ?>) t.getArguments().get(arity);

                    return new FunctionType<>(t, parameterTypes, returnType);
                });
    }

    private static <X extends TypeSystemContext<X>> Optional<TemplateTypeImpl<X>> interpretAsFunctionType(X context, TypeMixin<X, ?> type) {
        if (!(type instanceof TemplateTypeImpl<?>))
            return Optional.none();

        final TemplateTypeImpl<X> templateType = (TemplateTypeImpl<X>) type;
        final AbstractTemplateTypeConstructor<X> functionConstructor = context.getTypeSystem().getFunctionOrNull(templateType.getArguments().size() - 1);

        return templateType.getConstructor().isSameAs(functionConstructor)
                ? Optional.some(templateType)
                : Optional.none();
    }

    private final TemplateTypeImpl<X> templateType;
    private final AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes;
    private final ProperTypeMixin<X, ?> returnType;

    public FunctionType(TemplateTypeImpl<X> templateType, AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes, ProperTypeMixin<X, ?> returnType) {
        this.templateType = templateType;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public TemplateTypeImpl<X> asTemplateType() {
        return templateType;
    }

    public AbstractTypeList<X, ProperTypeMixin<X, ?>> getParameterTypes() {
        return parameterTypes;
    }

    public ProperTypeMixin<X, ?> getReturnType() {
        return returnType;
    }

    public Optional<FunctionType<X>> tryGetReturnTypeAsFunction(X context) {
        return tryFrom(context, getReturnType());
    }
}
