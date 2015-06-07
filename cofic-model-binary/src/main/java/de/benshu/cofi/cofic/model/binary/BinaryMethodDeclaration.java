package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.deserialization.internal.UnboundTypeParameterList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.functions.FunctionType;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolve;
import static de.benshu.commons.core.streams.Collectors.list;

public class BinaryMethodDeclaration extends BinaryTypeBody.Containable implements BinaryMemberDeclaration {
    private final ImmutableSet<BinaryAnnotation> annotations;
    private final Fqn fqn;

    private final UnboundTypeParameterList typeParameters;
    private final UnboundType signature;

    public BinaryMethodDeclaration(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            TypeParameterListReference typeParameters,
            TypeReference signature) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.annotations = ancestryIncludingMe.constructAll(annotations);
        this.fqn = ancestry.closest(BinaryMemberDeclaration.class).get().getFqn().getChild(name);
        this.typeParameters = resolve(ancestryIncludingMe, typeParameters);
        this.signature = resolve(ancestryIncludingMe, signature);
    }

    @Override
    public Fqn getFqn() {
        return fqn;
    }

    @Override
    public <X extends BinaryModelContext<X>> CombinableSourceMemberDescriptor<X> toDescriptor(X context) {
        return new Descriptor<>(getName(), ImmutableList.of(new SignatureDescriptor<>(context)));
    }

    @Override
    public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bindTypeParameters(X context) {
        return typeParameters.bind(context);
    }

    private static class Descriptor<X extends BinaryModelContext<X>> implements SourceMethodDescriptor<X>, CombinableSourceMemberDescriptor<X> {
        private final String name;
        private final ImmutableList<SourceMethodSignatureDescriptor<X>> signatureDescriptors;

        private Descriptor(String name, ImmutableList<SourceMethodSignatureDescriptor<X>> signatureDescriptors) {
            this.name = name;
            this.signatureDescriptors = signatureDescriptors;
        }

        @Override
        public ImmutableList<SourceMethodSignatureDescriptor<X>> getMethodSignatureDescriptors() {
            return signatureDescriptors;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public CombinableSourceMemberDescriptor<X> combineWith(CombinableSourceMemberDescriptor<X> other) {
            checkArgument(other.getName().equals(getName()));

            final ImmutableList<SourceMethodSignatureDescriptor<X>> combinedSignatureDescriptors = Stream.concat(
                    getMethodSignatureDescriptors().stream(),
                    ((Descriptor<X>) other).getMethodSignatureDescriptors().stream()
            ).collect(list());

            return new Descriptor<>(name, combinedSignatureDescriptors);

        }
    }

    private class SignatureDescriptor<X extends BinaryModelContext<X>> implements SourceMethodSignatureDescriptor<X> {
        private final X context;

        public SignatureDescriptor(X context) {
            this.context = context;
        }

        @Override
        public TypeParameterListImpl<X> getTypeParameters() {
            return bindTypeParameters(context);
        }

        @Override
        public ImmutableList<ImmutableList<SourceType<X>>> getParameterTypes() {
            final ImmutableList.Builder<ImmutableList<SourceType<X>>> builder = ImmutableList.builder();

            Optional<FunctionType<X>> current = FunctionType.tryFrom(context, signature.<X>bind(context));
            while (current.isPresent()) {
                builder.add(current.get().getParameterTypes().stream()
                        .map(SourceType::of)
                        .collect(list()));
                current = current.get().tryGetReturnTypeAsFunction(context);
            }

            return builder.build();
        }

        @Override
        public SourceType<X> getReturnType() {
            return getReturnTypeFrom(FunctionType.forceFrom(context, signature.<X>bind(context)));
        }

        private SourceType<X> getReturnTypeFrom(FunctionType<X> functionType) {
            return functionType.tryGetReturnTypeAsFunction(context)
                    .map(this::getReturnTypeFrom)
                    .getOrSupply(() -> SourceType.of(functionType.getReturnType()));
        }
    }
}
