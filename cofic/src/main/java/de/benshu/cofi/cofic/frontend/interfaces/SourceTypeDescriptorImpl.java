package de.benshu.cofi.cofic.frontend.interfaces;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.declarations.source.SourceTypeDescriptor;
import de.benshu.cofi.types.tags.IndividualTags;

class SourceTypeDescriptorImpl implements SourceTypeDescriptor<Pass> {
    private final AbstractTypeDeclaration<Pass> declaration;
    private final String name;
    private final IndividualTags tags;

    public SourceTypeDescriptorImpl(AbstractTypeDeclaration<Pass> declaration, String name, IndividualTags tags) {
        this.declaration = declaration;
        this.name = name;
        this.tags = tags;
    }

    @Override
    public IndividualTags getTags(Pass context) {
        return tags;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SourceType<Pass> getType(Pass context) {
        return SourceType.of(context.lookUpTypeOf(declaration));
    }
}
