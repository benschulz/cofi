package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.parser.AstNodeConstructorMethod;

public class Closure<X extends ModelContext<X>> extends ExpressionNode<X> {
    public static final class Case<X extends ModelContext<X>> extends AbstractModelNode<X> {
        @AstNodeConstructorMethod
        public static <X extends ModelContext<X>> Case<X> of(ImmutableList<ParameterImpl<X>> params, ImmutableList<Statement<X>> body) {
            return new Case<>(params, body);
        }

        public final ImmutableList<ParameterImpl<X>> params;
        public final ImmutableList<Statement<X>> body;

        private Case(ImmutableList<ParameterImpl<X>> params, ImmutableList<Statement<X>> body) {
            this.params = params;
            this.body = body;
        }

        @Override
        public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
            return visitor.visitClosureCase(this, aggregate);
        }

        @Override
        public <N, L extends N, D extends L, S extends N, E extends N, T extends N> N accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
            return transformer.transformClosureCase(this);
        }
    }

    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> Closure<X> of(ImmutableList<Case<X>> cases) {
        return new Closure<>(cases);
    }

    public final ImmutableList<Case<X>> cases;

    private Closure(ImmutableList<Case<X>> cases) {
        this.cases = cases;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitClosure(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> E accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformClosure(this);
    }
}
