package de.benshu.cofi.model.impl;

public interface ModelVisitor<X extends ModelContext<X>, T> {
    default T defaultAction(T aggregate) {
        throw new AssertionError();
    }

    default T visit(ModelNodeMixin<X> modelNode, T aggregate) {
        return modelNode.accept(this, aggregate);
    }

    default T visitNonNull(ModelNodeMixin<X> modelNode, T aggregate) {
        return modelNode == null ? aggregate : modelNode.accept(this, aggregate);
    }

    default T visitAll(Iterable<? extends ModelNodeMixin<X>> modelNodes, T aggregate) {
        for (ModelNodeMixin<X> n : modelNodes)
            aggregate = n.accept(this, aggregate);
        return aggregate;
    }

    default T visitAllNonNull(Iterable<? extends ModelNodeMixin<X>> modelNodes, T aggregate) {
        return modelNodes == null ? aggregate : visitAll(modelNodes, aggregate);
    }

    default T visitAbstractionStatement(AbstractionStatement<X> abstractionStatement, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitAbstractionStatementPiece(AbstractionStatement.Piece<X> abstractionStatementPiece, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitAnnotation(AnnotationImpl<X> annotation, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitAnnotationPropertyAssignment(AnnotationImpl.PropertyAssignment<X> propertyAssignment, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitAssignment(Assignment<X> assignment, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitClassDeclaration(ClassDeclaration<X> classDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitClosure(Closure<X> closure, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitClosureCase(Closure.Case<X> closureCase, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitCompilationUnit(CompilationUnit<X> compilationUnit, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitCompilationUnitModuleDeclaration(CompilationUnit.ModuleDeclaration<X> moduleDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitCompilationUnitPackageDeclaration(CompilationUnit.PackageDeclaration<X> packageDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitExpressionStatement(ExpressionStatement<X> expressionStatement, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitFullyQualifiedName(FullyQualifiedName<X> fullyQualifiedName, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitFunctionInvocationExpression(FunctionInvocationExpression<X> functionInvocationExpression, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitFunctionInvocationStatement(FunctionInvocationStatement<X> functionInvocationStatement, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitFunctionType(FunctionTypeExpression<X> functionType, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitImportStatement(ImportStatement<X> importStatement, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitInitializerStatement(InitializerStatement<X> initializer, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitLiteralExpression(LiteralExpression<X> literalExpr, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitLiteralType(LiteralTypeExpression<X> literalTypeExpression, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitLocalVariableDeclaration(LocalVariableDeclaration<X> localVariableDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitMemberAccessExpression(MemberAccessExpression<X> memberAccessExpression, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitMethodDeclaration(MethodDeclarationImpl<X> methodDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitMethodDeclarationPiece(MethodDeclarationImpl.Piece<X> piece, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitModifier(ModifierImpl<X> modifier, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitNameExpression(NameExpression<X> nameExpression, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitNamedType(NamedTypeExpression<X> namedType, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitObjectDeclaration(ObjectDeclaration<X> objectDecl, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitPackageObjectDeclaration(PackageObjectDeclaration<X> packageObjectDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitParameter(ParameterImpl<X> parameter, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitPropertyDeclaration(PropertyDeclaration<X> propertyDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitRelativeName(RelativeNameImpl<X> relativeName, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitRootExpression(RootExpression<X> rootExpression, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitThisExpr(ThisExpr<X> thisExpr, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitTraitDeclaration(TraitDeclaration<X> traitDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitTupleType(TupleTypeExpression<X> tupleType, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitTypeBody(TypeBody<X> typeBody, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitTypeParameterDeclaration(TypeParamDecl<X> typeParameterDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitTypeParameters(TypeParameters<X> typeParameters, T aggregate) {
        return defaultAction(aggregate);
    }

    default T visitUnionDeclaration(UnionDeclaration<X> unionDeclaration, T aggregate) {
        return defaultAction(aggregate);
    }
}
