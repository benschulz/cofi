package de.benshu.cofi.types.impl.test.modules;

import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;
import static de.benshu.cofi.types.Variance.INVARIANT;

public class CollectionTypes {
    private final TemplateTypeImpl<TestContext> list;

    public CollectionTypes(TypeBuilder typeBuilder) {
        this.list = typeBuilder.createTemplateType()
                .called("cofi", "collect", "TypeParameterList")
                .parametrizedBy(typeBuilder.createParameters().called("E").withVariances(INVARIANT).triviallyConstrained()).extendingTop().declaringNoMembers().applyTrivially();
    }

    public TemplateTypeImpl<TestContext> getList() {
        return list;
    }
}
