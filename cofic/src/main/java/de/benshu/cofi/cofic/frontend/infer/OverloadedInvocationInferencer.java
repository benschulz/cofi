package de.benshu.cofi.cofic.frontend.infer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.inference.Parametrization;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.FunctionTypes;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;

import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;
import static java.util.stream.Collectors.joining;

public class OverloadedInvocationInferencer implements OverloadedExpressionInferencer {
    private final Pass pass;
    private final OverloadedExpressionInferencer primary;
    private final InferFunctionInvocation functionInvocation;
    private ImmutableList<OverloadedExpressionInferencer> argInferencers;

    public OverloadedInvocationInferencer(Pass pass, OverloadedExpressionInferencer primary,
                                          InferFunctionInvocation functionInvocation) {
        this.pass = pass;
        this.primary = primary;
        this.functionInvocation = functionInvocation;
    }

    public void setArgs(ImmutableList<OverloadedExpressionInferencer> argInferencers) {
        Preconditions.checkState(this.argInferencers == null);
        this.argInferencers = argInferencers;
    }

    @Override
    public Iterable<ExpressionInferencer> unoverload() {
        Preconditions.checkState(argInferencers != null);

        return new Iterable<ExpressionInferencer>() {
            @Override
            public Iterator<ExpressionInferencer> iterator() {
                return new AbstractIterator<ExpressionInferencer>() {
                    private final int count = argInferencers.size() + 1;
                    private final ImmutableList<Iterable<ExpressionInferencer>> iterables = getSubIterables();
                    private final Deque<Iterator<ExpressionInferencer>> iterators = new ArrayDeque<>(count);
                    private final Deque<ExpressionInferencer> inferencers = new ArrayDeque<>(count);

                    {
                        while (iterators.size() < count) {
                            Iterator<ExpressionInferencer> iterator = iterables.get(iterators.size()).iterator();

                            if (!iterator.hasNext())
                                break; // TODO when does this ever happen? if it does not, we can use refill to fill initially

                            iterators.push(iterator);
                            inferencers.addLast(iterator.next());
                        }
                    }

                    @Override
                    protected ExpressionInferencer computeNext() {
                        try {
                            if (inferencers.size() == count) {
                                return new Unoverloaded(pass, ImmutableList.copyOf(inferencers));
                            } else {
                                return endOfData();
                            }
                        } finally {
                            nextCombination();
                        }
                    }

                    private void nextCombination() {
                        if (popExhausted()) {
                            inferencers.removeLast();
                            refill();
                        }
                    }

                    /**
                     * @return true if another combination exists
                     */
                    private boolean popExhausted() {
                        while (!iterators.isEmpty() && !iterators.peek().hasNext()) {
                            iterators.pop();
                            inferencers.removeLast();
                        }

                        return !iterators.isEmpty();
                    }

                    private void refill() {
                        inferencers.addLast(iterators.peek().next());
                        while (iterators.size() < count) {
                            iterators.push(iterables.get(iterators.size()).iterator());
                            inferencers.addLast(iterators.peek().next());
                        }
                    }
                };
            }
        };
    }

    private ImmutableList<Iterable<ExpressionInferencer>> getSubIterables() {
        ImmutableList.Builder<Iterable<ExpressionInferencer>> builder = ImmutableList.builder();

        builder.add(unoverloadPrimary());
        for (OverloadedExpressionInferencer argInferencer : argInferencers) {
            builder.add(argInferencer.unoverload());
        }

        return builder.build();
    }

    private Iterable<ExpressionInferencer> unoverloadPrimary() {
        return Iterables.concat(Iterables.transform(primary.unoverload(),
                new Function<ExpressionInferencer, Iterable<ExpressionInferencer>>() {
                    @Override
                    public Iterable<ExpressionInferencer> apply(ExpressionInferencer primaryInferencer) {
                        for (ProperTypeMixin<Pass, ?> pst : primaryInferencer.inferSpecific(pass)) {
                            // TODO there should be a visitor which extracts the type and yields an intersection of functions
                            //      for unions it should simply use the _order_ of the first element
                            //      (if it's a union of intersections; also, normalize beforehand)
                            if (pst instanceof TemplateTypeImpl<?>) {
                                return templateTypePrimary(primaryInferencer, (TemplateTypeImpl<Pass>) pst);
                            } else if (pst instanceof AbstractIntersectionType<?, ?>) {
                                return intersectionTypePrimary(primaryInferencer, (AbstractIntersectionType<Pass, ?>) pst);
                            } else {
                                throw null;
                            }
                        }

                        return ImmutableList.of();
                    }

                    private Iterable<ExpressionInferencer> templateTypePrimary(final ExpressionInferencer primaryInferencer,
                                                                               final TemplateTypeImpl<Pass> pst) {
                        ExpressionInferencer wrapped = new AbstractExpressionInferencer(primaryInferencer.getTypeArgCount()) {
                            @Override
                            public Optional<Parametrization<Pass>> inferGeneric(
                                    Pass pass, TypeParameterListImpl<Pass> params, int offset,
                                    AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context) {
                                for (final Parametrization<Pass> p : primaryInferencer.inferGeneric(pass, params, offset, constraints, context)) {
                                    Parametrization<Pass> wrapped = new Parametrization<Pass>() {
                                        @Override
                                        public ProperTypeMixin<Pass, ?> getExplicitType() {
                                            return p.getExplicitType();
                                        }

                                        @Override
                                        public ProperTypeMixin<Pass, ?> getImplicitType() {
                                            return p.getImplicitType();
                                        }

                                        @Override
                                        public AbstractConstraints<Pass> getConstraints() {
                                            return p.getConstraints();
                                        }

                                        @Override
                                        public void apply(Substitutions<Pass> substitutions) {
                                            functionInvocation.setSignature(
                                                    -1,
                                                    getExplicitType().substitute(substitutions),
                                                    getImplicitType().substitute(substitutions));
                                            p.apply(substitutions);
                                        }
                                    };
                                    return some(wrapped);
                                }

                                return none();
                            }

                            @Override
                            Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
                                return Optional.<ProperTypeMixin<Pass, ?>>some(pst);
                            }

                            @Override
                            public String toString() {
                                return primaryInferencer.toString();
                            }
                        };

                        return ImmutableList.of(wrapped);
                    }

                    private Iterable<ExpressionInferencer> intersectionTypePrimary(final ExpressionInferencer primaryInferencer,
                                                                                   final AbstractIntersectionType<Pass, ?> pst) {
                        ImmutableList.Builder<ExpressionInferencer> builder = ImmutableList.builder();

                        for (int i = 0; i < pst.getElements().size(); ++i) {
                            final int index = i;

                            ExpressionInferencer wrapped = new AbstractExpressionInferencer(primaryInferencer.getTypeArgCount()) {
                                @Override
                                public Optional<Parametrization<Pass>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset,
                                                                                    AbstractConstraints<Pass> constraints, ProperTypeMixin<Pass, ?> context) {
                                    for (final Parametrization<Pass> p : primaryInferencer.inferGeneric(pass, params, offset, constraints, context)) {
                                        Parametrization<Pass> wrapped = new Parametrization<Pass>() {
                                            @Override
                                            public ProperTypeMixin<Pass, ?> getImplicitType() {
                                                return ((AbstractIntersectionType<Pass, ?>) p.getImplicitType()).getElements().get(index);
                                            }

                                            @Override
                                            public ProperTypeMixin<Pass, ?> getExplicitType() {
                                                return ((AbstractIntersectionType<Pass, ?>) p.getExplicitType()).getElements().get(index);
                                            }

                                            @Override
                                            public AbstractConstraints<Pass> getConstraints() {
                                                return p.getConstraints();
                                            }

                                            @Override
                                            public void apply(Substitutions<Pass> substitutions) {
                                                functionInvocation.setSignature(
                                                        index,
                                                        getExplicitType().substitute(substitutions),
                                                        getImplicitType().substitute(substitutions));

                                                p.apply(substitutions);
                                            }
                                        };
                                        return some(wrapped);
                                    }

                                    return none();
                                }

                                @Override
                                Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
                                    ProperTypeMixin<Pass, ?> type = pst.getElements().get(index);
                                    return Optional.<ProperTypeMixin<Pass, ?>>some(type);
                                }

                                @Override
                                public String toString() {
                                    return primaryInferencer.toString();
                                }
                            };

                            builder.add(wrapped);
                        }

                        return builder.build();
                    }
                }));
    }

    @Override
    public String toString() {
        final String primary = this.primary.toString();
        final ImmutableList<String> args = argInferencers.stream().map(Object::toString).collect(Collector.of(
                ImmutableList::<String>builder,
                ImmutableList.Builder::add,
                (left, right) -> left.addAll(right.build()),
                ImmutableList.Builder::build));

        return primary + "(" + args.stream().collect(joining(", ")) + ")";
    }

    private static class Unoverloaded extends AbstractExpressionInferencer {
        private static int determineTypeArgCount(ImmutableList<ExpressionInferencer> subInferencers) {
            int total = 0;
            for (ExpressionInferencer si : subInferencers) {
                total += si.getTypeArgCount();
            }
            return total;
        }

        private final Pass pass;
        private final ImmutableList<ExpressionInferencer> subInferencers;

        public Unoverloaded(Pass pass, ImmutableList<ExpressionInferencer> subInferencers) {
            super(determineTypeArgCount(subInferencers));
            this.pass = pass;
            this.subInferencers = subInferencers;
        }

        @Override
        public Optional<Parametrization<Pass>> inferGeneric(Pass pass, TypeParameterListImpl<Pass> params, int offset, AbstractConstraints<Pass> constraints,
                                                            ProperTypeMixin<Pass, ?> context) {
            int arity = subInferencers.size() - 1;
            AbstractTemplateTypeConstructor<Pass> function = pass.getTypeSystem().getFunction(arity);
            TypeMixin<Pass, ?>[] args = new TypeMixin[arity + 1];
            // TODO Function<TupleX<sis[1].specific,...>, context> [X = sis.size()-1]
            Arrays.fill(args, pass.getTypeSystem().getBottom());
            args[arity] = context;
            ProperTypeMixin<Pass, ?> currentContext = function.apply(AbstractTypeList.of(args));

            List<Parametrization<Pass>> parametrizations = new ArrayList<>(subInferencers.size());

            for (ExpressionInferencer si : subInferencers) {
                for (Parametrization<Pass> p : si.inferGeneric(pass, params, offset, constraints, currentContext)) {
                    parametrizations.add(p);

                    if (parametrizations.size() == subInferencers.size()) {
                        return constrainReturn(parametrizations, context);
                    }

                    offset += si.getTypeArgCount();
                    constraints = p.getConstraints();
                    currentContext = extractParamTypes(parametrizations).get(parametrizations.size() - 1);
                }
            }

            return none();
        }

        private Optional<Parametrization<Pass>> constrainReturn(final List<Parametrization<Pass>> parametrizations,
                                                                ProperTypeMixin<Pass, ?> context) {
            final ProperTypeMixin<Pass, ?> returnType = extractReturnType(parametrizations);
            final AbstractConstraints<Pass> constraints = parametrizations.get(parametrizations.size() - 1).getConstraints()
                    .establishSubtype(returnType, context);

            if (constraints.isAll()) {
                return none();
            }

            Parametrization<Pass> parametrization = new Parametrization<Pass>() {
                @Override
                public ProperTypeMixin<Pass, ?> getExplicitType() {
                    return returnType;
                }

                @Override
                public AbstractConstraints<Pass> getConstraints() {
                    return constraints;
                }

                @Override
                public void apply(Substitutions<Pass> substitutions) {
                    for (Parametrization<Pass> p : parametrizations) {
                        p.apply(substitutions);
                    }
                }
            };
            return some(parametrization);
        }

        private AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> extractParamTypes(List<Parametrization<Pass>> parametrizations) {
            return FunctionTypes.extractParamTypes(pass, getPrimaryType(parametrizations));
        }

        private ProperTypeMixin<Pass, ?> extractReturnType(List<Parametrization<Pass>> parametrizations) {
            return FunctionTypes.extractReturnType(pass, getPrimaryType(parametrizations));
        }

        private ProperTypeMixin<Pass, ?> getPrimaryType(List<Parametrization<Pass>> parametrizations) {
            return parametrizations.get(0).getImplicitType();
        }

        @Override
        Optional<ProperTypeMixin<Pass, ?>> doInferSpecific(Pass pass) {
            for (ProperTypeMixin<Pass, ?> primarySpecificType : subInferencers.get(0).inferSpecific(pass)) {
                ProperTypeMixin<Pass, ?> returnType = FunctionTypes.extractReturnType(pass, primarySpecificType);
                return Optional.<ProperTypeMixin<Pass, ?>>some(returnType);
            }

            return none();
        }

        @Override
        public String toString() {
            final String primary = subInferencers.get(0).toString();
            final ImmutableList<String> args = subInferencers.subList(1, subInferencers.size()).stream().map(Object::toString).collect(Collector.of(
                    ImmutableList::<String>builder,
                    ImmutableList.Builder::add,
                    (left, right) -> left.addAll(right.build()),
                    ImmutableList.Builder::build));

            return primary + "(" + args.stream().collect(joining(", ")) + ")";
        }
    }
}
