package de.benshu.cofi.cofic.backend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.frontend.namespace.AbstractResolution;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.AnnotatedNodeMixin;
import de.benshu.cofi.model.impl.AnnotationImpl;
import de.benshu.cofi.model.impl.ClassDeclaration;
import de.benshu.cofi.model.impl.CompilationUnit;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.model.impl.FunctionInvocationExpression;
import de.benshu.cofi.model.impl.LiteralExpression;
import de.benshu.cofi.model.impl.MemberAccessExpression;
import de.benshu.cofi.model.impl.MethodDeclarationImpl;
import de.benshu.cofi.model.impl.ModelTransformer;
import de.benshu.cofi.model.impl.ObjectDeclaration;
import de.benshu.cofi.model.impl.PackageObjectDeclaration;
import de.benshu.cofi.model.impl.ParameterImpl;
import de.benshu.cofi.model.impl.ThisExpression;
import de.benshu.cofi.model.impl.TraitDeclaration;
import de.benshu.cofi.model.impl.UnionDeclaration;
import de.benshu.cofi.runtime.AbstractObject;
import de.benshu.cofi.runtime.AbstractTypeDeclaration;
import de.benshu.cofi.runtime.Annotation;
import de.benshu.cofi.runtime.Assignment;
import de.benshu.cofi.runtime.Class;
import de.benshu.cofi.runtime.Closure;
import de.benshu.cofi.runtime.Companion;
import de.benshu.cofi.runtime.Expression;
import de.benshu.cofi.runtime.ExpressionStatement;
import de.benshu.cofi.runtime.FunctionInvocation;
import de.benshu.cofi.runtime.LiteralValue;
import de.benshu.cofi.runtime.LocalVariableDeclaration;
import de.benshu.cofi.runtime.MemberAccess;
import de.benshu.cofi.runtime.MethodDeclaration;
import de.benshu.cofi.runtime.ModelNode;
import de.benshu.cofi.runtime.Module;
import de.benshu.cofi.runtime.NameExpression;
import de.benshu.cofi.runtime.ObjectSingleton;
import de.benshu.cofi.runtime.Package;
import de.benshu.cofi.runtime.Parameter;
import de.benshu.cofi.runtime.PropertyDeclaration;
import de.benshu.cofi.runtime.RootExpression;
import de.benshu.cofi.runtime.SingletonCompanion;
import de.benshu.cofi.runtime.Statement;
import de.benshu.cofi.runtime.Trait;
import de.benshu.cofi.runtime.TypeBody;
import de.benshu.cofi.runtime.TypeExpression;
import de.benshu.cofi.runtime.Union;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.TemplateTypeConstructor;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.commons.core.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;
import static de.benshu.commons.core.streams.Collectors.set;
import static de.benshu.commons.core.streams.Collectors.setMultimap;
import static java.util.Comparator.naturalOrder;

public class ToRuntimeModelTransformer implements ModelTransformer<
        Pass,
        Constructor<? extends ModelNode>,
        Constructor<? extends TypeBody.Containable>,
        Constructor<? extends AbstractTypeDeclaration>,
        Constructor<? extends Statement>,
        Constructor<? extends Expression>,
        Constructor<? extends TypeExpression>> {

    public static Module transformModule(Pass pass, ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return new ToRuntimeModelTransformer(pass).transformModule(compilationUnits);
    }

    private final Pass pass;

    private ToRuntimeModelTransformer(Pass pass) {
        this.pass = pass;
    }

    private Module transformModule(ImmutableSet<CompilationUnit<Pass>> compilationUnits) {
        return new Module(
                compilationUnits.iterator().next().moduleDeclaration.name.fqn,
                transformPackages(compilationUnits.stream()
                        .flatMap(u -> u.declarations.stream().map(d -> immutableEntry(u.packageDeclaration.name.fqn, d)))
                        .collect(setMultimap())),
                () -> { throw new AssertionError(); },
                x -> TypeParameterListImpl.empty().unbind());
    }

    private Constructor<Package> transformPackages(ImmutableSetMultimap<Fqn, de.benshu.cofi.model.impl.AbstractTypeDeclaration<Pass>> topLevelDeclarations) {
        Map<Fqn, Constructor<Package>> transformed = new HashMap<>();

        topLevelDeclarations.asMap().entrySet().stream()
                .sorted((a, b) -> -a.getKey().compareTo(b.getKey()))
                .collect(list()).forEach(e -> {
            final ImmutableSet<Constructor<Package>> subpackages = transformed.entrySet().stream()
                    .filter(te -> te.getKey().getParent().equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(set());

            transformed.put(e.getKey(), transformPackage(e.getKey(), ImmutableSet.copyOf(e.getValue()), subpackages));
        });

        return transformed.get(topLevelDeclarations.keySet().stream().min(naturalOrder()).get());
    }

    private Constructor<Package> transformPackage(Fqn fqn, ImmutableSet<de.benshu.cofi.model.impl.AbstractTypeDeclaration<Pass>> topLevelDeclarations, ImmutableSet<Constructor<Package>> subpackages) {
        final PackageObjectDeclaration<Pass> packageObjectDeclaration = topLevelDeclarations.stream()
                .filter(d -> d instanceof PackageObjectDeclaration<?>)
                .map(d -> (PackageObjectDeclaration<Pass>) d)
                .findAny().get();

        final ImmutableSet<Constructor<AbstractTypeDeclaration>> otherNonCompanionObjectDeclarations = topLevelDeclarations.stream()
                .filter(d -> !(d instanceof PackageObjectDeclaration<?>))
                .filter(d -> !pass.isCompanion(d))
                .map(this::transform)
                .map(c -> ((Constructor<AbstractTypeDeclaration>) c))
                .collect(set());

        final TemplateTypeConstructor type = packageObjectDeclaration.getType(pass).unbind();
        return as -> new Package(
                as,
                transformAnnotationsOf(packageObjectDeclaration),
                fqn.getLocalName(),
                x -> type.getParameters(),
                d -> type,
                transformTypeBody(packageObjectDeclaration.body),
                otherNonCompanionObjectDeclarations,
                subpackages);
    }

    @Override
    public Constructor<Annotation> transformAnnotation(AnnotationImpl<Pass> annotation) {
        return as -> new Annotation(
                as,
                x -> pass.lookUpProperTypeOf(annotation.getTypeExpression()).unbind(),
                Optional.from(annotation.value).map(this::transform).map(this::covariant),
                annotation.propertyAssignments.stream()
                        .map(this::transformAnnotationPropertyAssignment)
                        .map(this::covariant)
                        .collect(set())
        );
    }

    @Override
    public Constructor<Statement> transformAssignment(de.benshu.cofi.model.impl.Assignment<Pass> assignment) {
        return as -> new Assignment(
                as,
                covariant(transform(assignment.lhs)),
                covariant(transform(assignment.rhs))
        );
    }

    @Override
    public Constructor<Annotation.PropertyAssignment> transformAnnotationPropertyAssignment(AnnotationImpl.PropertyAssignment<Pass> propertyAssignment) {
        throw null;
    }

    @Override
    public Constructor<Class> transformClassDeclaration(ClassDeclaration<Pass> classDeclaration) {
        return as -> new Class(
                as,
                transformAnnotationsOf(classDeclaration),
                classDeclaration.id.getLexeme(),
                x -> pass.lookUpTypeParametersOf(classDeclaration).unbind(),
                d -> pass.lookUpTypeOf(classDeclaration).unbind(),
                classDeclaration.getParameters().stream().map(this::transformParameter).map(this::covariant).collect(list()),
                transformTypeBody(classDeclaration.body),
                (Constructor<Companion>) transformObjectDeclaration(pass.lookUpCompanionObjectOf(classDeclaration))
        );
    }

    @Override
    public Constructor<Expression> transformClosure(de.benshu.cofi.model.impl.Closure<Pass> closure) {
        return as -> new Closure(
                as,
                closure.cases.stream().map(this::transformClosureCase).map(this::covariant).collect(list()),
                x -> pass.lookUpTypeOf(closure).unbind());
    }

    @Override
    public Constructor<Closure.Case> transformClosureCase(de.benshu.cofi.model.impl.Closure.Case<Pass> closureCase) {
        return as -> new Closure.Case(
                as,
                closureCase.params.stream().map(this::transformParameter).map(this::covariant).collect(list()),
                closureCase.body.stream().map(this::transform).map(this::covariant).collect(list())
        );
    }

    @Override
    public Constructor<Statement> transformExpressionStatement(de.benshu.cofi.model.impl.ExpressionStatement<Pass> expressionStatement) {
        return as -> new ExpressionStatement(
                as,
                expressionStatement.annotations.stream().map(this::transformAnnotation).map(this::covariant).collect(set()),
                covariant(transform(expressionStatement.expression))
        );
    }

    @Override
    public Constructor<Expression> transformFunctionInvocationExpression(FunctionInvocationExpression<Pass> functionInvocationExpression) {
        return as -> new FunctionInvocation(
                as,
                covariant(transform(functionInvocationExpression.primary)),
                functionInvocationExpression.args.stream().map(this::transform).map(this::covariant).collect(list()),
                x -> pass.lookUpTypeOf(functionInvocationExpression).unbind());
    }

    @Override
    public Constructor<LiteralValue> transformLiteralExpression(LiteralExpression<Pass> literalExpr) {
        return as -> new LiteralValue(
                as,
                literalExpr.literal.getLexeme(),
                x -> pass.lookUpTypeOf(literalExpr).unbind()
        );
    }

    @Override
    public Constructor<Statement> transformLocalVariableDeclaration(de.benshu.cofi.model.impl.LocalVariableDeclaration<Pass> localVariableDeclaration) {
        return as -> new LocalVariableDeclaration(
                as,
                transformAnnotationsOf(localVariableDeclaration),
                localVariableDeclaration.getName(),
                x -> pass.lookUpProperTypeOf(localVariableDeclaration.type).unbind(),
                transformNonNull(localVariableDeclaration.value).map(this::covariant)
        );
    }

    @Override
    public Constructor<Expression> transformMemberAccessExpression(MemberAccessExpression<Pass> memberAccessExpression) {
        return as -> new MemberAccess(
                as,
                covariant(transform(memberAccessExpression.primary)),
                Iterables.getOnlyElement(memberAccessExpression.name.ids).getLexeme(),
                pass.lookUpTypeArgumentsTo(memberAccessExpression.name).stream().map(t -> (TypeReference<Type>) (x -> t.unbind())).collect(list()),
                x -> pass.lookUpTypeOf(memberAccessExpression).unbind());
    }

    @Override
    public Constructor<MethodDeclaration> transformMethodDeclaration(MethodDeclarationImpl<Pass> methodDeclaration) {
        ProperTypeMixin<Pass, ?> properType = pass.lookUpProperTypeOf(methodDeclaration.returnType);
        for (MethodDeclarationImpl.Piece<Pass> piece : methodDeclaration.pieces)
            properType = pass.getTypeSystem().constructFunction(piece.params.stream().map(p -> pass.lookUpProperTypeOf(p.type)).collect(typeList()), properType);

        AdHoc.TemplateTypeConstructor<Pass> type = AdHoc.templateTypeConstructor(
                pass, pass.lookUpTypeParametersOf(methodDeclaration.pieces.get(0)), (TemplateTypeImpl<Pass>) properType);

        return as -> new MethodDeclaration(
                as,
                transformAnnotationsOf(methodDeclaration),
                methodDeclaration.getName(),
                methodDeclaration.pieces.stream().map(this::transformMethodDeclarationPiece).map(this::covariant).collect(list()),
                x -> type.unbind().getParameters(),
                x -> type.unbind().applyTrivially(),
                x -> type.unbind(),
                Optional.from(methodDeclaration.body).map(b -> b.stream()
                        .map(this::transform)
                        .map(this::covariant)
                        .collect(list())));
    }

    @Override
    public Constructor<MethodDeclaration.Piece> transformMethodDeclarationPiece(MethodDeclarationImpl.Piece<Pass> piece) {
        return as -> new MethodDeclaration.Piece(
                as,
                piece.name.getLexeme(),
                piece.params.stream().map(this::transformParameter).map(this::covariant).collect(Collector.of(
                        ImmutableList::<Constructor<Parameter>>builder,
                        ImmutableList.Builder::add,
                        (left, right) -> left.addAll(right.build()),
                        ImmutableList.Builder::build))
        );
    }

    @Override
    public Constructor<Expression> transformNameExpression(de.benshu.cofi.model.impl.NameExpression<Pass> nameExpression) {
        final AbstractResolution resolution = pass.lookUpResolutionOf(nameExpression);
        final Optional<ExpressionNode<Pass>> implicitPrimary = resolution.isMember() ? Optional.some(resolution.getImplicitPrimary()) : Optional.none();

        return implicitPrimary
                .<Constructor<Expression>>map(p -> as -> new MemberAccess(
                        as,
                        covariant(transform(p)),
                        Iterables.getOnlyElement(nameExpression.name.ids).getLexeme(),
                        pass.lookUpTypeArgumentsTo(nameExpression.name).stream().map(t -> (TypeReference<Type>) x -> t.unbind()).collect(Collector.of(
                                ImmutableList::<TypeReference<?>>builder,
                                ImmutableList.Builder::add,
                                (left, right) -> left.addAll(right.build()),
                                ImmutableList.Builder::build)),
                        x -> pass.lookUpTypeOf(nameExpression).unbind()))
                .getOrSupply(() -> as -> new NameExpression(
                        as,
                        Iterables.getOnlyElement(nameExpression.name.ids).getLexeme(),
                        x -> pass.lookUpTypeOf(nameExpression).unbind()));
    }

    @Override
    public Constructor<? extends AbstractObject> transformObjectDeclaration(ObjectDeclaration<Pass> objectDeclaration) {
        // TODO clean this mess up
        if (pass.isCompanion(objectDeclaration)) {
            if (pass.lookUpTypeParametersOf(objectDeclaration).isEmpty())
                return as -> new SingletonCompanion(
                        as,
                        transformAnnotationsOf(objectDeclaration),
                        objectDeclaration.id.getLexeme(),
                        x -> pass.lookUpTypeParametersOf(objectDeclaration).unbind(),
                        d -> pass.lookUpTypeOf(objectDeclaration).unbind(),
                        transformTypeBody(objectDeclaration.body)
                );
            else
                return as -> new Companion.MultitonCompanion(
                        as,
                        transformAnnotationsOf(objectDeclaration),
                        objectDeclaration.id.getLexeme(),
                        x -> pass.lookUpTypeParametersOf(objectDeclaration).unbind(),
                        d -> pass.lookUpTypeOf(objectDeclaration).unbind(),
                        transformTypeBody(objectDeclaration.body)
                );
        } else
            return as -> new ObjectSingleton(
                    as,
                    transformAnnotationsOf(objectDeclaration),
                    objectDeclaration.id.getLexeme(),
                    x -> pass.lookUpTypeParametersOf(objectDeclaration).unbind(),
                    d -> pass.lookUpTypeOf(objectDeclaration).unbind(),
                    transformTypeBody(objectDeclaration.body)
            );
    }

    @Override
    public Constructor<Parameter> transformParameter(ParameterImpl<Pass> parameter) {
        return as -> new Parameter(
                as,
                transformAnnotationsOf(parameter),
                parameter.name.getLexeme(),
                x -> pass.lookUpProperTypeOf(parameter.type).unbind(),
                parameter.varargs != null,
                transformNonNull(parameter.defaultValue).map(this::covariant)
        );
    }

    @Override
    public Constructor<PropertyDeclaration> transformPropertyDeclaration(de.benshu.cofi.model.impl.PropertyDeclaration<Pass> propertyDeclaration) {
        final de.benshu.cofi.model.impl.AbstractTypeDeclaration<Pass> container = pass.lookUpContainerOf(propertyDeclaration);
        final AbstractMember<Pass> property = pass.lookUpTypeOf(container).applyTrivially().lookupMember(propertyDeclaration.getName()).get();

        return as -> new PropertyDeclaration(
                as,
                transformAnnotationsOf(propertyDeclaration),
                propertyDeclaration.getName(),
                x -> TypeParameterListImpl.empty().unbind(), propertyDeclaration.traits.stream()
                .map(t -> (TypeReference<TemplateTypeConstructor>) x -> (TemplateTypeConstructor) pass.lookUpTypeOf(t).unbind())
                .collect(set()), x -> property.getType().unbind(),
                x -> pass.lookUpProperTypeOf(propertyDeclaration.type).unbind(),
                transformNonNull(propertyDeclaration.initialValue).map(this::covariant)
        );
    }

    @Override
    public Constructor<RootExpression> transformRootExpression(de.benshu.cofi.model.impl.RootExpression<Pass> rootExpression) {
        return as -> new RootExpression(as, x -> pass.lookUpTypeOf(rootExpression).unbind());
    }

    @Override
    public Constructor<Expression> transformThisExpr(ThisExpression<Pass> thisExpression) {
        return as -> new de.benshu.cofi.runtime.ThisExpression(as, x -> pass.lookUpTypeOf(thisExpression).unbind());
    }

    @Override
    public Constructor<Trait> transformTraitDeclaration(TraitDeclaration<Pass> traitDeclaration) {
        return as -> new Trait(
                as,
                transformAnnotationsOf(traitDeclaration),
                traitDeclaration.id.getLexeme(),
                x -> pass.lookUpTypeParametersOf(traitDeclaration).unbind(),
                d -> pass.lookUpTypeOf(traitDeclaration).unbind(),
                transformTypeBody(traitDeclaration.body),
                (Constructor<Companion>) transformObjectDeclaration(pass.lookUpCompanionObjectOf(traitDeclaration))
        );
    }

    @Override
    public Constructor<TypeBody> transformTypeBody(de.benshu.cofi.model.impl.TypeBody<Pass> typeBody) {
        return as -> new TypeBody(as, typeBody.elements.stream()
                .map(this::transform)
                .map(e -> ((Constructor<TypeBody.Containable>) e))
                .collect(Collector.of(
                        ImmutableList::<Constructor<TypeBody.Containable>>builder,
                        ImmutableList.Builder::add,
                        (left, right) -> left.addAll(right.build()),
                        ImmutableList.Builder::build)));
    }

    @Override
    public Constructor<AbstractTypeDeclaration> transformUnionDeclaration(UnionDeclaration<Pass> unionDeclaration) {
        return as -> new Union(
                as,
                transformAnnotationsOf(unionDeclaration),
                unionDeclaration.id.getLexeme(),
                x -> pass.lookUpTypeParametersOf(unionDeclaration).unbind(),
                d -> pass.lookUpTypeOf(unionDeclaration).unbind(),
                transformTypeBody(unionDeclaration.body),
                (Constructor<Companion>) transformObjectDeclaration(pass.lookUpCompanionObjectOf(unionDeclaration))
        );
    }

    private ImmutableSet<Constructor<Annotation>> transformAnnotationsOf(AnnotatedNodeMixin<Pass> annotatedNode) {
        return annotatedNode.getAnnotationsAndModifiers().stream()
                .map(this::transformAnnotation)
                .collect(set());
    }

    private <T> Constructor<T> covariant(Constructor<? extends T> constructor) {
        return constructor::construct;
    }
}
