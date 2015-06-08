package de.benshu.cofi.cofic.frontend.interfaces;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.model.impl.AbstractTypeDeclaration;
import de.benshu.cofi.model.impl.PropertyDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourcePropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.tags.IndividualTags;

class SourcePropertyDescriptorImpl implements SourcePropertyDescriptor<Pass> {
    private final Pass pass;
    private final ImmutableList<SourceType<Pass>> traits;
    private final PropertyDeclaration<Pass> propertyDeclaration;
    private final AbstractTypeDeclaration<Pass> owner;

    public SourcePropertyDescriptorImpl(Pass pass, ImmutableList<SourceType<Pass>> traits, PropertyDeclaration<Pass> propertyDeclaration, AbstractTypeDeclaration<Pass> owner) {
        this.pass = pass;
        this.traits = traits;
        this.propertyDeclaration = propertyDeclaration;
        this.owner = owner;
    }

    @Override
    public ImmutableList<SourceType<Pass>> getTraits() {
        return traits;
    }

    @Override
    public String getName() {
        return propertyDeclaration.getName();
    }

    @Override
    public IndividualTags getTags(Pass context) {
        return IndividualTags.empty();
    }

    @Override
    public SourceType<Pass> getValueType() {
        return SourceType.of(pass.lookUpProperTypeOf(propertyDeclaration.type));
    }
}
