package de.benshu.cofi.types.impl.test.modules;

import com.google.inject.Inject;

import de.benshu.cofi.types.impl.intersections.ConstructedIntersectionTypeImpl;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;

public class AnimalTypes {
    private final TemplateTypeImpl<TestContext> aquatic;
    private final TemplateTypeImpl<TestContext> mammal;

    private final ConstructedIntersectionTypeImpl<TestContext> aquaticMammal;
    private final TemplateTypeImpl<TestContext> platypus;

    @Inject
    public AnimalTypes(TypeBuilder typeBuilder) {
        this.aquatic = typeBuilder.createTemplateType().called("Aquatic").extendingTop().applyTrivially();
        this.mammal = typeBuilder.createTemplateType().called("Mammal").extendingTop().applyTrivially();

        this.aquaticMammal = typeBuilder.createIntersectionType().called("AquaticMammal").of(aquatic, mammal).applyTrivially();
        this.platypus = typeBuilder.createTemplateType().called("Platypus").extending(aquatic, mammal).declaringNoMembers().applyTrivially();
    }

    public TemplateTypeImpl<TestContext> getAquatic() {
        return aquatic;
    }

    public TemplateTypeImpl<TestContext> getMammal() {
        return mammal;
    }

    public ConstructedIntersectionTypeImpl<TestContext> getAquaticMammal() {
        return aquaticMammal;
    }

    public TemplateTypeImpl<TestContext> getPlatypus() {
        return platypus;
    }
}
