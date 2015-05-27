package de.benshu.cofi.types.impl.members;

import com.google.common.base.Equivalence;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;

import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.bound.Method;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ConstructedTypeMixin;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeEquivalence;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.InterpretedMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodDescriptor;
import de.benshu.cofi.types.impl.declarations.InterpretedMethodSignatureDescriptor;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.tags.TagCombiner;
import de.benshu.cofi.types.impl.tags.TagCombiners;
import de.benshu.cofi.types.impl.tags.Tagger;
import de.benshu.cofi.types.tags.HashTags;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.Tables.immutableCell;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.table;

public final class MethodImpl<X extends TypeSystemContext<X>> extends AbstractMember<X> implements Method<X> {
    public static <X extends TypeSystemContext<X>> MethodImpl<X> createMethod(X context, ProperTypeMixin<X, ?> owner, InterpretedMethodDescriptor<X> descriptor) {
        return create(
                context,
                owner,
                descriptor.getName(),
                normalizeSignatures(
                        context,
                        descriptor.getMethodSignatureDescriptors().stream()
                                .map(d -> toSignature(context, d))
                                .collect(list())::stream,
                        TagCombiners::intersect // TODO need some other combiner
                ),
                tagged -> HashTags.create(tagged, descriptor.getTags(context))
        );
    }

    private static <X extends TypeSystemContext<X>> MethodImpl<X> create(X context, ProperTypeMixin<X, ?> owner, String name, ImmutableList<MethodSignatureImpl<X>> signatures, Tagger tagger) {
        final TypeParameterListImpl<X> typeParameters = signatures.get(0).typeParameters;

        final ProperTypeMixin<X, ?> combinedType = AnonymousIntersectionType.createIfNonTrivial(context, signatures.stream()
                .map(s -> s.getType().apply(typeParameters.getVariables()))
                .collect(typeList()));

        final ProperTypeConstructorMixin<X, ?, ?> combinedTypeConstructor = AdHoc.properTypeConstructor(
                context, typeParameters, combinedType.substitutable());

        return new MethodImpl<>(context, owner, name, combinedTypeConstructor, signatures, tagger);
    }

    private static <X extends TypeSystemContext<X>> MethodSignatureImpl<X> toSignature(X context, InterpretedMethodSignatureDescriptor<X> signatureDescriptor) {
        ProperTypeMixin<X, ?> returnType = signatureDescriptor.getReturnType();
        for (int i = signatureDescriptor.getParameterTypes().size() - 1; i >= 1; --i) {
            returnType = context.getTypeSystem().constructFunction(signatureDescriptor.getParameterTypes().get(i), returnType);
        }
        final AbstractTypeList<X, ProperTypeMixin<X, ?>> parameterTypes = signatureDescriptor.getParameterTypes().get(0);


        return new MethodSignatureImpl<>(
                -1,
                signatureDescriptor.getTypeParameters(),
                parameterTypes,
                returnType,
                ImmutableSet.of(),
                tagged -> HashTags.create(tagged, signatureDescriptor.getTags())
        );
    }

    private static <X extends TypeSystemContext<X>> ImmutableList<MethodSignatureImpl<X>> normalizeSignatures(X context, Supplier<Stream<MethodSignatureImpl<X>>> signaturesToNormalize, TagCombiner tagCombiner) {
        final MethodSignatureComparator<X> signatureComparator = new MethodSignatureComparator<>(context);

        final ImmutableTable<MethodSignatureImpl<X>, MethodSignatureImpl<X>, MethodSignatureRelation> relations = signaturesToNormalize.get()
                .flatMap(s -> signaturesToNormalize.get().map(o -> Pair.of(s, o)))
                .flatMap(p -> {
                    final MethodSignatureRelation relation = signatureComparator.compare(p.a, p.b);
                    return Stream.of(
                            immutableCell(p.a, p.b, relation),
                            immutableCell(p.b, p.a, relation.reverse())
                    );
                })
                .distinct()
                .collect(table());

        final Map<MethodSignatureImpl<X>, MethodSignatureImpl<X>> processed = new HashMap<>();
        final List<MethodSignatureImpl<X>> combinedSignatures = Lists.newArrayList();

        signaturesToNormalize.get()
                .sorted((a, b) -> relations.get(a, b).asComparatorValue())
                .forEachOrdered(s -> {
                    if (processed.containsKey(s))
                        return;

                    final ImmutableSet<MethodSignatureImpl<X>> supersignatures = Lists.reverse(combinedSignatures).stream()
                            .filter(c -> relations.get(s, c) == MethodSignatureRelation.SUBSIGNATURE)
                            .collect(set());

                    final ImmutableSet<MethodSignatureImpl<X>> equivalenceClass = relations.rowMap().get(s).entrySet().stream()
                            .filter(e -> e.getValue() == MethodSignatureRelation.EQUAL)
                            .map(Map.Entry::getKey)
                            .collect(set());

                    final MethodSignatureImpl<X> combinedSignature = combine(combinedSignatures.size(), context, equivalenceClass, supersignatures, tagCombiner);

                    combinedSignatures.add(combinedSignature);
                    equivalenceClass.stream().forEach(
                            p -> processed.put(p, combinedSignature));
                });

        return ImmutableList.copyOf(combinedSignatures);
    }

    private static <X extends TypeSystemContext<X>> MethodSignatureImpl<X> combine(int index, X context, ImmutableSet<MethodSignatureImpl<X>> equivalenceClass, ImmutableSet<MethodSignatureImpl<X>> supersignatures, TagCombiner tagCombiner) {
        final MethodSignatureImpl<X> first = equivalenceClass.iterator().next();

        final TypeEquivalence<X> equivalence = TypeEquivalence.given(equivalenceClass.iterator().next().typeParameters.getConstraints());

        final ProperTypeMixin<X, ?> combinedReturnType = AnonymousIntersectionType.createIfNonTrivial(context, equivalenceClass.stream()
                .map(s -> s.returnType)
                .map(equivalence::wrap)
                .distinct()
                .map(Equivalence.Wrapper::get)
                .collect(typeList()));

        final Tagger combinedTags = tagCombiner.combine(equivalenceClass.stream()
                .map(MethodSignatureImpl::getTags)
                .collect(set()));

        return new MethodSignatureImpl<>(index, first.typeParameters, first.parameterTypes, combinedReturnType, supersignatures, combinedTags);
    }

    private final ProperTypeMixin<X, ?> owner;
    private final String name;
    private final ProperTypeConstructorMixin<X, ?, ?> type;
    private final ImmutableList<MethodSignatureImpl<X>> signatures;
    private final Tags tags;

    private MethodImpl(X context, ProperTypeMixin<X, ?> owner, String name, ProperTypeConstructorMixin<X, ?, ?> type, ImmutableList<MethodSignatureImpl<X>> signatures, Tagger tagger) {
        super(context);

        this.owner = owner;
        this.name = name;
        this.type = type;
        this.signatures = signatures;
        this.tags = tagger.tag(this);
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.METHOD;
    }

    @Override
    public ProperTypeMixin<X, ?> getOwner() {
        return owner;
    }

    @Override
    public MethodSignatureImpl<X> getRootSignature() {
        return getSignatures().get(0);
    }

    @Override
    public ImmutableList<MethodSignatureImpl<X>> getSignatures() {
        return signatures;
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> getType() {
        return type;
    }

    @Override
    public MethodImpl<X> intersectWith(AbstractConstraints<X> contextualConstraints, AbstractMember<X> otherMember) {
        final MethodImpl<X> otherMethod = (MethodImpl<X>) otherMember;

        return combineWith(context, otherMethod, AnonymousIntersectionType::createIfNonTrivial, TagCombiners::intersect);
    }

    @Override
    public MethodImpl<X> refine(AbstractConstraints<X> contextualConstraints, InterpretedMemberDescriptor<X> descriptor) {
        final InterpretedMethodDescriptor<X> methodDescriptor = (InterpretedMethodDescriptor<X>) descriptor;

        final ImmutableList<MethodSignatureImpl<X>> refinedSignatures = combineSignaturesWith(
                context,
                methodDescriptor.getMethodSignatureDescriptors().stream()
                        .map(d -> toSignature(context, d))
                        .collect(list())::stream,
                TagCombiners::intersect); // TODO wrong TagCombiner

        return create(context, owner, name, refinedSignatures, TagCombiners.refine(getTags(), descriptor.getTags(context)));
    }

    @Override
    public MethodImpl<X> bequest(ProperTypeConstructorMixin<X, ?, ?> newOwner) {
        final ConstructedTypeMixin<X, ?, ?> currentOwner = (ConstructedTypeMixin<X, ?, ?>) owner;

        final ImmutableList<MethodSignatureImpl<X>> bequeathedSignatures = this.signatures.stream()
                .map(s -> s.bequest(newOwner, currentOwner.getArgumentsAsSubstitutions()))
                .collect(list());

        return new MethodImpl<>(context, newOwner.applyTrivially(), name, type, bequeathedSignatures, TagCombiners.bequest(getTags()));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MethodImpl<X> substitute(ProperTypeMixin<X, ?> substitutedOwner, Substitutions<X> substitutions) {
        List<MethodSignatureImpl<X>> substitutedSignatures = Lists.newArrayList();

        for (MethodSignatureImpl<X> unsubstitutedSignature : signatures) {
            substitutedSignatures.add(new MethodSignatureImpl<>(
                    unsubstitutedSignature.index,
                    unsubstitutedSignature.typeParameters, // TODO substitute constraints?!
                    unsubstitutedSignature.parameterTypes.stream()
                            .map(p -> p.substitute(substitutions))
                            .collect(typeList()),
                    unsubstitutedSignature.returnType.substitute(substitutions),
                    unsubstitutedSignature.supersignatures.stream()
                            .map(s -> substitutedSignatures.get(s.index))
                            .collect(set()),
                    TagCombiners.substitute(unsubstitutedSignature.getTags())
            ));
        }

        return create(context, substitutedOwner, name, ImmutableList.copyOf(substitutedSignatures), TagCombiners.substitute(getTags()));
    }

    @Override
    public de.benshu.cofi.types.Method unbind() {
        return new Unbound<>(this, context);
    }

    private MethodImpl<X> combineWith(X context, MethodImpl<X> otherMethod, BiFunction<X, AbstractTypeList<X, ProperTypeMixin<X, ?>>, ProperTypeMixin<X, ?>> ownerCombiner, TagCombiner tagCombiner) {
        ProperTypeMixin<X, ?> combinedOwner = ownerCombiner.apply(context, AbstractTypeList.of(owner, otherMethod.getOwner()));
        ImmutableList<MethodSignatureImpl<X>> intersectedSignatures = combineSignaturesWith(context, otherMethod.getSignatures()::stream, tagCombiner);

        final Tagger tagger = tagCombiner.combine(tags, otherMethod.tags);

        return create(context, combinedOwner, name, intersectedSignatures, tagger);
    }

    private ImmutableList<MethodSignatureImpl<X>> combineSignaturesWith(X context, Supplier<Stream<MethodSignatureImpl<X>>> otherSignaturesSupplier, TagCombiner tagCombiner) {
        return normalizeSignatures(context, () -> Stream.concat(getSignatures().stream(), otherSignaturesSupplier.get()), tagCombiner);
    }

    private static class Unbound<X extends TypeSystemContext<X>>
            extends AbstractUnboundMember<X, MethodImpl<X>>
            implements de.benshu.cofi.types.Method {

        public Unbound(MethodImpl<X> unbound, X context) {
            super(unbound);
        }

        @Override
        public Signature getRootSignature() {
            return unbound.getRootSignature().unbind();
        }

        @Override
        public ImmutableList<? extends Signature> getSignatures() {
            return unbound.getSignatures().stream()
                    .map(MethodSignatureImpl::unbind)
                    .collect(list());
        }
    }
}