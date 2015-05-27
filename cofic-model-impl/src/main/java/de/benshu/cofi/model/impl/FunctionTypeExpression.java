package de.benshu.cofi.model.impl;

import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import static com.google.common.base.Preconditions.checkState;

public class FunctionTypeExpression<X extends ModelContext<X>> extends TypeExpression<X> {
    public static <X extends ModelContext<X>> FunctionTypeExpression<X> of(ModelNodeMixin<X> parent, TypeExpression<X> in, TypeExpression<X> out) {
        return new FunctionTypeExpression<>(in, out);
    }

    public final TypeExpression<X> in;
    public final TypeExpression<X> out;

    private FunctionTypeExpression(TypeExpression<X> in, TypeExpression<X> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitFunctionType(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends E> T accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformFunctionType(this);
    }
}
