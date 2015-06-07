package de.benshu.cofi.binary.deserialization.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.AdHoc;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.TypeVariableImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Disjunction;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.templates.TemplateTypeConstructorMixin;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkState;
import static de.benshu.commons.core.streams.Collectors.list;

public class TypeParser {
    private final FqnResolver fqnResolver;
    private final Namer namer;

    public TypeParser(FqnResolver fqnResolver, Namer namer) {
        this.fqnResolver = fqnResolver;
        this.namer = namer;
    }

    public <X extends TypeSystemContext<X>> ContextPrepared<X> in(X context, TypeReferenceContext typeReferenceContext) {
        return new ContextPrepared<>(context, typeReferenceContext);
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

    public class ContextPrepared<X extends TypeSystemContext<X>> {
        private final X context;
        private final TypeReferenceContext typeReferenceContext;

        public ContextPrepared(X context, TypeReferenceContext typeReferenceContext) {
            this.context = context;
            this.typeReferenceContext = typeReferenceContext;
        }

        public TypeParameterListImpl<X> parseTypeParameters(String typeParametersString) {
            return new TypeParameterListParse<>(context, typeReferenceContext, tokenizeTypeString(typeParametersString)).perform();
        }

        public TemplateTypeConstructorMixin<X> parseTemplateTypeConstructor(TypeParameterListImpl<X> typeParameters, String typeString) {
            return AdHoc.templateTypeConstructor(context, typeParameters, parseTemplateType(typeString));
        }

        public TemplateTypeImpl<X> parseTemplateType(String typeString) {
            return (TemplateTypeImpl<X>) parseType(typeString);
        }

        public TypeMixin<X, ?> parseType(String typeString) {
            return new TypeParse<>(context, typeReferenceContext, tokenizeTypeString(typeString)).perform();
        }
    }

    private abstract class Parse<X extends TypeSystemContext<X>, T> {
        final X context;
        final TypeReferenceContext typeReferenceContext;

        private final ImmutableList<String> tokens;
        private int index = 0;

        public Parse(X context, TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            this.context = context;
            this.typeReferenceContext = typeReferenceContext;
            this.tokens = tokens;
        }

        public T perform() {
            checkState(index == 0);

            return performInternal();
        }

        protected abstract T performInternal();

        protected final TypeMixin<X, ?> lookUpNamedType() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            while (test(".")) {
                builder.add(tokens.get(index + 1));
                index += 2;
            }

            return fqnResolver.<X>resolve(context, Fqn.from(builder.build()));
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

    private class TypeParse<X extends TypeSystemContext<X>> extends Parse<X, TypeMixin<X, ?>> {
        private TypeMixin<X, ?> result;

        public TypeParse(X context, TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(context, typeReferenceContext, tokens);
        }

        @Override
        protected final TypeMixin<X, ?> performInternal() {
            switch (peek()) {
                case "(":
                    advance();
                    result = performInternal();
                    assrt(")");
                    advance();
                    return result;
                case ".":
                    result = lookUpNamedType();
                    if (test("\u3008"))
                        result = invoke((TypeConstructorMixin<X, ?, ?>) result);

                    while (peek() != null && !test(")") && !test("\u3009") && !test(","))
                        throw null; // TODO & and |

                    return result;
                default:
                    return result = lookUpTypeVariable(read());
            }
        }

        private TypeMixin<X, ?> invoke(TypeConstructorMixin<X, ?, ?> typeConstructor) {
            assrt("\u3008");
            advance();

            ImmutableList.Builder<TypeMixin<X, ?>> builder = ImmutableList.builder();

            while (!test("\u3009")) {
                builder.add(performInternal());

                // This is lenient wrt. trailing commas..
                if (test(","))
                    advance();
            }
            advance();

            return typeConstructor.apply(AbstractTypeList.of(builder.build()));
        }

        private TypeVariableImpl<X, ?> lookUpTypeVariable(String name) {
            final AbstractConstraints<X> constraints = typeReferenceContext.getConstraints(context);

            Monosemous<X> currentConstraints = constraints.isDisjunctive()
                    ? ((Disjunction<X>) constraints).getOptions().iterator().next()
                    : (Monosemous<X>) constraints;

            while (true) {
                Optional<TypeParameterImpl<X>> parameter = currentConstraints.getTypeParams().stream()
                        .filter(p -> namer.getNameOf(p).equals(name))
                        .findAny();

                if (parameter.isPresent())
                    return parameter.get().getVariable();

                Monosemous<X> parent = currentConstraints.getParent();
                checkState(parent != currentConstraints);
                currentConstraints = parent;
            }
        }
    }

    private class TypeParameterListParse<X extends TypeSystemContext<X>> extends Parse<X, TypeParameterListImpl<X>> {
        public TypeParameterListParse(X context, TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(context, typeReferenceContext, tokens);
        }

        @Override
        protected final TypeParameterListImpl<X> performInternal() {
            assrt("\u3008");
            advance();

            final ImmutableList<Pair<Variance, IndividualTags>> parameters = collectParameters().stream()
                    .map(p -> Pair.of(p.a, namer.name(p.b)))
                    .collect(list());

            final AtomicReference<AbstractConstraints<X>> constraints = new AtomicReference<>();
            final TypeParameterListImpl<X> typeParameters = TypeParameterListImpl.create(new TypeParameterListDeclaration<X>() {
                @Override
                public <O> O supplyParameters(X context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                    return interpreter.interpret(parameters, context.getChecker());
                }

                @Override
                public <O> O supplyConstraints(X context, Interpreter<AbstractConstraints<X>, O> interpreter) {
                    return interpreter.interpret(constraints.get(), context.getChecker());
                }
            }).bind(context);


            if (test("|"))
                throw null;

            final AbstractConstraints<X> contextualConstraints = typeReferenceContext.getOuterConstraints(context)
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

    public interface FqnResolver {
        <X extends TypeSystemContext<X>> TypeMixin<X, ?> resolve(X context, Fqn fqn);
    }

    public interface Namer {
        IndividualTags name(String name);

        String getNameOf(TypeParameterImpl<?> typeParameter);
    }
}
