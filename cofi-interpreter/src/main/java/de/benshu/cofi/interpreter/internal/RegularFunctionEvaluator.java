package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.runtime.LocalVariableDeclaration;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.Parameter;
import de.benshu.cofi.runtime.Statement;
import de.benshu.cofi.types.TemplateType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.cofi.interpreter.internal.FunctionEvaluator.collectArguments;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.map;

public class RegularFunctionEvaluator implements FunctionEvaluator {
    static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        return new RegularFunctionEvaluator(
                moduleInterpretation,
                method.getSignature().applyTrivially(), // FIXME wrong
                new EvaluationContext(receiver),
                method.getBody().get(),
                method.getPieces().stream()
                        .flatMap(p -> p.getParameters().stream())
                        .collect(list())
        );
    }

    private final ModuleInterpretation moduleInterpretation;
    private final TemplateType signature;
    private final EvaluationContext enclosingEvaluationContext;
    private final ImmutableList<Statement> statements;
    private final ImmutableList<Parameter> parameters;

    public RegularFunctionEvaluator(ModuleInterpretation moduleInterpretation,
                                    TemplateType signature,
                                    EvaluationContext enclosingEvaluationContext,
                                    ImmutableList<Statement> statements,
                                    ImmutableList<Parameter> parameters) {
        this.moduleInterpretation = moduleInterpretation;
        this.signature = signature;
        this.enclosingEvaluationContext = enclosingEvaluationContext;
        this.statements = statements;
        this.parameters = parameters;
    }

    @Override
    public TemplateType getSignature() {
        return signature;
    }

    public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
        checkArgument(arguments.size() == parameters.size());

        final EvaluationContext evaluationContext = enclosingEvaluationContext.narrow(ImmutableList.of(
                collectArguments(moduleInterpretation.util, parameters, arguments),
                statements.stream()
                        .filter(LocalVariableDeclaration.class::isInstance)
                        .map(LocalVariableDeclaration.class::cast)
                        .map(v -> immutableEntry(v, moduleInterpretation.util.createField(v.getValueType(), moduleInterpretation.util.nil)))
                        .collect(map())
        ));

        try {
            final StatementEvaluator statementEvaluator = moduleInterpretation.getStatementEvaluator();
            statements.forEach(s -> statementEvaluator.evaluate(evaluationContext, s));
            return moduleInterpretation.util.unit;
        } catch (CofiReturnValue returnValue) {
            return returnValue.getValue();
        }
    }
}
