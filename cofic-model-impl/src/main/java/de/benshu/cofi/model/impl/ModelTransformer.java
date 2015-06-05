package de.benshu.cofi.model.impl;

import de.benshu.commons.core.Optional;

public interface ModelTransformer<X extends ModelContext<X>, N, L extends N, D extends L, S extends N, E extends N, T extends N> {
    default N transform(ModelNodeMixin<X> node) {
        return node.accept(this);
    }

    default Optional<N> transformNonNull(ModelNodeMixin<X> node) {
        return node == null ? Optional.none() : Optional.some(node.accept(this));
    }

    default L transform(TypeBody.Element<X> bodyElement) {
        return bodyElement.accept(this);
    }

    default Optional<L> transformNonNull(TypeBody.Element<X> bodyElement) {
        return bodyElement == null ? Optional.none() : Optional.some(bodyElement.accept(this));
    }

    default D transform(AbstractTypeDeclaration<X> typeDeclaration) {
        return typeDeclaration.accept(this);
    }

    default Optional<D> transformNonNull(AbstractTypeDeclaration<X> typeDeclaration) {
        return typeDeclaration == null ? Optional.none() : Optional.some(typeDeclaration.accept(this));
    }

    default S transform(Statement<X> statement) {
        return statement.accept(this);
    }

    default Optional<S> transformNonNull(Statement<X> statement) {
        return statement == null ? Optional.none() : Optional.some(statement.accept(this));
    }

    default E transform(ExpressionNode<X> expression) {
        return expression.accept(this);
    }

    default Optional<E> transformNonNull(ExpressionNode<X> expression) {
        return expression == null ? Optional.none() : Optional.some(expression.accept(this));
    }

    default T transform(TypeExpression<X> typeExpression) {
        return typeExpression.accept(this);
    }

    default Optional<T> transformNonNull(TypeExpression<X> typeExpression) {
        return typeExpression == null ? Optional.none() : Optional.some(typeExpression.accept(this));
    }

    default N transformAnnotation(AnnotationImpl<X> annotation) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformAnnotationPropertyAssignment(AnnotationImpl.PropertyAssignment<X> propertyAssignment) {
        throw new AssertionError(this.getClass().toString());
    }

    default D transformClassDeclaration(ClassDeclaration<X> classDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformClosure(Closure<X> closure) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformClosureCase(Closure.Case<X> closureCase) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformCompilationUnit(CompilationUnit<X> compilationUnit) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformCompilationUnitModuleDeclaration(CompilationUnit.ModuleDeclaration<X> moduleDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformCompilationUnitPackageDeclaration(CompilationUnit.PackageDeclaration<X> packageDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default S transformExpressionStatement(ExpressionStatement<X> expressionStatement) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformFullyQualifiedName(FullyQualifiedName<X> fullyQualifiedName) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformFunctionInvocationExpression(FunctionInvocationExpression<X> functionInvocationExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default T transformFunctionType(FunctionTypeExpression<X> functionType) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformImportStatement(ImportStatement<X> importStatement) {
        throw new AssertionError(this.getClass().toString());
    }

    default L transformInitializerStatement(InitializerStatement<X> initializer) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformLiteralExpression(LiteralExpression<X> literalExpr) {
        throw new AssertionError(this.getClass().toString());
    }

    default T transformLiteralType(LiteralTypeExpression<X> literalTypeExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default S transformLocalVariableDeclaration(LocalVariableDeclaration<X> localVariableDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformMemberAccessExpression(MemberAccessExpression<X> memberAccessExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default L transformMethodDeclaration(MethodDeclarationImpl<X> methodDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformMethodDeclarationPiece(MethodDeclarationImpl.Piece<X> piece) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformModifier(ModifierImpl<X> modifier) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformNameExpression(NameExpression<X> nameExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default T transformNamedType(NamedTypeExpression<X> namedType) {
        throw new AssertionError(this.getClass().toString());
    }

    default D transformObjectDeclaration(ObjectDeclaration<X> objectDecl) {
        throw new AssertionError(this.getClass().toString());
    }

    default D transformPackageObjectDeclaration(PackageObjectDeclaration<X> packageObjectDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformParameter(ParameterImpl<X> parameter) {
        throw new AssertionError(this.getClass().toString());
    }

    default L transformPropertyDeclaration(PropertyDeclaration<X> propertyDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformRootExpression(RootExpression<X> rootExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformRelativeName(RelativeNameImpl<X> relativeName) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformThisExpr(ThisExpression<X> thisExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default D transformTraitDeclaration(TraitDeclaration<X> traitDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default T transformTupleType(TupleTypeExpression<X> tupleType) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformTypeBody(TypeBody<X> typeBody) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformTypeParameterDeclaration(TypeParamDecl<X> typeParameterDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default N transformTypeParameters(TypeParameters<X> typeParameters) {
        throw new AssertionError(this.getClass().toString());
    }

    default D transformUnionDeclaration(UnionDeclaration<X> unionDeclaration) {
        throw new AssertionError(this.getClass().toString());
    }

    default E transformUserDefinedExpression(UserDefinedExpression<X> userDefinedExpression) {
        throw new AssertionError(this.getClass().toString());
    }

    default S transformUserDefinedStatement(UserDefinedStatement<X> userDefinedStatement) {
        throw new AssertionError(this.getClass().toString());
    }
}
