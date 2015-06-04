package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.impl.FunctionTypes;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.some;

public class OverloadedClosureInferencer<T> implements OverloadedExpressionInferencer<T> {
    private final InferClosure<T> closure;

    public OverloadedClosureInferencer(InferClosure<T> closure) {
        this.closure = closure;
    }

    @Override
    public Iterable<ExpressionInferencer<T>> unoverload() {
        return ImmutableList.<ExpressionInferencer<T>>of(new Unoverloaded<T>(closure));
    }

    private static class Unoverloaded<T> implements ExpressionInferencer<T> {
        private final InferClosure<T> closure;

        public Unoverloaded(InferClosure<T> closure) {
            this.closure = closure;
        }

        @Override
        public int getTypeArgCount() {
            return 0;
        }

        @Override
        public Optional<Parametrization<Pass, T>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset, final AbstractConstraints<Pass> constraints,
                                                               final ProperTypeMixin<Pass, ?> context) {
            Parametrization<Pass, T> p = new Parametrization<Pass, T>() {
                @Override
                public ProperTypeMixin<Pass, ?> getExplicitType() {
                    return context;
                }

                @Override
                public AbstractConstraints<Pass> getConstraints() {
                    return constraints;
                }

                @Override
                public T apply(Substitutions<Pass> substitutions, T aggregate) {
                    return closure.setSignature(getImplicitType().substitute(substitutions), aggregate);
                }
            };
            return some(p);
        }

        @Override
        public Optional<ProperTypeMixin<Pass, ?>> inferSpecific(Pass pass) {
            return some(FunctionTypes.construct(pass, closure.getParameterTypes(), pass.getTypeSystem().getBottom()));
        }
    }
}
