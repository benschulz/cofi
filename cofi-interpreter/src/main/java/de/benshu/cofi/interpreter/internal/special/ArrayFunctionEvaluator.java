package de.benshu.cofi.interpreter.internal.special;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.interpreter.internal.CofiObject;
import de.benshu.cofi.interpreter.internal.FunctionEvaluator;
import de.benshu.cofi.interpreter.internal.ModuleInterpretation;
import de.benshu.cofi.interpreter.internal.Utilities;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.types.TemplateType;
import de.benshu.commons.core.exception.UnexpectedBranchException;

import static com.google.common.base.Preconditions.checkArgument;
import static de.benshu.cofi.interpreter.internal.special.SpecialFunctionEvaluator.onlyArgument;

public class ArrayFunctionEvaluator {
    public static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        switch (method.getName()) {
            default:
                throw new UnexpectedBranchException(method.getName());
            case "get":
                return new GetEvaluator(moduleInterpretation.util, receiver);
            case "length":
                return new LengthEvaluator(moduleInterpretation.util, receiver);
            case "setAtTo":
                return new SetAtToEvaluator(moduleInterpretation.util, receiver);
        }
    }

    private static class GetEvaluator implements FunctionEvaluator {
        private final Utilities util;
        private final CofiObject receiver;

        public GetEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            return receiver.getSpecialValues().getInstance(CofiObject[].class)[util.intFromNatural(onlyArgument(arguments))];
        }
    }

    private static class LengthEvaluator implements FunctionEvaluator {
        private final Utilities util;
        private final CofiObject receiver;

        public LengthEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            return util.naturalFrom(receiver.getSpecialValues().getInstance(CofiObject[].class).length);
        }
    }

    private static class SetAtToEvaluator implements FunctionEvaluator {
        private final Utilities util;
        private final CofiObject receiver;

        public SetAtToEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            return (TemplateType) receiver.getType().lookupMember("setAtTo").get().getType().applyTrivially();
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            checkArgument(arguments.size() == 2);

            final CofiObject[] array = receiver.getSpecialValues().getInstance(CofiObject[].class);

            final int index = util.intFromNatural(arguments.get(0));
            final CofiObject newValue = arguments.get(1);

            final CofiObject oldValue = array[index];
            array[index] = newValue;
            return oldValue;
        }
    }
}
