package de.benshu.cofi.interpreter.internal.special;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.benshu.cofi.interpreter.internal.CofiObject;
import de.benshu.cofi.interpreter.internal.CofiReturnValue;
import de.benshu.cofi.interpreter.internal.EvaluationContext;
import de.benshu.cofi.interpreter.internal.FunctionEvaluator;
import de.benshu.cofi.interpreter.internal.ModuleInterpretation;
import de.benshu.cofi.interpreter.internal.Utilities;
import de.benshu.cofi.runtime.Class;
import de.benshu.cofi.runtime.Companion;
import de.benshu.cofi.runtime.Expression;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.PropertyDeclaration;
import de.benshu.cofi.runtime.TypeDeclaration;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.TemplateType;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import java.util.Arrays;
import java.util.function.Function;

import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.map;

public class SpecialFunctionEvaluator {
    public static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        final Utilities util = moduleInterpretation.util;
        final String methodName = method.getName();

        // TODO Rely less on types and more on the runtime model.

        if (util.isOfExactType(receiver, "cofi", "lang")
                && methodName.equals("return"))
            return new ReturnEvaluator();

        if (util.isOfExactType(receiver, "cofi", "lang", "primitives", "Int32"))
            return Int32FunctionEvaluator.forMethod(moduleInterpretation, receiver, method);

        if (util.isTypeInvocationOf(receiver, "cofi", "lang", "FieldImpl"))
            return FieldImplFunctionEvaluator.forMethod(moduleInterpretation, receiver, method);

        if (receiver.getType().getTags().get(TypeDeclaration.TAG) instanceof Companion)
            if (((Companion) receiver.getType().getTags().get(TypeDeclaration.TAG)).getAccompanied() instanceof de.benshu.cofi.runtime.Class
                    && methodName.equals("create")) {

                final Companion companion = (Companion) receiver.getType().getTags().get(TypeDeclaration.TAG);
                final Class accompanied = (Class) companion.getAccompanied();

                final TemplateType companionType = receiver.getType();

                return instanceCreateEvaluatorFor(moduleInterpretation, accompanied, companionType.getArguments());
            } else if (methodName.equals("cast"))
                return new CastEvaluator(receiver);

        if (util.isTypeInvocationOf(receiver, "cofi", "lang", "collect", "Array"))
            return ArrayFunctionEvaluator.forMethod(moduleInterpretation, receiver, method);

        if (util.isOfExactType(receiver, "cofi", "lang", "Console")
                && methodName.equals("println"))
            return ConsoleFunctionEvaluator.forMethod(moduleInterpretation, receiver, method);

        throw new UnexpectedBranchException();
    }

    public static FunctionEvaluator instanceCreateEvaluatorFor(ModuleInterpretation moduleInterpretation, Class klazz, Type... typeArguments) {
        return instanceCreateEvaluatorFor(moduleInterpretation, klazz, TypeList.of(typeArguments));
    }

    private static FunctionEvaluator instanceCreateEvaluatorFor(ModuleInterpretation moduleInterpretation, Class klazz, TypeList<?> typeArguments) {
        return new CreateEvaluator(moduleInterpretation, klazz, typeArguments);
    }

    private static class ReturnEvaluator implements FunctionEvaluator {
        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            throw new CofiReturnValue(onlyArgument(arguments));
        }

    }

    private static class CreateEvaluator implements FunctionEvaluator {
        private final ModuleInterpretation moduleInterpretation;
        private final Class klazz;
        private final TypeList<?> typeArguments;

        public CreateEvaluator(ModuleInterpretation moduleInterpretation, Class klazz, TypeList<?> typeArguments) {
            this.moduleInterpretation = moduleInterpretation;
            this.klazz = klazz;
            this.typeArguments = typeArguments;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            final Utilities util = moduleInterpretation.util;

            if (klazz.equals(util.lookUpTypeDeclaration("cofi", "lang", "collect", "Array"))) {
                final CofiObject naturalLength = onlyArgument(arguments);
                final int length = util.intFromNatural(naturalLength);
                // TODO Array needs to be a FixList<Optional<E>>
                final CofiObject[] elements = new CofiObject[length];
                Arrays.fill(elements, util.nil);
                return util.createArray((ProperType) Iterables.getOnlyElement(typeArguments), elements);
            }

            final ImmutableList<PropertyDeclaration> properties = klazz.getBody().getElements().stream()
                    .filter(PropertyDeclaration.class::isInstance)
                    .map(PropertyDeclaration.class::cast)
                    .collect(list());

            final CofiObject instance = new CofiObject(klazz.getType().apply(typeArguments), properties.stream()
                    .collect(map(Function.identity(), p -> util.createField(p.getValueType(), util.nil))));

            final EvaluationContext evaluationContext = new EvaluationContext(instance, ImmutableList.of(
                    FunctionEvaluator.collectArguments(util, klazz.getParameters(), arguments),
                    instance.getProperties()
            ));

            // TODO full statement support
            properties.forEach(d -> {
                for (Expression initialValueExpression : d.getInitialValue()) {
                    final CofiObject initialValue = moduleInterpretation.getExpressionEvaluator()
                            .evaluate(evaluationContext, initialValueExpression);

                    instance.getProperties().get(d).getSpecialValues().put(CofiObject.class, initialValue);
                }
            });

            return instance;
        }
    }

    private static class CastEvaluator implements FunctionEvaluator {
        private final CofiObject receiver;

        public CastEvaluator(CofiObject receiver) {this.receiver = receiver;}

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            final Companion companion = (Companion) receiver.getType().getTags().get(TypeDeclaration.TAG);
            final Class accompanied = (Class) companion.getAccompanied();

            final TemplateType companionType = receiver.getType();
            final ProperType accompaniedType = accompanied.getType().apply(companionType.getArguments());

            final CofiObject value = onlyArgument(arguments);

            if (!AbstractConstraints.none().unbind().isSubtype(value.getType(), accompaniedType))
                throw new ClassCastException(value + " is not an instance of " + accompaniedType + ".");

            return value;
        }
    }

    static CofiObject onlyArgument(ImmutableList<CofiObject> arguments) {
        return Iterables.getOnlyElement(arguments);
    }
}
