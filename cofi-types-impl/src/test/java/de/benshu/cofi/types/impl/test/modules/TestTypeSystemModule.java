package de.benshu.cofi.types.impl.test.modules;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.templates.UnboundTemplateTypeConstructor;
import de.benshu.cofi.types.impl.Bottom;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemImpl;
import de.benshu.cofi.types.impl.declarations.SourceMemberDescriptors;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.test.StringNameTag;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;
import de.benshu.cofi.types.impl.test.TypeDeclarationFactory;
import de.benshu.cofi.types.impl.test.Util;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;
import static de.benshu.cofi.types.impl.test.Util.flattenFqn;
import static de.benshu.cofi.types.impl.test.Util.types;

public class TestTypeSystemModule implements Module {
    private final Function<String, TypeMixin<TestContext, ?>> lookerUpper = from -> {
        TestTypeSystemModule module = this;

        if ("Type".equals(from)) {
            return module.rawMetaType;
        } else if ("Unit".equals(from)) {
            return module.unit;
        } else if ("Function".equals(from)) {
            return module.function;
        } else if (from.startsWith("Tuple")) {
            final String size = from.substring(5);

            if (size.isEmpty()) {
                return module.tuple;
            }

            return module.tuples.get(Integer.parseInt(size) - 2);
        } else if (from.startsWith("Function")) {
            String params = from.substring(8);
            return module.functions.get(Integer.parseInt(params));
        }

        throw new UnsupportedOperationException("Type name: " + from);
    };

    final UnboundTemplateTypeConstructor<TestContext> unboundTopConstructor = AbstractTemplateTypeConstructor.<TestContext>create(TemplateTypeDeclaration.memoizing(
            x -> noTypeParams(),
            x -> ImmutableList.of(),
            x -> SourceMemberDescriptors.empty(),
            x -> StringNameTag.labeled(flattenFqn("cofi", "lang", "Object"))));


    final TypeSystemImpl<TestContext> typeSystem = TypeSystemImpl.create(lookerUpper, StringNameTag.INSTANCE, this::getTopConstructor);

    final TestContext context = new TestContext(typeSystem);

    private final AbstractTemplateTypeConstructor<TestContext> topConstructor = unboundTopConstructor.bind(context);

    private final TypeDeclarationFactory typeDeclarationFactory = new TypeDeclarationFactory();

    final TemplateTypeImpl<TestContext> top = typeSystem.getTop();

    final Bottom<TestContext> bot = typeSystem.getBottom();

    final AbstractTemplateTypeConstructor<TestContext> rawMetaType = createTemplateType(
            flattenFqn("cofi", "lang", "Type"),
            createTypeParams(Variance.INVARIANT, "T"),
            types(top)
    );

    final AbstractTemplateTypeConstructor<TestContext> tuple = createTemplateType(
            flattenFqn("cofi", "lang", "Tuple"),
            createTypeParams(Variance.COVARIANT, "B"),
            types(top)
    );

    final AbstractTemplateTypeConstructor<TestContext> unit = createTemplateType(
            flattenFqn("cofi", "lang", "Unit"),
            noTypeParams(),
            types(tuple.apply(types(bot)))
    );

    final AbstractTemplateTypeConstructor<TestContext> function = createTemplateType(
            flattenFqn("cofi", "lang", "Function"),
            createTypeParams(Variance.CONTRAVARIANT, "P", Variance.COVARIANT, "R"),
            types(top)
    );

    final ImmutableList<AbstractTemplateTypeConstructor<TestContext>> tuples;
    final ImmutableList<AbstractTemplateTypeConstructor<TestContext>> functions;

    {
        final ImmutableList.Builder<AbstractTemplateTypeConstructor<TestContext>> tb = ImmutableList.builder();
        for (int i = 2; i < 22; ++i) {
            final TypeParameterListImpl<TestContext> params = createTupleParams(i);
            // TODO supertype is T1|T2|T3|...
            final AbstractTemplateTypeConstructor<TestContext> t = createTemplateType(flattenFqn("cofi", "lang", "Tuple" + i), params, types(top));
            tb.add(t);
        }
        tuples = tb.build();

        final ImmutableList.Builder<AbstractTemplateTypeConstructor<TestContext>> ftb = ImmutableList.builder();
        for (int i = 0; i < 22; ++i) {
            TypeParameterListImpl<TestContext> params = createTupleParams(i + 1);
            TypeMixin<TestContext, ?> paramTypes = typeSystem.constructTuple(params.getVariables().subList(0, i));
            TemplateTypeImpl<TestContext> supertype = typeSystem.getFunction().apply(types(paramTypes, params.getVariables().get(i)));

            AbstractTemplateTypeConstructor<TestContext> ft = createTemplateType(flattenFqn("cofi", "lang", "Function" + i), params, types(supertype));
            ftb.add(ft);
        }
        functions = ftb.build();
    }

    AbstractTemplateTypeConstructor<TestContext> createTemplateType(String name, TypeParameterListImpl<TestContext> params, final AbstractTypeList<TestContext, ?> supertypes) {
        return AbstractTemplateTypeConstructor.create(typeDeclarationFactory.createTemplateTypeDeclaration(
                () -> params,
                () -> Util.source(supertypes),
                () -> StringNameTag.labeled(name))).bind(context);
    }

    private AbstractTemplateTypeConstructor<TestContext> getTopConstructor() {
        return topConstructor;
    }

    private TypeParameterListImpl<TestContext> createTupleParams(int size) {
        final ImmutableList.Builder<Pair<Variance, String>> builder = ImmutableList.builder();

        for (int i = 0; i < size; ++i) {
            builder.add(Pair.of(Variance.COVARIANT, getTupleParamName(i, size)));
        }

        return createTypeParams(builder.build());
    }

    private String getTupleParamName(int i, int size) {
        return size > 26 ? "T" + (i + 1) : Integer.toString(10 + i, Character.MAX_RADIX);
    }

    private TypeParameterListImpl<TestContext> noTypeParams() {
        return createTypeParams(ImmutableList.of());
    }

    private TypeParameterListImpl<TestContext> createTypeParams(Variance v1, String n1) {
        return createTypeParams(ImmutableList.of(
                Pair.of(v1, n1)
        ));
    }

    private TypeParameterListImpl<TestContext> createTypeParams(Variance v1, String n1, Variance v2, String n2) {
        return createTypeParams(ImmutableList.of(
                Pair.of(v1, n1),
                Pair.of(v2, n2)
        ));
    }

    private TypeParameterListImpl<TestContext> createTypeParams(ImmutableList<Pair<Variance, String>> params) {
        final ImmutableList.Builder<Pair<Variance, IndividualTags>> builder = ImmutableList.builder();

        for (Pair<Variance, String> p : params) {
            builder.add(Pair.of(p.a, StringNameTag.labeled(p.b)));
        }

        return TypeParameterListImpl.createTrivial(builder.build(), context);
    }

    @Override
    public void configure(Binder binder) {
        TypeBuilder typeBuilder = new TypeBuilder(context, typeSystem, typeDeclarationFactory);

        binder.bind(new TypeLiteral<TypeSystemImpl<TestContext>>() {}).toInstance(typeSystem);
        binder.bind(TypeDeclarationFactory.class).toInstance(typeDeclarationFactory);
        binder.bind(TypeBuilder.class).toInstance(typeBuilder);
        binder.bind(NumberTypes.class).toInstance(new NumberTypes(typeBuilder));
        binder.bind(CollectionTypes.class).toInstance(new CollectionTypes(typeBuilder));
        binder.bind(DayEnumTypes.class).toInstance(new DayEnumTypes(typeBuilder));
    }
}
