package de.benshu.cofi.interpreter.internal.special;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.interpreter.internal.CofiObject;
import de.benshu.cofi.interpreter.internal.FunctionEvaluator;
import de.benshu.cofi.interpreter.internal.ModuleInterpretation;
import de.benshu.cofi.interpreter.internal.Utilities;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.types.TemplateType;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import static de.benshu.cofi.interpreter.internal.special.SpecialFunctionEvaluator.onlyArgument;

public class FieldImplFunctionEvaluator {
    public static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        final String methodName = method.getName();

        switch (methodName) {
            default:
                throw new UnexpectedBranchException(method.getName());
            case "get":
                return new GetEvaluator(receiver);
            case "set":
                return new SetEvaluator(moduleInterpretation.util, receiver);
        }
    }

    private static class GetEvaluator implements FunctionEvaluator {
        private final CofiObject receiver;

        public GetEvaluator(CofiObject receiver) {
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            return receiver.getSpecialValues().getInstance(CofiObject.class);
        }
    }

    private static class SetEvaluator implements FunctionEvaluator {
        private final Utilities util;
        private final CofiObject receiver;

        public SetEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            receiver.getSpecialValues().putInstance(CofiObject.class, onlyArgument(arguments));
            return util.unit;
        }
    }
}
