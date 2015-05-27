package de.benshu.cofi.types.impl;

import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.intersections.ConstructedIntersectionTypeImpl;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;
import de.benshu.cofi.types.impl.test.matcher.Equal;
import de.benshu.cofi.types.impl.test.matcher.Subtype;
import de.benshu.cofi.types.impl.test.matcher.Supertype;
import de.benshu.cofi.types.impl.test.modules.AnimalTypes;
import de.benshu.cofi.types.impl.test.modules.DayEnumTypes;
import de.benshu.cofi.types.impl.test.modules.NumberTypes;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;

import de.benshu.cofi.types.impl.unions.ConstructedUnionTypeImpl;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static de.benshu.cofi.types.Variance.*;
import static de.benshu.cofi.types.impl.test.Util.types;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Guice(modules = TestTypeSystemModule.class)
public class SubtypingTest {
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
    public void monomorphicTemplateTypeIsSubtypeOfTop() {
        TemplateTypeImpl<TestContext> monomorphicTemplateType = typeBuilder
                .createTemplateType()
                .called("MonomorphicTemplateType").extendingTop().applyTrivially();

        TemplateTypeImpl<TestContext> top = types.getTop();

        assertThat(monomorphicTemplateType, is(Subtype.of(context, top).givenNoConstraints()));
    }

    @Test
    public void bivariantSubtype() {
        AbstractTemplateTypeConstructor<TestContext> templateTypeConstructor = typeBuilder
                .createTemplateType()
                .called("TemplateType")
                .parametrizedBy(typeBuilder.createParameters().called("T").withVariances(BIVARIANT).triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially()
                .getConstructor();

        final TemplateTypeImpl<TestContext> ofRational = templateTypeConstructor.apply(types(numberTypes.getRational()));
        final TemplateTypeImpl<TestContext> ofNatural = templateTypeConstructor.apply(types(numberTypes.getNatural()));

        assertThat(ofNatural, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofRational, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofNatural, is(Subtype.of(context, ofRational).givenNoConstraints()));
    }

    @Test
    public void contravariantSubtype() {
        AbstractTemplateTypeConstructor<TestContext> templateTypeConstructor = typeBuilder
                .createTemplateType()
                .called("TemplateType")
                .parametrizedBy(typeBuilder.createParameters().called("T").withVariances(CONTRAVARIANT).triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially()
                .getConstructor();

        final TemplateTypeImpl<TestContext> ofRational = templateTypeConstructor.apply(types(numberTypes.getRational()));
        final TemplateTypeImpl<TestContext> ofNatural = templateTypeConstructor.apply(types(numberTypes.getNatural()));

        assertThat(ofNatural, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofRational, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofNatural, is(not(Subtype.of(context, ofRational).givenNoConstraints())));
    }

    @Test
    public void covariantSubtype() {
        AbstractTemplateTypeConstructor<TestContext> templateTypeConstructor = typeBuilder
                .createTemplateType()
                .called("TemplateType")
                .parametrizedBy(typeBuilder.createParameters().called("T").withVariances(COVARIANT).triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially()
                .getConstructor();

        final TemplateTypeImpl<TestContext> ofRational = templateTypeConstructor.apply(types(numberTypes.getRational()));
        final TemplateTypeImpl<TestContext> ofNatural = templateTypeConstructor.apply(types(numberTypes.getNatural()));

        assertThat(ofNatural, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofRational, is(not(Subtype.of(context, ofNatural).givenNoConstraints())));
        assertThat(ofNatural, is(Subtype.of(context, ofRational).givenNoConstraints()));
    }

    @Test
    public void invariantSubtype() {
        AbstractTemplateTypeConstructor<TestContext> templateTypeConstructor = typeBuilder
                .createTemplateType()
                .called("TemplateType")
                .parametrizedBy(typeBuilder.createParameters().called("T").withVariances(INVARIANT).triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially()
                .getConstructor();

        final TemplateTypeImpl<TestContext> ofRational = templateTypeConstructor.apply(types(numberTypes.getRational()));
        final TemplateTypeImpl<TestContext> ofNatural = templateTypeConstructor.apply(types(numberTypes.getNatural()));

        assertThat(ofNatural, is(Subtype.of(context, ofNatural).givenNoConstraints()));
        assertThat(ofRational, is(not(Subtype.of(context, ofNatural).givenNoConstraints())));
        assertThat(ofNatural, is(not(Subtype.of(context, ofRational).givenNoConstraints())));
    }

    @Test
    public void tuesdayIsAWorkday() {
        TemplateTypeImpl<TestContext> tuesday = dayEnumTypes.getTuesday();
        ConstructedUnionTypeImpl<TestContext> workday = dayEnumTypes.getWorkday();

        assertThat(tuesday, is(Subtype.of(context, workday).givenNoConstraints()));
    }

    @Test
    public void sundayIsNotAWorkday() {
        TemplateTypeImpl<TestContext> sunday = dayEnumTypes.getSunday();
        ConstructedUnionTypeImpl<TestContext> workday = dayEnumTypes.getWorkday();

        assertThat(sunday, is(not(Subtype.of(context, workday).givenNoConstraints())));
    }

    @Test
    public void workdayIsAWeekday() {
        ConstructedUnionTypeImpl<TestContext> workday = dayEnumTypes.getWorkday();
        ConstructedUnionTypeImpl<TestContext> weekday = dayEnumTypes.getWeekday();

        assertThat(workday, is(Subtype.of(context, weekday).givenNoConstraints()));
    }

    @Test
    public void platypusIsAnAquaticMammal() {
        TemplateTypeImpl<TestContext> platypus = animalTypes.getPlatypus();
        ConstructedIntersectionTypeImpl<TestContext> aquaticMammal = animalTypes.getAquaticMammal();

        assertThat(platypus, is(Subtype.of(context, aquaticMammal).givenNoConstraints()));
    }

    @Test
    public void aquaticMammalIsAquatic() {
        TemplateTypeImpl<TestContext> aquatic = animalTypes.getAquatic();
        ConstructedIntersectionTypeImpl<TestContext> aquaticMammal = animalTypes.getAquaticMammal();

        assertThat(aquaticMammal, is(Subtype.of(context, aquatic).givenNoConstraints()));
    }

    @Test
    public void mammalIsNotAquaticMammal() {
        TemplateTypeImpl<TestContext> mammal = animalTypes.getMammal();
        ConstructedIntersectionTypeImpl<TestContext> aquaticMammal = animalTypes.getAquaticMammal();

        assertThat(mammal, is(not(Subtype.of(context, aquaticMammal).givenNoConstraints())));
    }

    @Test
    public void errorIsSubtypeOfBottom() {
        Error<TestContext> errorType = typeBuilder.createErrorType();

        assertThat(errorType, is(Subtype.of(context, types.getBottom()).givenNoConstraints()));
    }

    @Test
    public void errorIsSupertypeOfTop() {
        Error<TestContext> errorType = typeBuilder.createErrorType();

        assertThat(errorType, is(Supertype.of(context, types.getBottom()).givenNoConstraints()));
    }

    @Test
    public void test() {
        final TemplateTypeImpl<TestContext> top = types.getTop();

        final ConstructedIntersectionTypeImpl<TestContext> tipTop = typeBuilder.createIntersectionType().called("TipTop").of(top).applyTrivially();

        assertThat(tipTop, is(Equal.to(context, top).givenNoConstraints()));
    }

    @Test
    public void test2() {
        final TemplateTypeImpl<TestContext> top = types.getTop();
        final ConstructedIntersectionTypeImpl<TestContext> tipTop = typeBuilder.createIntersectionType().called("TipTop").of(top).applyTrivially();

        final TypeParameterListImpl<TestContext> typeParameters = typeBuilder.createParameters().called("T").withVariances(INVARIANT).constrainedToBeSubtypesOf(top);
        final TypeVariableImpl<TestContext, ?> typeVariable = typeParameters.getVariables().get(0);
        final AbstractConstraints<TestContext> constraints = typeParameters.getConstraints();

        assertThat(typeVariable, is(Subtype.of(context, tipTop).given(constraints)));
    }
}
