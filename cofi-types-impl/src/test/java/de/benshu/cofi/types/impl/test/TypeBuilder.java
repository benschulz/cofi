package de.benshu.cofi.types.impl.test;

import com.google.common.base.Functions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.AbstractProperType;
import de.benshu.cofi.types.impl.AbstractTypeConstructor;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.Error;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.IntersectionTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.TypeDeclaration;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptor;
import de.benshu.cofi.types.impl.declarations.source.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.intersections.AbstractIntersectionTypeConstructor;
import de.benshu.cofi.types.impl.intersections.ConstructedIntersectionTypeImpl;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.impl.unions.ConstructedUnionTypeImpl;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.function.Function;
import java.util.function.Supplier;

import static de.benshu.cofi.types.impl.test.Util.flattenFqn;
import static de.benshu.commons.core.streams.Collectors.list;

public class TypeBuilder {
    private final TestContext context;
    private final TypeSystemImpl<TestContext> typeSystem;
    private final TypeDeclarationFactory typeDeclarationFactory;

    public TypeBuilder(TestContext context, TypeSystemImpl<TestContext> typeSystem, TypeDeclarationFactory typeDeclarationFactory) {
        this.context = context;
        this.typeSystem = typeSystem;
        this.typeDeclarationFactory = typeDeclarationFactory;
    }

    public TemplateTypeToBeNamed createTemplateType() {
        return new TemplateTypeToBeNamed();
    }

    public ParametersToBeNamed createParameters() {
        return new ParametersToBeNamed();
    }

    public IntersectionOrUnionTypeToBeNamed<IntersectionTypeDeclaration<TestContext>, AbstractIntersectionTypeConstructor<TestContext>, ConstructedIntersectionTypeImpl<TestContext>> createIntersectionType() {
        return new IntersectionOrUnionTypeToBeNamed<>(
                name -> parameters -> elements -> tieTheKnot(m -> AbstractIntersectionTypeConstructor.create(
                        typeDeclarationFactory.createIntersectionTypeDeclaration(() -> parameters, () -> elements, () -> StringNameTag.labeled(name))
                ).bind(context)));
    }

    public IntersectionOrUnionTypeToBeNamed<UnionTypeDeclaration<TestContext>, AbstractUnionTypeConstructor<TestContext>, ConstructedUnionTypeImpl<TestContext>> createUnionType() {
        return new IntersectionOrUnionTypeToBeNamed<>(
                name -> parameters -> elements -> tieTheKnot(m -> AbstractUnionTypeConstructor.create(
                        typeDeclarationFactory.createUnionTypeDeclaration(() -> parameters, () -> elements, () -> StringNameTag.labeled(name))
                ).bind(context)));
    }

    public Error<TestContext> createErrorType() {
        return createErrorType("-Error-");
    }

    public Error<TestContext> createErrorType(String name) {
        return Error.create(StringNameTag.labeled(name));
    }

    private <C extends AbstractTypeConstructor<TestContext, ?, T>, T extends TypeMixin<TestContext, ?>> C tieTheKnot(
            Function<Supplier<AbstractTypeConstructor<TestContext, ?, TemplateTypeImpl<TestContext>>>, C> typeFactory
    ) {
        return tieTheKnot(
                t -> AdHoc.typeConstructor(
                        context, t.getParameters(),
                        typeSystem.getMetaType().apply(AbstractTypeList.of(t.applyTrivially()))),
                typeFactory

        );
    }

    // and what a knot it is...
    private <C extends AbstractTypeConstructor<TestContext, ?, T>, T extends TypeMixin<TestContext, ?>> C tieTheKnot(
            Function<C, AbstractTypeConstructor<TestContext, ?, TemplateTypeImpl<TestContext>>> metaTypeFactory,
            Function<Supplier<AbstractTypeConstructor<TestContext, ?, TemplateTypeImpl<TestContext>>>, C> typeFactory
    ) {
        // *sadface 2 * TODO extract this "pattern" to somewhere else (also used in TestTypeSytemModule)
        @SuppressWarnings("unchecked")
        C[] type = (C[]) new AbstractTypeConstructor<?, ?, ?>[1];
        @SuppressWarnings("unchecked")
        AbstractTypeConstructor<TestContext, ?, TemplateTypeImpl<TestContext>>[] metaType = (AbstractTypeConstructor<TestContext, ?, TemplateTypeImpl<TestContext>>[]) new AbstractTypeConstructor<?, ?, ?>[1];

        return type[0] = typeFactory.apply(() -> metaType[0] == null
                ? metaType[0] = metaTypeFactory.apply(type[0])
                : metaType[0]);
    }

    public class ParametersToBeConstrained {
        private final ParameterListDeclaration declaration;
        private final TypeParameterListImpl<TestContext> params;

        private ParametersToBeConstrained(ParameterListDeclaration declaration, TypeParameterListImpl<TestContext> params) {
            this.declaration = declaration;
            this.params = params;
        }

        public TypeParameterListImpl<TestContext> triviallyConstrained() {
            declaration.setConstraints(AbstractConstraints.trivial(context, params));
            return params;
        }

        @SafeVarargs
        public final TypeParameterListImpl<TestContext> constrainedToBeSubtypesOf(TypeMixin<TestContext, ?>... upperBounds) {
            final AbstractConstraints<TestContext> trivial = AbstractConstraints.trivial(context, params);

            final AbstractConstraints<TestContext> constraints = ContiguousSet.create(Range.closedOpen(0, params.size()), DiscreteDomain.integers()).stream().reduce(trivial,
                    (a, b) -> a.establishSubtype(params.getVariables().get(b),
                            upperBounds[b]), AbstractConstraints::and);

            declaration.setConstraints(constraints);
            return params;
        }
    }

    public class ParametersToBeVarianceClassified {
        private ImmutableList<String> names;

        private ParametersToBeVarianceClassified(ImmutableList<String> names) {
            this.names = names;
        }

        public ParametersToBeConstrained withVariances(Variance... variances) {
            return withVariances(ImmutableList.copyOf(variances));
        }

        private ParametersToBeConstrained withVariances(ImmutableList<? extends Variance> variances) {
            final ParameterListDeclaration declaration = new ParameterListDeclaration(variances, names);
            TypeParameterListImpl<TestContext> params = TypeParameterListImpl.create(declaration).bind(context);
            return new ParametersToBeConstrained(declaration, params);
        }

        public TypeParameterListImpl<TestContext> triviallyConstrained() {
            return allInvariant().triviallyConstrained();
        }

        private ParametersToBeConstrained allInvariant() {
            ImmutableList<Variance> variances = ImmutableList.copyOf(Lists.transform(names,
                    Functions.constant(Variance.INVARIANT)));
            return withVariances(variances);
        }
    }

    private static class ParameterListDeclaration implements TypeParameterListDeclaration<TestContext> {
        private final ImmutableList<? extends Variance> variances;
        private final ImmutableList<String> names;

        private AbstractConstraints<TestContext> constraints;

        public ParameterListDeclaration(ImmutableList<? extends Variance> variances, ImmutableList<String> names) {
            this.variances = variances;
            this.names = names;
        }

        @Override
        public <O> O supplyParameters(TestContext context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
            final ImmutableList<Pair<Variance, IndividualTags>> up = Pair.up(variances, names).stream()
                    .map(p -> Pair.of(p.a, StringNameTag.labeled(p.b)))
                    .collect(list());

            return interpreter.interpret(up, context.getChecker());
        }

        @Override
        public <O> O supplyConstraints(TestContext context, TypeParameterListImpl<TestContext> bound, Interpreter<AbstractConstraints<TestContext>, O> interpreter) {
            return interpreter.interpret(constraints, context.getChecker());
        }

        public void setConstraints(AbstractConstraints<TestContext> constraints) {
            this.constraints = constraints;
        }
    }

    public class ParametersToBeNamed {
        public ParametersToBeVarianceClassified called(String... names) {
            return new ParametersToBeVarianceClassified(ImmutableList.copyOf(names));
        }
    }

    public class TemplateTypeToBeNamed {
        public TemplateTypeToBeParametrized called(String... fqn) {
            return new TemplateTypeToBeParametrized(flattenFqn(fqn));
        }
    }

    public class TemplateTypeToBeParametrized {
        private String name;

        private TemplateTypeToBeParametrized(String name) {
            this.name = name;
        }

        public TemplateTypeToBeOrdered parametrizedBy(TypeParameterListImpl<TestContext> parameters) {
            return new TemplateTypeToBeOrdered(name, parameters);
        }

        @SafeVarargs
        public final TemplateTypeToFilled extending(TemplateTypeImpl<TestContext>... supertypes) {
            return notParametrized().extending(supertypes);
        }

        public TemplateTypeToFilled extendingTop() {
            return notParametrized().extendingTop();
        }

        private TemplateTypeToBeOrdered notParametrized() {
            return parametrizedBy(TypeParameterListImpl.empty());
        }
    }

    public class TemplateTypeToBeOrdered {
        private final String name;
        private final TypeParameterListImpl<TestContext> parameters;

        public TemplateTypeToBeOrdered(String name, TypeParameterListImpl<TestContext> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public TemplateTypeToFilled extendingTop() {
            return extending(typeSystem.getTop());
        }

        @SafeVarargs
        public final TemplateTypeToFilled extending(TemplateTypeImpl<TestContext>... supertypes) {
            return new TemplateTypeToFilled(name, parameters, supertypes);
        }
    }

    public class TemplateTypeToFilled {
        private final String name;
        private final TypeParameterListImpl<TestContext> parameters;
        private final TemplateTypeImpl<TestContext>[] supertypes;

        public TemplateTypeToFilled(String name, TypeParameterListImpl<TestContext> parameters, TemplateTypeImpl<TestContext>[] supertypes) {
            this.name = name;
            this.parameters = parameters;
            this.supertypes = supertypes;
        }

        public TemplateTypeImpl<TestContext> applyTrivially() {
            return declaringNoMembers().applyTrivially();
        }

        public AbstractTemplateTypeConstructor<TestContext> declaringNoMembers() {
            return declaring(ImmutableSet.of());
        }

        public AbstractTemplateTypeConstructor<TestContext> declaring(ImmutableSet<SourceMemberDescriptor<TestContext>> members) {
            // TODO FIXME context
            SourceMemberDescriptors<TestContext> descriptors = new SourceMemberDescriptors<>(AbstractConstraints.none(), members);

            return tieTheKnot(meta -> AbstractTemplateTypeConstructor.create(
                    typeDeclarationFactory.createTemplateTypeDeclaration(
                            () -> parameters,
                            () -> Util.source(AbstractTypeList.<TestContext, TypeMixin<TestContext, ?>>of(supertypes)),
                            () -> descriptors,
                            () -> StringNameTag.labeled(name))
            ).bind(context));
        }
    }

    public class IntersectionOrUnionTypeToBeNamed<D extends TypeDeclaration<TestContext>, C extends AbstractTypeConstructor<TestContext, ?, T>, T extends AbstractProperType<TestContext, T>> {
        private final Function<String, Function<TypeParameterListImpl<TestContext>, Function<ImmutableList<SourceType<TestContext>>, C>>> typeFactory;

        public IntersectionOrUnionTypeToBeNamed(
                Function<String, Function<TypeParameterListImpl<TestContext>, Function<ImmutableList<SourceType<TestContext>>, C>>> typeFactory
        ) {
            this.typeFactory = typeFactory;
        }

        public IntersectionOrUnionTypeToParametrized<D, C, T> called(String... fqn) {
            return new IntersectionOrUnionTypeToParametrized<>(typeFactory.apply(flattenFqn(fqn)));
        }
    }

    public class IntersectionOrUnionTypeToParametrized<D extends TypeDeclaration<TestContext>, C extends AbstractTypeConstructor<TestContext, ?, T>, T extends AbstractProperType<TestContext, T>> {
        private final Function<TypeParameterListImpl<TestContext>, Function<ImmutableList<SourceType<TestContext>>, C>> typeFactory;

        public IntersectionOrUnionTypeToParametrized(Function<TypeParameterListImpl<TestContext>, Function<ImmutableList<SourceType<TestContext>>, C>> typeFactory) {
            this.typeFactory = typeFactory;
        }

        public IntersectionOrUnionTypeToBeOrdered<D, C, T> parametrizedBy(TypeParameterListImpl<TestContext> params) {
            return new IntersectionOrUnionTypeToBeOrdered<>(typeFactory.apply(params));
        }

        public C of(ProperTypeMixin<TestContext, ?>... elements) {
            return parametrizedBy(TypeParameterListImpl.empty()).of(elements);
        }
    }

    private class IntersectionOrUnionTypeToBeOrdered<D extends TypeDeclaration<TestContext>, C extends AbstractTypeConstructor<TestContext, ?, T>, T extends AbstractProperType<TestContext, T>> {
        private final Function<ImmutableList<SourceType<TestContext>>, C> typeFactory;

        private IntersectionOrUnionTypeToBeOrdered(Function<ImmutableList<SourceType<TestContext>>, C> typeFactory) {
            this.typeFactory = typeFactory;
        }

        public C of(TypeMixin<TestContext, ?>... elements) {
            return typeFactory.apply(Util.source(AbstractTypeList.of(ImmutableList.copyOf(elements))));
        }
    }

}
