package de.benshu.cofi.types.impl;

import com.google.inject.Inject;

import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.matcher.IterableMatchers;
import de.benshu.cofi.types.impl.test.modules.TestTypeSystemModule;

import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Guice(modules = TestTypeSystemModule.class)
public class TopTest {
    @Inject
    private TestContext context;
    @Inject
    private TypeSystemImpl<TestContext> types;

    @Test
    public void topHasNoSupertypes() {
        TemplateTypeImpl<TestContext> top = types.getTop();

        assertThat(top.getSupertypes(), is(IterableMatchers.empty()));
    }
}
