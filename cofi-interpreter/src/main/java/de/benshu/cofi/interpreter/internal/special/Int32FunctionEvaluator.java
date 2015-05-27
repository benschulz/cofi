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

public class Int32FunctionEvaluator {
    public static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        switch (method.getName()) {
            default:
                throw new UnexpectedBranchException(method.getName());
            case "asString":
                return new AsStringEvaluator(moduleInterpretation.util, receiver);
            case "lessThan":
                return new LessThanEvaluator(moduleInterpretation.util, receiver);
            case "plus":
                return new PlusEvaluator(moduleInterpretation.util, receiver);
        }
    }

    private static class AsStringEvaluator implements FunctionEvaluator {
        private final Utilities util;
        private final CofiObject receiver;

        public AsStringEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            checkArgument(arguments.isEmpty());

            final int value = util.intFromInt32(receiver);
            return util.createString(String.valueOf(value));
        }
    }

    private static class LessThanEvaluator implements FunctionEvaluator {
        private final Utilities util;

        private final CofiObject receiver;

        public LessThanEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            return util.intFromInt32(receiver) < util.intFromInt32(onlyArgument(arguments))
                    ? util.lookUpOrCreateSingleton("cofi", "lang", "Bool", "TRUE")
                    : util.lookUpOrCreateSingleton("cofi", "lang", "Bool", "FALSE");
        }

    }

    private static class PlusEvaluator implements FunctionEvaluator {

        private final Utilities util;

        private final CofiObject receiver;

        public PlusEvaluator(Utilities util, CofiObject receiver) {
            this.util = util;
            this.receiver = receiver;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            return util.createIn32(util.intFromInt32(receiver) + util.intFromInt32(onlyArgument(arguments)));
        }

    }
}
