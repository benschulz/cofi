package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.model.common.FullyQualifiedTypeName;
import de.benshu.cofi.cofic.model.common.TypeTags;
import de.benshu.cofi.parser.AstNodeConstructorMethod;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.impl.declarations.TemplateTypeDeclaration;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.AbstractTemplateTypeConstructor;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.templates.UnboundTemplateTypeConstructor;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.jswizzle.copyable.CopyFactory;
import de.benshu.jswizzle.copyable.Copyable;

import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.list;

@Copyable
public final class ObjectDeclaration<X extends ModelContext<X>> extends AbstractObjectDeclaration<X> implements AnnotatedNodeMixin<X>, CopyableObjectDeclaration<X> {
    @AstNodeConstructorMethod
    public static <X extends ModelContext<X>> ObjectDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        return new ObjectDeclaration<>(annotations, modifiers, id, extending, body);
    }

    @CopyFactory
    public static <X extends ModelContext<X>> ObjectDeclaration<X> of(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, @SuppressWarnings("unused") TypeParameters<X> typeParameters, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        return new ObjectDeclaration<>(annotations, modifiers, id, extending, body);
    }

    public final Token id;
    private final UnboundTemplateTypeConstructor<X> unbound;
    private final TypeParameters<X> typeParameters = TypeParameters.none();

    ObjectDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, Token id, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers, extending, body);

        this.id = id;
        this.unbound = AbstractTemplateTypeConstructor.<X>create(
                TemplateTypeDeclaration.memoizing(
                        (x, b) -> x.lookUpTypeParametersOf(x.tryLookUpCompanionOf(this)
                                .getOrReturn(this)),
                        (x, b) -> {
                            final ImmutableList<SourceType<X>> sourceTypes = getExtending().stream()
                                    .map(e -> SourceType.of(x.lookUpTypeOf(e), e.getSourceSnippet()))
                                    .collect(list());

                            return x.tryLookUpAccompaniedBy(this)
                                    .map(c -> {
                                                final TemplateTypeImpl<X> supertype = x.getTypeSystem().getMetaType()
                                                        .apply(AbstractTypeList.of(x.lookUpTypeOf(c).applyTrivially()));

                                                return Stream.concat(Stream.of(SourceType.of(supertype, null)), sourceTypes.stream()).collect(list());
                                            }
                                    ).getOrReturn(sourceTypes);
                        },
                        (x, b) -> x.lookUpMemberDescriptorsOf(this),
                        (x, b) -> IndividualTags.empty()
                                .set(TypeTags.NAME, FullyQualifiedTypeName.create(() -> x.lookUpFqnOf(this), x.isCompanion(this) ? "\u262F" : ""))
                                .set(Tag.INSTANCE, this)
                )
        );
    }

    @Override
    public <T> T accept(ModelVisitor<X, T> visitor, T aggregate) {
        return visitor.visitObjectDeclaration(this, aggregate);
    }

    @Override
    public <N, L extends N, D extends L, S extends N, E extends N, T extends N> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer) {
        return transformer.transformObjectDeclaration(this);
    }

    @Override
    public Token getId() {
        return id;
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
