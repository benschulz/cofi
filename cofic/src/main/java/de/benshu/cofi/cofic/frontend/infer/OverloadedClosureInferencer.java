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

public class OverloadedClosureInferencer implements OverloadedExpressionInferencer {
    private final InferClosure closure;

    public OverloadedClosureInferencer(InferClosure closure) {
        this.closure = closure;
    }

    @Override
    public Iterable<ExpressionInferencer> unoverload() {
        return ImmutableList.<ExpressionInferencer>of(new Unoverloaded(closure));
    }

    private static class Unoverloaded implements ExpressionInferencer {
        private final InferClosure closure;

        public Unoverloaded(InferClosure closure) {
            this.closure = closure;
        }

        @Override
        public int getTypeArgCount() {
            return 0;
        }

        @Override
        public Optional<Parametrization<Pass>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset, final AbstractConstraints<Pass> constraints,
                                                            final ProperTypeMixin<Pass, ?> context) {
            Parametrization<Pass> p = new Parametrization<Pass>() {
                @Override
                public ProperTypeMixin<Pass, ?> getExplicitType() {
                    return context;
                }

                @Override
                public AbstractConstraints<Pass> getConstraints() {
                    return constraints;
                }

                @Override
                public void apply(Substitutions<Pass> substitutions) {
                    closure.setSignature(getImplicitType().substitute(substitutions));
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
