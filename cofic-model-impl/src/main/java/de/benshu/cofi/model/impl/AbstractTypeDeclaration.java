package de.benshu.cofi.model.impl;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.model.Named;
import de.benshu.cofi.model.TypeDeclaration;
import de.benshu.cofi.parser.lexer.Token;
import de.benshu.cofi.types.MemberSort;
import de.benshu.cofi.types.impl.ProperTypeConstructorMixin;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.jswizzle.copyable.Copyable;

import java.util.stream.Stream;

@Copyable
public abstract class AbstractTypeDeclaration<X extends ModelContext<X>>
        extends MemberDeclarationImpl<X>
        implements TypeParameterized<X>,
                   Named<X>,
                   TypeDeclaration<X>,
                   CopyableTypeDeclaration<X>,
                   TypeDeclarationContainer<X> {

    public final ImmutableList<TypeExpression<X>> extending;
    @Copyable.Include
    public final TypeBody<X> body;

    AbstractTypeDeclaration(ImmutableList<AnnotationImpl<X>> annotations, ImmutableList<ModifierImpl<X>> modifiers, ImmutableList<TypeExpression<X>> extending, TypeBody<X> body) {
        super(annotations, modifiers);

        this.extending = extending;
        this.body = body;
    }

    @Override
    public abstract <N, L extends N, D extends L, S extends N, E extends N, T extends E> D accept(ModelTransformer<X, N, L, D, S, E, T> transformer);

    @Copyable.Include
    public abstract Token getId();

    @Override
    public String getName() {
        return getId().getLexeme();
    }

    @Copyable.Include
    public final ImmutableList<TypeExpression<X>> getExtending() {
        return extending;
    }

    @Override
    public MemberSort getSort() {
        return MemberSort.TYPE;
    }

    @Copyable.Include
    public abstract TypeParameters<X> getTypeParameters();

    public ImmutableList<ParameterImpl<X>> getParameters() {
        return ImmutableList.of();
    }

    public boolean isTrait() {
        return false;
    }

    @Override
    final boolean isMember() {
        return true;
    }

    @Override
    public Stream<AbstractTypeDeclaration<X>> getTypeDeclarations() {
        return body.elements.stream()
                .filter(e -> e instanceof AbstractTypeDeclaration<?>)
                .map(e -> (AbstractTypeDeclaration<X>) e);
    }

    public abstract ProperTypeConstructorMixin<X, ?, ?> bind(X context);

    public enum Tag implements IndividualTag<AbstractTypeDeclaration> {
        INSTANCE;

        @Override
        public String debug() {
            return "TypeDeclaration";
        }
    }
}
