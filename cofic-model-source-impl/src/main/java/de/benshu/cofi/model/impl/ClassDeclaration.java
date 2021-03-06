package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.UnboundTemplateTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.streams.Collectors;
import de.benshu.jswizzle.copyable.CopyFactory;
import de.benshu.jswizzle.copyable.Copyable;

@Copyable
public class ClassDeclaration<X extends ModelContext<X>> extends AbstractTypeDeclaration<X> implements AnnotatedNodeMixin<X>, CopyableClassDeclaration<X> {
    @AstNodeConstructorMethod
    @CopyFactory
    public static <X extends ModelContext<X>> ClassDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters, ImmutableList<ParameterImpl<X>> parameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        return new ClassDeclaration<>(annotations, modifiers, id, typeParameters, parameters, extending, body);
    }

    public final Token id;
    public final TypeParameters<X> typeParameters;
    @Copyable.Include
    public final ImmutableList<ParameterImpl<X>> parameters;
    private final UnboundTemplateTypeConstructor<X> unbound;

    public ClassDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters, ImmutableList<ParameterImpl<X>> parameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);

        this.id = id;
        this.typeParameters = typeParameters;
        this.parameters = parameters;

        this.unbound = AbstractTemplateTypeConstructor.<X>create(
                TemplateTypeDeclaration.memoizing(
                        (x, b) -> x.lookUpTypeParametersOf(this),
                        (x, b) -> getExtending().stream().map(e -> SourceType.of(x.lookUpTypeOf(e), e.getSourceSnippet())).collect(Collectors.list()),
                        (x, b) -> x.lookUpMemberDescriptorsOf(this),
                        (x, b) -> IndividualTags.empty()
                                .set(TypeTags.NAME, FullyQualifiedTypeName.create(() -> x.lookUpFqnOf(this)))
                                .set(AbstractTypeDeclaration.Tag.INSTANCE, this)
                )
        );
    }

    @Override
    public Token getId() {
        return id;
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitClassDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformClassDeclaration(this);
    }

    @Override
    public TypeParameters<X> getTypeParameters() {
        return typeParameters;
    }

    public ImmutableList<ParameterImpl<X>> getParameters() {
        return parameters;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return unbound.bind(context);
    }
}
