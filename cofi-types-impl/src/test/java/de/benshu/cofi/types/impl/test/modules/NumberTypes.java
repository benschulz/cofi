package de.benshu.cofi.types.impl.test.modules;

import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;

public class NumberTypes {
    private final TemplateTypeImpl<TestContext> rational;
    private final TemplateTypeImpl<TestContext> positiveRational;

    private final TemplateTypeImpl<TestContext> integer;
    private final TemplateTypeImpl<TestContext> natural;

    public NumberTypes(TypeBuilder typeBuilder) {
        rational = typeBuilder.createTemplateType().called("cofi", "lang", "Ratioanl").extendingTop().applyTrivially();
        positiveRational = typeBuilder.createTemplateType().called("cofi", "lang", "PositiveRational").extending(rational).declaringNoMembers().applyTrivially();
        integer = typeBuilder.createTemplateType().called("cofi", "lang", "Integer").extending(rational).declaringNoMembers().applyTrivially();
        natural = typeBuilder.createTemplateType().called("cofi", "lang", "Natural").extending(integer, positiveRational).declaringNoMembers().applyTrivially();
    }

    public TemplateTypeImpl<TestContext> getRational() {
        return rational;
    }

    public TemplateTypeImpl<TestContext> getPositiveRational() {
        return positiveRational;
    }

    public TemplateTypeImpl<TestContext> getInteger() {
        return integer;
    }

    public TemplateTypeImpl<TestContext> getNatural() {
        return natural;
    }
}
