package de.benshu.cofi.runtime.serialization;

import com.google.common.collect.ImmutableList;

import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.runtime.NamedEntity;
import de.benshu.cofi.runtime.NamedEntityVisitor;
import de.benshu.cofi.runtime.context.FqnResolver;
import de.benshu.cofi.runtime.context.RuntimeContext;
import de.benshu.cofi.runtime.context.RuntimeTypeName;
import de.benshu.cofi.runtime.internal.TypeReferenceContext;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeParameterList;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Disjunction;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;
import static de.benshu.commons.core.streams.Collectors.list;

public class TypeParser {
    private final RuntimeContext context;
    private final FqnResolver fqnResolver;

    public TypeParser(RuntimeContext context, FqnResolver fqnResolver) {
        this.context = context;
        this.fqnResolver = fqnResolver;
    }

    public ContextPrepared in(TypeReferenceContext typeReferenceContext) {
        return new ContextPrepared(typeReferenceContext);
    }

    private ImmutableList<String> tokenizeTypeString(String typeString) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        int tokenStart = 0;
        for (int i = 0; i < typeString.length(); ++i) {
            switch (typeString.charAt(i)) {
                case '.':
                case '(':
                case '|':
                case '&':
                case ')':
                case '\u3008':
                case '+':
                case '-':
                case ',':
                case '\u3009':
                    if (tokenStart < i)
                        builder.add(typeString.substring(tokenStart, i));
                    tokenStart = i + 1;
                    builder.add(typeString.substring(i, tokenStart));
            }
        }
        if (tokenStart < typeString.length())
            builder.add(typeString.substring(tokenStart));

        return builder.build();
    }

    public class ContextPrepared {
        private final TypeReferenceContext typeReferenceContext;

        public ContextPrepared(TypeReferenceContext typeReferenceContext) {
            this.typeReferenceContext = typeReferenceContext;
        }

        public Type parseType(String typeString) {
            return parseBoundType(typeString).unbind();
        }

        public TypeParameterList parseTypeParameters(String typeParametersString) {
            return parseBoundTypeParameters(typeParametersString).unbind();
        }

        TemplateTypeConstructorMixin<RuntimeContext> parseBoundTemplateTypeConstructor(TypeParameterListImpl<RuntimeContext> typeParameters, String typeString) {
            return AdHoc.templateTypeConstructor(context, typeParameters, parseBoundTemplateType(typeString));
        }

        TemplateTypeImpl<RuntimeContext> parseBoundTemplateType(String typeString) {
            return (TemplateTypeImpl<RuntimeContext>) parseBoundType(typeString);
        }

        TypeMixin<RuntimeContext, ?> parseBoundType(String typeString) {
            return new TypeParse(typeReferenceContext, tokenizeTypeString(typeString)).perform();
        }

        TypeParameterListImpl<RuntimeContext> parseBoundTypeParameters(String typeParametersString) {
            return new TypeParameterListParse(typeReferenceContext, tokenizeTypeString(typeParametersString)).perform();
        }
    }

    private abstract class Parse<T> {
        final TypeReferenceContext typeReferenceContext;

        private final ImmutableList<String> tokens;
        private int index = 0;

        public Parse(TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            this.typeReferenceContext = typeReferenceContext;
            this.tokens = tokens;
        }

        public T perform() {
            checkState(index == 0);

            return performInternal();
        }

        protected abstract T performInternal();

        protected final Type lookUpNamedType() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            while (test(".")) {
                builder.add(tokens.get(index + 1));
                index += 2;
            }

            return fqnResolver.resolve(Fqn.from(builder.build())).accept(new NamedEntityVisitor<Type>() {
                @Override
                public Type defaultAction(NamedEntity namedEntity) {
                    return namedEntity.getType();
                }
            });
        }

        protected final String advance() {
            ++index;
            return peek();
        }

        protected final String read() {
            final String token = tokens.get(index);
            ++index;
            return token;
        }

        protected final String peek() {
            return index < tokens.size() ? tokens.get(index) : null;
        }

        protected final boolean test(String expected) {
            return Objects.equals(peek(), expected);
        }

        protected final void assrt(String expected) {
            if (!test(expected))
                throw new IllegalArgumentException("Expected: '" + expected + "', Actual: '" + peek() + "'");
        }
    }

    private class TypeParse extends Parse<TypeMixin<RuntimeContext, ?>> {
        private TypeMixin<RuntimeContext, ?> result;

        public TypeParse(TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(typeReferenceContext, tokens);
        }

        protected final TypeMixin<RuntimeContext, ?> performInternal() {
            switch (peek()) {
                case "(":
                    advance();
                    result = performInternal();
                    assrt(")");
                    advance();
                    return result;
                case ".":
                    result = TypeMixin.rebind(lookUpNamedType());
                    if (test("\u3008"))
                        result = invoke((TypeConstructorMixin<RuntimeContext, ?, ?>) result);

                    while (peek() != null && !test(")") && !test("\u3009") && !test(","))
                        throw null; // TODO & and |

                    return result;
                default:
                    return result = lookUpTypeVariable(read());
            }
        }

        private TypeMixin<RuntimeContext, ?> invoke(TypeConstructorMixin<RuntimeContext, ?, ?> typeConstructor) {
            assrt("\u3008");
            advance();

            ImmutableList.Builder<TypeMixin<RuntimeContext, ?>> builder = ImmutableList.builder();

            while (!test("\u3009")) {
                builder.add(performInternal());

                // This is lenient wrt. trailing commas..
                if (test(","))
                    advance();
            }
            advance();

            return typeConstructor.apply(AbstractTypeList.of(builder.build()));
        }

        private TypeVariableImpl<RuntimeContext, ?> lookUpTypeVariable(String name) {
            final AbstractConstraints<RuntimeContext> constraints = AbstractConstraints.rebind(typeReferenceContext.getConstraints());

            Monosemous<RuntimeContext> currentConstraints = constraints.isDisjunctive()
                    ? ((Disjunction<RuntimeContext>) constraints).getOptions().iterator().next()
                    : (Monosemous<RuntimeContext>) constraints;

            while (true) {
                Optional<TypeParameterImpl<RuntimeContext>> parameter = currentConstraints.getTypeParams().stream()
                        .filter(p -> p.getTags().get(RuntimeTypeName.TAG).debug().equals(name))
                        .findAny();

                if (parameter.isPresent())
                    return parameter.get().getVariable();

                Monosemous<RuntimeContext> parent = currentConstraints.getParent();
                checkState(parent != currentConstraints);
                currentConstraints = parent;
            }
        }
    }

    private class TypeParameterListParse extends Parse<TypeParameterListImpl<RuntimeContext>> {
        public TypeParameterListParse(TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(typeReferenceContext, tokens);
        }

        protected final TypeParameterListImpl<RuntimeContext> performInternal() {
            assrt("\u3008");
            advance();

            final ImmutableList<Pair<Variance, IndividualTags>> parameters = collectParameters().stream()
                    .map(p -> Pair.of(p.a, IndividualTags.of(RuntimeTypeName.TAG, RuntimeTypeName.of(p.b))))
                    .collect(list());

            final AtomicReference<AbstractConstraints<RuntimeContext>> constraints = new AtomicReference<>();
            final TypeParameterListImpl<RuntimeContext> typeParameters = TypeParameterListImpl.create(new TypeParameterListDeclaration<RuntimeContext>() {
                @Override
                public <O> O supplyParameters(RuntimeContext context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                    return interpreter.interpret(parameters, context.getChecker());
                }

                @Override
                public <O> O supplyConstraints(RuntimeContext context, Interpreter<AbstractConstraints<RuntimeContext>, O> interpreter) {
                    return interpreter.interpret(constraints.get(), context.getChecker());
                }
            }).bind(context);


            if (test("|"))
                throw null;

            final AbstractConstraints<RuntimeContext> contextualConstraints = typeReferenceContext.getOuterConstraints()
                    .map(AbstractConstraints::<RuntimeContext>rebind)
                    .getOrReturn(AbstractConstraints.none());
            constraints.set(AbstractConstraints.trivial(context, contextualConstraints, typeParameters));

            return typeParameters;
        }

        private ImmutableList<Pair<Variance, String>> collectParameters() {
            if (test("|") || test("\u3009"))
                return ImmutableList.of();

            ImmutableList.Builder<Pair<Variance, String>> builder = ImmutableList.builder();

            do {
                Variance variance = Variance.INVARIANT;
                while (test("+") || test("-")) {
                    variance = variance.and(test("+") ? Variance.COVARIANT : Variance.CONTRAVARIANT);
                    advance();
                }

                builder.add(Pair.of(variance, read()));
            } while (test(",") && Objects.equals(read(), ","));

            return builder.build();
        }
    }
}
