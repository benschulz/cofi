package de.benshu.cofi.types.impl.test.matcher;

import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.test.TestContext;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class Supertype extends TypeSafeMatcher<TypeMixin<TestContext, ?>> {
    public static InContextPreparation<Supertype> of(TestContext context, TypeMixin<TestContext, ?> subtype) {
        return new InContextPreparation<>(c -> new Supertype(context, subtype, c));
    }

    private final TestContext context;
    private final TypeMixin<TestContext, ?> subtype;
    private final AbstractConstraints<TestContext> constraints;

    private Supertype(TestContext context, TypeMixin<TestContext, ?> subtype, AbstractConstraints<TestContext> constraints) {
        this.context = context;
        this.subtype = subtype;
        this.constraints = constraints;
    }

    @Override
    protected boolean matchesSafely(TypeMixin<TestContext, ?> type) {
        return constraints.isSubtype(subtype, type);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(subtype).appendText("[").appendValue(constraints).appendText("]");
    }
}
