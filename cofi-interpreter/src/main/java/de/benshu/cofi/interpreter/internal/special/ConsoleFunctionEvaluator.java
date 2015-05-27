package de.benshu.cofi.interpreter.internal.special;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.interpreter.internal.CofiObject;
import de.benshu.cofi.interpreter.internal.FunctionEvaluator;
import de.benshu.cofi.interpreter.internal.ModuleInterpretation;
import de.benshu.cofi.interpreter.internal.Utilities;
import de.benshu.cofi.runtime.Class;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.types.TemplateType;
import de.benshu.commons.core.exception.UnexpectedBranchException;

public class ConsoleFunctionEvaluator {
    public static FunctionEvaluator forMethod(ModuleInterpretation moduleInterpretation, CofiObject receiver, MethodDeclaration method) {
        switch (method.getName()) {
            default:
                throw new UnexpectedBranchException(method.getName());
            case "println":
                return new PrintlnEvaluator(moduleInterpretation.util);
        }
    }

    private static class PrintlnEvaluator implements FunctionEvaluator {
        private final Utilities util;

        public PrintlnEvaluator(Utilities util) {
            this.util = util;
        }

        @Override
        public TemplateType getSignature() {
            throw null;
        }

        @Override
        public CofiObject evaluate(ImmutableList<CofiObject> arguments) {
            final CofiObject value = arguments.get(0);

            final Class stringClass = util.lookUpClass("cofi", "lang", "String");

            System.out.print("Console.println > ");
            if (util.isExactType(value.getType(), stringClass)) {
                final MemberDeclaration csDeclaration = value.getType().lookupMember("cs").get().getTags().get(MemberDeclaration.TAG);
                final CofiObject charArrayField = value.getProperties().get(csDeclaration);
                final CofiObject charArray = charArrayField.getSpecialValues().getInstance(CofiObject.class);
                final CofiObject[] chars = charArray.getSpecialValues().getInstance(CofiObject[].class);

                for (CofiObject c : chars) {
                    System.out.print((char) c.getSpecialValues().getInstance(long.class).intValue());
                }

                System.out.println();
            } else {
                System.out.println(value);
            }

            return util.unit;
        }
    }
}
