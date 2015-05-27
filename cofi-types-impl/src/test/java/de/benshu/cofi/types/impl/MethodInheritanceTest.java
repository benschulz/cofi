package de.benshu.cofi.types.impl;

import com.google.inject.Inject;

import de.benshu.cofi.types.impl.declarations.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;
import de.benshu.cofi.types.impl.test.matcher.Subtype;
import de.benshu.cofi.types.impl.test.modules.NumberTypes;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static de.benshu.cofi.types.impl.test.Util.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Guice(modules = TestTypeSystemModule.class)
public class MethodInheritanceTest {
    @Inject
    private TestContext context;
    @Inject
    private TypeSystemImpl<TestContext> types;
    @Inject
    private NumberTypes numberTypes;
    @Inject
    private TypeBuilder typeBuilder;

    @Test
    public void whenInheritingFromTwoSupertypes_shouldIntersectReturnTypes() {
        SourceMethodDescriptor<TestContext> aIntegerReturn = md("method", msd(types(), numberTypes.getInteger()));
        SourceMethodDescriptor<TestContext> bPositiveRationalReturn = md("method", msd(types(), numberTypes.getPositiveRational()));

        TemplateTypeImpl<TestContext> supertypeA = typeBuilder.createTemplateType().called("A").extendingTop().declaring(mdm(aIntegerReturn)).applyTrivially();
        TemplateTypeImpl<TestContext> supertypeB = typeBuilder.createTemplateType().called("B").extendingTop().declaring(mdm(bPositiveRationalReturn)).applyTrivially();

        TemplateTypeImpl<TestContext> subtype = typeBuilder.createTemplateType().called("T").extending(supertypeA, supertypeB).declaringNoMembers().applyTrivially();

        AbstractMember<TestContext> method = subtype.lookupMember("method").get();
        ProperTypeMixin<TestContext, ?> methodType = method.getType().apply(AbstractTypeList.empty());

        assertThat(methodType, is(Subtype.of(context, types.constructFunction(types(), numberTypes.getInteger())).givenNoConstraints()));
        assertThat(methodType, is(Subtype.of(context, types.constructFunction(types(), numberTypes.getPositiveRational())).givenNoConstraints()));
    }
}
