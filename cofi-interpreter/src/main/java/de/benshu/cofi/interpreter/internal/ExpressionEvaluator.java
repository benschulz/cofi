package de.benshu.cofi.interpreter.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.runtime.Closure;
import de.benshu.cofi.runtime.Expression;
import de.benshu.cofi.runtime.ExpressionVisitor;
import de.benshu.cofi.runtime.FunctionInvocation;
import de.benshu.cofi.runtime.LiteralValue;
import de.benshu.cofi.runtime.MemberAccess;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.Multiton;
import de.benshu.cofi.runtime.NameExpression;
import de.benshu.cofi.runtime.RootExpression;
import de.benshu.cofi.runtime.Singleton;
import de.benshu.cofi.runtime.ThisExpression;
import de.benshu.cofi.runtime.context.tags.Implementations;
import de.benshu.cofi.types.Member;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.TypeList;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import static de.benshu.commons.core.streams.Collectors.list;

public class ExpressionEvaluator {
    private final ModuleInterpretation moduleInterpretation;

    public ExpressionEvaluator(ModuleInterpretation moduleInterpretation) {
        this.moduleInterpretation = moduleInterpretation;
    }

    public CofiObject evaluate(EvaluationContext context, Expression expression) {
        return new Evaluation(context, expression).perform();
    }

    CofiObject evaluateFunctionInvocation(CofiObject function, ImmutableList<CofiObject> arguments) {
        return function.getSpecialValues().getInstance(FunctionEvaluator.class).evaluate(arguments);
    }

    private class Evaluation implements ExpressionVisitor<CofiObject> {
        private final EvaluationContext context;
        private final Expression expression;

        public Evaluation(EvaluationContext context, Expression expression) {
            this.context = context;
            this.expression = expression;
        }

        public CofiObject perform() {
            return visit(expression);
        }

        @Override
        public CofiObject visitClosure(Closure closure) {
            final Closure.Case onlyCase = Iterables.getOnlyElement(closure.getCases());

            final CofiObject functionObject = new CofiObject((TemplateType) closure.getType());
            functionObject.getSpecialValues().putInstance(FunctionEvaluator.class,
                    new RegularFunctionEvaluator(moduleInterpretation, (TemplateType) closure.getType(), context, onlyCase.getBody(), onlyCase.getParameters()));

            return functionObject;
        }

        @Override
        public CofiObject visitFunctionInvocation(FunctionInvocation functionInvocation) {
            final CofiObject function = evaluateSubexpression(functionInvocation.getPrimary());
            final ImmutableList<CofiObject> arguments = functionInvocation.getArguments().stream()
                    .map(this::evaluateSubexpression)
                    .collect(list());

            return evaluateFunctionInvocation(function, arguments);
        }

        @Override
        public CofiObject visitLiteralValue(LiteralValue literalValue) {
            final ProperType type = literalValue.getType();
            String literal = literalValue.getLiteral();

            if (moduleInterpretation.util.isExactType(type, "cofi", "lang", "Natural")) {
                return moduleInterpretation.util.naturalFrom(Integer.parseInt(literal));
            } else if (moduleInterpretation.util.isExactType(type, "cofi", "lang", "String")) {
                return moduleInterpretation.util.createString(literal.substring(1, literal.length() - 1));
            }

            throw new UnexpectedBranchException();
        }

        @Override
        public CofiObject visitMemberAccess(MemberAccess memberAccess) {
            final CofiObject primary = evaluateSubexpression(memberAccess.getPrimary());

            String memberName = memberAccess.getMemberName();
            TypeList<?> typeArguments = memberAccess.getTypeArguments();
            final Member member = primary.getType().lookupMember(memberName).get();

            switch (member.getSort()) {
                case METHOD:
                    final MethodDeclaration effectiveImplementation = member.getTags().get(Implementations.TAG).getEffectiveImplementation();
                    final TemplateType signature = effectiveImplementation.getSignature().apply(typeArguments);
                    final CofiObject functionObject = new CofiObject(signature);
                    functionObject.getSpecialValues().putInstance(FunctionEvaluator.class,
                            FunctionEvaluator.forMethod(moduleInterpretation, primary, effectiveImplementation));
                    return functionObject;
                case TYPE:
                    return typeArguments.isEmpty()
                            ? moduleInterpretation.getSingletons().lookUpOrCreate((Singleton) member.getTags().get(MemberDeclaration.TAG))
                            : moduleInterpretation.getSingletons().lookUpOrCreate((Multiton) member.getTags().get(MemberDeclaration.TAG), typeArguments);
                case PROPERTY:
                    return primary.getProperties().get(member.getTags().get(MemberDeclaration.TAG));
                default:
                    throw new UnexpectedBranchException();
            }
        }

        @Override
        public CofiObject visitNameExpression(NameExpression nameExpression) {
            return context.get(context.lookUpVariable(nameExpression.getName()));
        }

        @Override
        public CofiObject visitRootExpression(RootExpression rootExpression) {
            return moduleInterpretation.util.lookUpOrCreateSingleton(/* root */);
        }

        @Override
        public CofiObject visitThisExpression(ThisExpression thisExpression) {
            return context.getOwner();
        }

        private CofiObject evaluateSubexpression(Expression subexpression) {
            return evaluate(context, subexpression);
        }
    }
}
