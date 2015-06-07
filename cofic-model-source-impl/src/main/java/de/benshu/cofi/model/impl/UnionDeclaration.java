package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.unions.AbstractUnionTypeConstructor;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.unions.UnboundUnionTypeConstructor;
import de.benshu.cofi.types.impl.declarations.SourceType;
import de.benshu.cofi.types.impl.declarations.UnionTypeDeclaration;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.streams.Collectors;
import de.benshu.jswizzle.copyable.CopyFactory;
import de.benshu.jswizzle.copyable.Copyable;

@Copyable
public class UnionDeclaration<X extends ModelContext<X>> extends AbstractTypeDeclaration<X> implements AnnotatedNodeMixin<X>, CopyableUnionDeclaration<X> {
    @AstNodeConstructorMethod
    @CopyFactory
    public static <X extends ModelContext<X>> UnionDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations,
                                                                     ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters,
                                                                     ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        return new UnionDeclaration<>(annotations, modifiers, id, typeParameters, extending, body);
    }

    public final Token id;
    public final TypeParameters<X> typeParameters;
    private final UnboundUnionTypeConstructor<X> unbound;

    UnionDeclaration(ImmutableList<AnnotationImpl<X>> annotations,
                     ImmutableList<ModifierImpl<X>> modifiers, Token id, TypeParameters<X> typeParameters,
                     ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);
        this.id = id;

        this.typeParameters = typeParameters;
        this.unbound = AbstractUnionTypeConstructor.<X>create(
                UnionTypeDeclaration.lazy(
                        x -> x.lookUpTypeParametersOf(this),
                        x -> getExtending().stream().map(e -> SourceType.of(x.lookUpTypeOf(e), e.getSourceSnippet())).collect(Collectors.list()),
                        x -> IndividualTags.empty()
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
        return visitor.visitUnionDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformUnionDeclaration(this);
    }

    @Override
    public TypeParameters<X> getTypeParameters() {
        return typeParameters;
    }

    @Override
    public ProperTypeConstructorMixin<X, ?, ?> bind(X context) {
        return unbound.bind(context);
    }
}
