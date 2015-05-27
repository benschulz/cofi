package de.benshu.cofi.types.impl.test.matcher;

import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.test.TestContext;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class Subtype extends TypeSafeMatcher<TypeMixin<TestContext, ?>> {
    public static InContextPreparation<Subtype> of(TestContext context, TypeMixin<TestContext, ?> supertype) {
        return new InContextPreparation<>(c -> new Subtype(context, supertype, c));
    }

    private final TestContext context;
    private final TypeMixin<TestContext, ?> supertype;
    private final AbstractConstraints<TestContext> constraints;

    private Subtype(TestContext context, TypeMixin<TestContext, ?> supertype, AbstractConstraints<TestContext> constraints) {
        this.context = context;
        this.supertype = supertype;
        this.constraints = constraints;
    }

    @Override
    protected boolean matchesSafely(TypeMixin<TestContext, ?> type) {
        return constraints.isSubtype(type, supertype);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(supertype).appendText("[").appendValue(constraints).appendText("]");
    }
}
