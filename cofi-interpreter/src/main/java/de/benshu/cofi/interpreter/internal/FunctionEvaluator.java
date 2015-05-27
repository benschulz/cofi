package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.benshu.cofi.interpreter.internal.special.SpecialFunctionEvaluator;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.Parameter;
import de.benshu.cofi.runtime.VariableDeclaration;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TypeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.map;

public interface FunctionEvaluator {
    static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        final Utilities util = moduleInterpretation.util;

        final boolean runtimeImplemented = method.getAnnotations().stream()
                .anyMatch(a -> util.isExactType(a.getType(), "cofi", "lang", "RuntimeImplemented"));

        FunctionEvaluator result = runtimeImplemented
                ? SpecialFunctionEvaluator.forMethod(moduleInterpretation, receiver, method)
                : RegularFunctionEvaluator.forMethod(moduleInterpretation, receiver, method);

        for (MethodDeclaration.Piece piece : method.getPieces().subList(0, method.getPieces().size() - 1)) {
            result = result.curry(piece.getParameters());
        }

        return result;
    }

    static ImmutableMap<VariableDeclaration, CofiObject> collectArguments(
            Utilities util,
            ImmutableList<? extends VariableDeclaration> variables,
            ImmutableList<CofiObject> arguments) {

        return IntStream.range(0, variables.size())
                .mapToObj(i -> Maps.immutableEntry(
                        variables.get(i),
                        util.createField(variables.get(i).getValueType(), arguments.get(i))
                ))
                .collect(map());
    }

    TemplateType getSignature();

    default TypeList<ProperType> getParameterTypes() {
        final TypeList<?> functionTypeArguments = getSignature().getArguments();
        return (TypeList<ProperType>) functionTypeArguments.subList(0, functionTypeArguments.size() - 1);
    }

    default ProperType getReturnType() {
        final TypeList<?> functionTypeArguments = getSignature().getArguments();
        return (ProperType) functionTypeArguments.get(functionTypeArguments.size() - 1);
    }

    CofiObject evaluate(ImmutableList<CofiObject> arguments);

    default FunctionEvaluator curry(ImmutableList<Parameter> parameters) {
        checkArgument(parameters.size() == getParameterTypes().size());

        final FunctionEvaluator uncurried = this;
        final TemplateType curriedSignature = (TemplateType) getReturnType();

        return new FunctionEvaluator() {
            @Override
            public TemplateType getSignature() {
                return uncurried.getSignature();
            }

            @Override
            public CofiObject evaluate(ImmutableList<CofiObject> firstArguments) {
                final CofiObject result = new CofiObject(uncurried.getSignature());

                result.getSpecialValues().putInstance(FunctionEvaluator.class, new FunctionEvaluator() {
                    @Override
                    public TemplateType getSignature() {
                        return curriedSignature;
                    }

                    @Override
                    public CofiObject evaluate(ImmutableList<CofiObject> remainingArguments) {
                        return uncurried.evaluate(Stream.concat(firstArguments.stream(), remainingArguments.stream()).collect(list()));
                    }
                });

                return result;
            }
        };
    }
}
