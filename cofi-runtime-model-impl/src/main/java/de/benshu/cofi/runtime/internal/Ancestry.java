package de.benshu.cofi.runtime.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import de.benshu.cofi.runtime.MemberDeclaration;
import de.benshu.cofi.runtime.ModelNode;
import de.benshu.cofi.types.Constraints;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.commons.core.Optional;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterators.singletonIterator;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;

public abstract class Ancestry implements Iterable<ModelNode> {
    private static final Nil EMPTY = new Nil();

    public static Ancestry empty() {
        return EMPTY;
    }

    public static Ancestry first(ModelNode adamOrEve) {
        return empty().append(adamOrEve);
    }

    private Ancestry() {}

    public Stream<ModelNode> ancestors() {
        return StreamSupport.stream(spliterator(), false);
    }

    public Ancestry append(ModelNode child) {
        return new Head(this, child);
    }

    public <N extends ModelNode> Optional<N> closest(Class<N> ancestorType) {
        return beginningWith(ancestorType).map(Head::getParent);
    }

    protected abstract <N extends ModelNode> Optional<Head<N>> beginningWith(Class<N> ancestorType);

    public TypeReferenceContext toTypeReferenceContext() {
        final Head<MemberDeclaration> memberDeclarationAncestry = beginningWith(MemberDeclaration.class).get();
        final MemberDeclaration memberDeclaration = memberDeclarationAncestry.getParent();
        final Optional<MemberDeclaration> outerMemberDeclaration = memberDeclarationAncestry.getGrandAncestry().closest(MemberDeclaration.class);

        return new TypeReferenceContext() {
            @Override
            public Optional<Constraints> getOuterConstraints() {
                return outerMemberDeclaration.map(d -> d.getTypeParameters().getConstraints());
            }

            @Override
            public Constraints getConstraints() {
                return memberDeclaration.getTypeParameters().getConstraints();
            }
        };
    }

    public <T> T construct(Constructor<T> constructor) {
        return constructor.construct(this);
    }

    public <T> ImmutableList<T> constructAll(ImmutableList<Constructor<T>> constructors) {
        return constructors.stream().map(this::construct).collect(list());
    }

    public <T> ImmutableSet<T> constructAll(ImmutableSet<Constructor<T>> constructors) {
        return constructors.stream().map(this::construct).collect(set());
    }

    public Supplier<TypeParameterList> resolve(TypeParameterListReference reference) {
        return MemoizingSupplier.of(() -> reference.resolve(this.toTypeReferenceContext()));
    }

    public <T extends Type> Supplier<T> resolve(TypeReference<T> reference) {
        return MemoizingSupplier.of(() -> reference.resolve(this.toTypeReferenceContext()));
    }

    @SuppressWarnings("unchecked")
    public <T extends Type> Supplier<TypeList<T>> resolveList(Collection<? extends TypeReference<? extends T>> references) {
        return MemoizingSupplier.of(() -> (TypeList<T>) TypeList.of(references.stream()
                .map(r -> r.resolve(this.toTypeReferenceContext()))
                .toArray(Type[]::new)));
    }

    public <T extends Type> Supplier<ImmutableSet<T>> resolveAll(ImmutableSet<? extends TypeReference<? extends T>> references) {
        return MemoizingSupplier.of(() -> references.stream()
                .map(r -> r.resolve(this.toTypeReferenceContext()))
                .collect(set()));
    }

    public <T extends Type> Supplier<ImmutableList<T>> resolveAll(ImmutableList<? extends TypeReference<? extends T>> references) {
        return MemoizingSupplier.of(() -> {
            return references.stream()
                    .map(r -> r.resolve(this.toTypeReferenceContext()))
                    .collect(list());
        });
    }

    private static class Head<N extends ModelNode> extends Ancestry {
        private final Ancestry grandAncestry;
        private final N parent;

        private Head(Ancestry grandAncestry, N parent) {
            this.grandAncestry = grandAncestry;
            this.parent = parent;
        }

        public N getParent() {
            return parent;
        }

        public Ancestry getGrandAncestry() {
            return grandAncestry;
        }

        @Override
        protected <T extends ModelNode> Optional<Head<T>> beginningWith(Class<T> ancestorType) {
            return ancestorType.isInstance(parent)
                    ? some(new Head<T>(grandAncestry, ancestorType.cast(parent)))
                    : grandAncestry.beginningWith(ancestorType);
        }

        @Override
        public Iterator<ModelNode> iterator() {
            return Iterators.concat(singletonIterator(parent), grandAncestry.iterator());
        }
    }

    private static class Nil extends Ancestry {
        @Override
        public Iterator<ModelNode> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        protected <N extends ModelNode> Optional<Head<N>> beginningWith(Class<N> ancestorType) {
            return none();
        }
    }
}
