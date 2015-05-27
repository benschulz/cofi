package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.Optional;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public class OverloadedSimpleValueInferencer implements OverloadedExpressionInferencer {
    private final ProperTypeMixin<Pass, ?> type;

    public OverloadedSimpleValueInferencer(ProperTypeMixin<Pass, ?> type) {
        this.type = type;
    }

    @Override
    public Iterable<ExpressionInferencer> unoverload() {
        return ImmutableList.<ExpressionInferencer>of(new Unoverloaded(type));
    }

    @Override
    public String toString() {
        return type.toString();
    }

    private static class Unoverloaded extends AbstractExpressionInferencer {
        private final ProperTypeMixin<Pass, ?> type;

        public Unoverloaded(ProperTypeMixin<Pass, ?> type) {
            super(0);
            this.type = type;
        }

        @Override
        public Optional<Parametrization<Pass>> inferGeneric(Pass pass, final TypeParameterListImpl<Pass> params, int offset,
                                                            AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context) {
            final AbstractConstraints<Pass> cs = constraints.establishSubtype(type, context);

            if (cs.isAll()) {
                return none();
            } else {
                Parametrization<Pass> parametrization = new Parametrization<Pass>() {
                    @Override
                    public AbstractConstraints<Pass> getConstraints() {
                        return cs;
                    }

                    public void apply(Substitutions<Pass> substitutions) {
                    }

                    @Override
                    public ProperTypeMixin<Pass, ?> getExplicitType() {
                        return type;
                    }
                };
                return some(parametrization);
            }
        }

        @Override
        Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
            return Optional.<ProperTypeMixin<Pass, ?>>some(type);
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }
}
