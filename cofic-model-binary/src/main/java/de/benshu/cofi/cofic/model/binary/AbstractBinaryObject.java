package de.benshu.cofi.cofic.model.binary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.binary.deserialization.internal.BinaryModelContext;
import de.benshu.cofi.binary.deserialization.internal.UnboundType;
import de.benshu.cofi.binary.internal.Ancestry;
import de.benshu.cofi.binary.internal.Constructor;
import de.benshu.cofi.cofic.model.binary.internal.TypeParameterListReference;
import de.benshu.cofi.cofic.model.binary.internal.TypeReference;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.CombinableSourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;

import static de.benshu.cofi.cofic.model.binary.internal.Resolution.resolveAll;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static java.util.function.Function.identity;

public abstract class AbstractBinaryObject extends AbstractBinaryTypeDeclaration {
    private final String postfix;
    private final ImmutableList<UnboundType> supertypes;

    AbstractBinaryObject(
            Ancestry ancestry,
            ImmutableSet<Constructor<BinaryAnnotation>> annotations,
            String name,
            String postfix,
            TypeParameterListReference typeParameters,
            ImmutableList<TypeReference> supertypes,
            Constructor<BinaryTypeBody> body) {
        super(ancestry, annotations, name, typeParameters, body);

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.postfix = postfix;
        this.supertypes = resolveAll(ancestryIncludingMe, supertypes);
    }

    @Override
    public <X extends BinaryModelContext<X>> ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return AbstractTemplateTypeConstructor.<X>create(TemplateTypeDeclaration.memoizing(
                (x, b) -> bindTypeParameters(x),
                (x, b) -> supertypes.stream().map(t -> SourceType.of(t.bind(x))).collect(list()),
                (x, b) -> SourceMemberDescriptors.create(b.getParameters().getConstraints(), getMemberDeclarations()
                        .map(d -> d.toDescriptor(context))
                        .collect(setMultimap(SourceMemberDescriptor::getName, identity()))
                        .asMap().values().stream()
                        .map(ds -> ds.stream().reduce(CombinableSourceMemberDescriptor::combineWith).get())
                        .collect(set())),
                (x, b) -> individualTags(context).set(TypeTags.NAME, FullyQualifiedTypeName.create(getFqn(), postfix))
        )).bind(context);
    }

    protected <X extends BinaryModelContext<X>> IndividualTags individualTags(X context) {
        return IndividualTags.empty();
    }
}
