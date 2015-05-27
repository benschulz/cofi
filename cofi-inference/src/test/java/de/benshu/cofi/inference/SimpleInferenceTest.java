package de.benshu.cofi.inference;

import com.google.inject.Inject;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.matcher.Equal;
import de.benshu.cofi.types.impl.test.modules.NumberTypes;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Guice(modules = TestTypeSystemModule.class)
public class SimpleInferenceTest {
    @Inject
    private TestContext context;
    @Inject
    private TypeSystemImpl<TestContext> types;
    @Inject
    private NumberTypes numberTypes;

    @Test
    public void simpleTest() {
        TypeParameterListImpl<TestContext> typeParameters = TypeParameterListImpl.createTrivial(2, context);
        TypeVariableImpl<TestContext, ?> a = typeParameters.getVariables().get(0);
        TypeVariableImpl<TestContext, ?> b = typeParameters.getVariables().get(1);

        AbstractConstraints<TestContext> constraints = AbstractConstraints.trivial(context, typeParameters)
                .establishSubtype(a, b)
                .establishSubtype(b, numberTypes.getNatural());

        Inferencer<TestContext> inferencer = new Inferencer<>(types, constraints);

        AbstractTypeList<TestContext, ?> invocation = inferencer.infer(context).iterator().next();

        assertThat(invocation.size(), is(equalTo(2)));
        assertThat(invocation.get(0), is(Equal.to(context, numberTypes.getNatural()).givenNoConstraints()));
        assertThat(invocation.get(1), is(Equal.to(context, numberTypes.getNatural()).givenNoConstraints()));
    }
}
