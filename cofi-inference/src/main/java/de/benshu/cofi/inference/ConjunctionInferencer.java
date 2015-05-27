package de.benshu.cofi.inference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.unions.AnonymousUnionType;
import de.benshu.cofi.types.impl.Bottom;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.Substitutions;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.commons.core.Optional;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

final class ConjunctionInferencer<X extends TypeSystemContext<X>> extends InternalInferencer<X> {
    private final Monosemous<X> cs;
    private final Inferencer.Mode mode;
    private final TypeSystemImpl<X> langTypes;
    private Iterable<AbstractTypeList<X, ?>> delegate;
    private ProperTypeMixin<X, ?>[] given;

    public ConjunctionInferencer(TypeSystemImpl<X> langTypes, Monosemous<X> cs, Inferencer.Mode mode, ProperTypeMixin<X, ?>[] given) {
        this.langTypes = langTypes;
        this.cs = cs;
        this.mode = mode;
        this.given = given;
    }

    @Override
    public Iterator<AbstractTypeList<X, ?>> iterator(X context) {
        return infer(context).iterator();
    }

    private Iterable<AbstractTypeList<X, ?>> infer(X context) {
        return Iterables.concat(
                simplyfyFirst(context),
                restrictMetasFirst(context),
                inferDefault(context)
        ).iterator().next();
    }

    private Optional<Iterable<AbstractTypeList<X, ?>>> simplyfyFirst(X context) {
        final AbstractConstraints<X> s = cs.simplify(context);

        if (s.equals(cs))
            return Optional.none();
        else if (s.isAll())
            return Optional.some(ImmutableList.<AbstractTypeList<X, ?>>of());
        else {
            return Optional.some(() -> Iterators.concat(Iterables.transform(inferencers(langTypes, s, mode, null), i -> i.iterator(context)).iterator()));
        }
    }

    private Optional<Iterable<AbstractTypeList<X, ?>>> restrictMetasFirst(X context) {
        if (given != null)
            return Optional.none();

        final ImmutableList.Builder<InternalInferencer<X>> builder = ImmutableList.builder();
        restrictMetas(context, builder, new ProperTypeMixin[cs.getTypeParams().size()], 0);
        final ImmutableList<InternalInferencer<X>> delegates = builder.build();

        if (!delegates.isEmpty()) {
            return Optional.some(() -> Iterators.concat(Iterables.transform(builder.build(), i -> i.iterator(context)).iterator()));
        }

        return Optional.none();
    }

    private void restrictMetas(X context, ImmutableList.Builder<InternalInferencer<X>> builder, ProperTypeMixin<X, ?>[] types, int index) {
        if (index >= types.length) {
            builder.addAll(restrictMetas(context, types));
            return;
        }

        final TypeVariableImpl<X, ?> v = cs.getTypeParams().getVariables().get(index);

        if (cs.getMetas(v).isEmpty()) {
            types[index] = null;
            restrictMetas(context, builder, types, index + 1);
        } else {
            // ??? maybe do both, but vary order based on mode??
            final AbstractTypeList<X, ProperTypeMixin<X, ?>> bounds = mode == Inferencer.Mode.GENERIC ? cs.getUppers(v) : cs.getLowers(v);

            for (ProperTypeMixin<X, ?> b : bounds) {
                types[index] = b;
                restrictMetas(context, builder, types, index + 1);
            }
        }
    }

    private ImmutableList<InternalInferencer<X>> restrictMetas(X context, ProperTypeMixin<X, ?>[] types) {
        AbstractConstraints<X> restricted = cs;

        for (int i = 0; i < types.length; ++i) {
            final ProperTypeMixin<X, ?> t = types[i];

            if (t == null) {
                continue;
            }

            final TypeVariableImpl<X, ?> v = cs.getTypeParams().getVariables().get(i);
            AbstractTypeList<X, ProperTypeMixin<X, ?>> bounds = mode == Inferencer.Mode.GENERIC ? cs.getUppers(v) : cs.getLowers(v);

            for (TypeMixin<X, ?> b : cs.getMetas(v)) {
                throw null;
//                restricted = restricted.establishSubtype(context, t.getMetaType(context), b);
            }

            for (TypeMixin<X, ?> b : bounds) {
                restricted = mode == Inferencer.Mode.GENERIC ? restricted.establishSubtype(t, b) : restricted.establishSubtype(b, t);
            }
        }

        if (restricted == cs) {
            return ImmutableList.of();
        }

        restricted = restricted.simplify(context);
        return inferencers(langTypes, restricted, mode, types);
    }

    private Optional<Iterable<AbstractTypeList<X, ?>>> inferDefault(X context) {
        final TypeParameterListImpl<X> params = cs.getTypeParams();
        final AbstractTypeList<X, TypeVariableImpl<X, ?>> vars = params.getVariables();

        final List<TypeVariableImpl<X, ?>> queued = vars.stream().collect(Collectors.toList());
        final Bottom<X> bot = langTypes.getBottom();
        final TypeMixin<X, ?>[] inferred = new TypeMixin[params.size()];
        Arrays.fill(inferred, bot);

        next:
        while (!queued.isEmpty()) {
            final Substitutions<X> substitutions = Substitutions.ofThrough(params, AbstractTypeList.of(inferred));

            for (TypeVariableImpl<X, ?> v : queued) {
                final int i = v.getParameter().getIndex();

                if (given == null || given[i] == null) {
                    final AbstractTypeList<X, ?> bounds = mode == Inferencer.Mode.GENERIC ? cs.getUppers(v) : cs.getLowers(v);

                    if (bounds.stream().anyMatch(b -> b.containsAny(queued))) {
                        continue;
                    }

                    if (bounds.size() == 1) {
                        inferred[i] = bounds.get(0).substitute(substitutions);
                    } else if (mode == Inferencer.Mode.GENERIC) {
                        inferred[i] = AnonymousIntersectionType.createIfNonTrivial(context, (AbstractTypeList<X, ProperTypeMixin<X, ?>>) bounds.substitute(substitutions));
                    } else {
                        inferred[i] = AnonymousUnionType.create(context, (AbstractTypeList<X, ProperTypeMixin<X, ?>>) bounds.substitute(substitutions));
                    }
                } else if (given[i].containsAny(queued)) {
                    continue;
                } else {
                    inferred[i] = given[i].substitute(substitutions);
                }

                queued.remove(v);
                continue next;
            }

            return Optional.some(ImmutableList.of());
        }

        return Optional.some(ImmutableList.of(AbstractTypeList.of(inferred)));
    }
}
