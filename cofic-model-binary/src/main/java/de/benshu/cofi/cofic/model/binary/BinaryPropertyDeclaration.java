package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourcePropertyDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;
import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveAll;
import static de.benshu.commons.core.streams.Collectors.list;

public class BinaryPropertyDeclaration extends BinaryTypeBody.Containable implements BinaryMemberDeclaration {
    private final ImmutableSet<BinaryAnnotation> annotations;
    private final Fqn fqn;
    private final UnboundTypeParameterList typeParameters;
    private final ImmutableList<UnboundType> traits;
    private final UnboundType valueType;

    public BinaryPropertyDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> traits,
            TypeReference valueType) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = ancestry.closest(BinaryMemberDeclaration.class).get().getFqn().getChild(name);
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.traits = resolveAll(ancestryIncludingMe, traits);
        this.valueType = resolve(ancestryIncludingMe, valueType);
    }

    @Override
    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context) {
        return typeParameters.bind(context);
    }

    @Override
    public <X extends BinaryModelContext<X>> CombinableSourceMemberDescriptor<X> toDescriptor(X context) {
        return new Descriptor<>(context);
    }

    private class Descriptor<X extends BinaryModelContext<X>> implements SourcePropertyDescriptor<X>, CombinableSourceMemberDescriptor<X> {
        private final X context;

        public Descriptor(X context) {this.context = context;}

        @Override
        public SourceType<X> getValueType() {
            return SourceType.<X>of(valueType.<X>bind(context));
        }

        @Override
        public ImmutableList<SourceType<X>> getTraits() {
            return traits.stream()
                    .map(t -> SourceType.of(t.<X>bind(context)))
                    .collect(list());
        }

        @Override
        public String getName() {
            return BinaryPropertyDeclaration.this.getName();
        }

        @Override
        public CombinableSourceMemberDescriptor<X> combineWith(CombinableSourceMemberDescriptor<X> other) {
            throw new UnsupportedOperationException();
        }
    }
}
