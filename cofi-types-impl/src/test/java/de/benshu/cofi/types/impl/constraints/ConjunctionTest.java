package de.benshu.cofi.types.impl.constraints;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@Guice(modules = TestTypeSystemModule.class)
public class ConjunctionTest {

    @Inject
    private TestContext context;

    @Inject
    private TypeSystemImpl<TestContext> types;

    @Test
    public void detectCycleOfSize2() {
        TypeParameterListImpl<TestContext> params = TypeParameterListImpl.createTrivial(2, context);
        TypeVariableImpl<TestContext, ?> a = params.getVariables().get(0);
        TypeVariableImpl<TestContext, ?> b = params.getVariables().get(1);

        Conjunction<TestContext> constraints = AbstractConstraints.trivial(context, params)
                .and(context, a, Constraint.upper(b))
                .and(context, b, Constraint.upper(a));

        assertEquals(constraints.getQuotientSet(), ImmutableSet.of(ImmutableSet.<Object>of(a, b)));
    }

    @Test
    public void detectCycleOfSize3() {
        TypeParameterListImpl<TestContext> params = TypeParameterListImpl.createTrivial(3, context);
        TypeVariableImpl<TestContext, ?> a = params.getVariables().get(0);
        TypeVariableImpl<TestContext, ?> b = params.getVariables().get(1);
        TypeVariableImpl<TestContext, ?> c = params.getVariables().get(2);

        Conjunction<TestContext> constraints = AbstractConstraints.trivial(context, params)
                .and(context, a, Constraint.upper(b))
                .and(context, b, Constraint.upper(c))
                .and(context, c, Constraint.upper(a));

        assertEquals(constraints.getQuotientSet(), ImmutableSet.of(ImmutableSet.<Object>of(a, b, c)));
    }

    @Test
    public void detectCycleOfSize4() {
        TypeParameterListImpl<TestContext> params = TypeParameterListImpl.createTrivial(4, context);
        TypeVariableImpl<TestContext, ?> a = params.getVariables().get(0);
        TypeVariableImpl<TestContext, ?> b = params.getVariables().get(1);
        TypeVariableImpl<TestContext, ?> c = params.getVariables().get(2);
        TypeVariableImpl<TestContext, ?> d = params.getVariables().get(3);

        Conjunction<TestContext> constraints = AbstractConstraints.trivial(context, params)
                .and(context, a, Constraint.upper(b))
                .and(context, b, Constraint.upper(c))
                .and(context, c, Constraint.upper(d))
                .and(context, d, Constraint.upper(a));

        assertEquals(constraints.getQuotientSet(), ImmutableSet.of(ImmutableSet.<Object>of(a, b, c, d)));
    }

    @Test
    public void detectTwoSeparateCyclesOfSizes2And4() {
        TypeParameterListImpl<TestContext> params = TypeParameterListImpl.createTrivial(6, context);
        TypeVariableImpl<TestContext, ?> a = params.getVariables().get(0);
        TypeVariableImpl<TestContext, ?> b = params.getVariables().get(1);
        TypeVariableImpl<TestContext, ?> c = params.getVariables().get(2);
        TypeVariableImpl<TestContext, ?> d = params.getVariables().get(3);
        TypeVariableImpl<TestContext, ?> e = params.getVariables().get(4);
        TypeVariableImpl<TestContext, ?> f = params.getVariables().get(5);

        Conjunction<TestContext> constraints = AbstractConstraints.trivial(context, params)
                .and(context, a, Constraint.upper(b))
                .and(context, b, Constraint.upper(c))
                .and(context, c, Constraint.upper(d))
                .and(context, d, Constraint.upper(a))
                .and(context, e, Constraint.upper(f))
                .and(context, f, Constraint.upper(e));

        assertEquals(constraints.getQuotientSet(), ImmutableSet.of(
                ImmutableSet.<Object>of(a, b, c, d),
                ImmutableSet.<Object>of(e, f)));
    }
}
