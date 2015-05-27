package de.benshu.cofi.types.impl.test.matcher;

import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.test.TestContext;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class Equal extends TypeSafeMatcher<TypeMixin<TestContext, ?>> {
    public static InContextPreparation<Equal> to(TestContext context, TypeMixin<TestContext, ?> otherType) {
        return new InContextPreparation<>(c -> new Equal(context, otherType, c));
    }

    private final TestContext context;
    private final TypeMixin<TestContext, ?> otherType;
    private final AbstractConstraints<TestContext> constraints;

    private Equal(TestContext context, TypeMixin<TestContext, ?> otherType, AbstractConstraints<TestContext> constraints) {
        this.context = context;
        this.otherType = otherType;
        this.constraints = constraints;
    }

    @Override
    protected boolean matchesSafely(TypeMixin<TestContext, ?> type) {
        return constraints.areEqualTypes(type, otherType);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(otherType).appendText("[").appendValue(constraints).appendText("]");
    }
}
