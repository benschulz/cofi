package de.benshu.cofi.types.impl;

import com.google.inject.Inject;

import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;
import de.benshu.cofi.types.impl.test.modules.AnimalTypes;
import de.benshu.cofi.types.impl.test.modules.DayEnumTypes;
import de.benshu.cofi.types.impl.test.modules.NumberTypes;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Guice(modules = TestTypeSystemModule.class)
public class DebugTest {
    @Inject
    private TestContext context;
    @Inject
    private TypeSystemImpl<TestContext> types;
    @Inject
    private AnimalTypes animalTypes;
    @Inject
    private NumberTypes numberTypes;
    @Inject
    private DayEnumTypes dayEnumTypes;
    @Inject
    private TypeBuilder typeBuilder;

    @Test
    public void monomorphicTemplateTypeConstructorGivesFqnPlusEmptyAngleBrackets() {
        AbstractTemplateTypeConstructor<TestContext> monomorphicTemplateTypeConstructor = typeBuilder
                .createTemplateType()
                .called("a", "b", "Monomorphic").extendingTop().applyTrivially()
                .getConstructor();

        assertThat(monomorphicTemplateTypeConstructor.debug(), is(equalTo("\u3008\u3009\u21a6.a.b.Monomorphic\u3008\u3009")));
    }

    @Test
    public void monomorphicTemplateTypeGivesFqn() {
        TemplateTypeImpl<TestContext> monomorphicTemplateType = typeBuilder
                .createTemplateType()
                .called("a", "b", "Monomorphic").extendingTop().applyTrivially();

        assertThat(monomorphicTemplateType.debug(), is(equalTo(".a.b.Monomorphic\u3008\u3009")));
    }

    @Test
    public void parametrizedTemplateTypeConstructorGivesFqnPlusTypeParameters() {
        AbstractTemplateTypeConstructor<TestContext> parametrizedTemplateType = typeBuilder
                .createTemplateType()
                .called("a", "b", "Parametrized")
                .parametrizedBy(typeBuilder.createParameters().called("T").triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially()
                .getConstructor();

        assertThat(parametrizedTemplateType.debug(), is(equalTo("\u3008T\u3009\u21a6.a.b.Parametrized\u3008T\u3009")));
    }

    @Test
    public void parametrizedTemplateTypeGivesFqnPlusTypeArguments() {
        TypeParameterListImpl<TestContext> parameters = typeBuilder.createParameters().called("T").triviallyConstrained();
        TemplateTypeImpl<TestContext> parametrizedTemplateType = typeBuilder
                .createTemplateType()
                .called("a", "b", "Parametrized")
                .parametrizedBy(parameters).extendingTop().declaringNoMembers().applyTrivially()
                .substitute(Substitutions.ofThrough(parameters, AbstractTypeList.of(numberTypes.getNatural())));

        assertThat(parametrizedTemplateType.debug(), is(equalTo(".a.b.Parametrized\u3008.cofi.lang.Natural\u3008\u3009\u3009")));
    }

    @Test
    public void anonymousIntersectionTypeGivesListOfElements() {
        TemplateTypeImpl<TestContext> element1 = typeBuilder.createTemplateType().called("x", "T1").extendingTop().applyTrivially();
        TemplateTypeImpl<TestContext> element2 = typeBuilder.createTemplateType().called("x", "T2").extendingTop().applyTrivially();
        AnonymousIntersectionType<TestContext> anonymousIntersectionType = AnonymousIntersectionType.create(context, AbstractTypeList.of(element1, element2));

        assertThat(anonymousIntersectionType.debug(), is(equalTo("(.x.T1\u3008\u3009 & .x.T2\u3008\u3009)")));
    }
}
