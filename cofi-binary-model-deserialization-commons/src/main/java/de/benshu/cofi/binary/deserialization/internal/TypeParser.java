package de.benshu.cofi.binary.deserialization.internal;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.types.Variance;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.TypeConstructorMixin;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeParameterImpl;
import de.benshu.cofi.types.impl.TypeParameterListImpl;
import de.benshu.cofi.types.impl.constraints.AbstractConstraints;
import de.benshu.cofi.types.impl.constraints.Disjunction;
import de.benshu.cofi.types.impl.constraints.Monosemous;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.TypeParameterListDeclaration;
import de.benshu.cofi.types.impl.intersections.AnonymousIntersectionType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.unions.AnonymousUnionType;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.commons.core.Pair;

import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;
import static de.benshu.commons.core.streams.Collectors.list;

public class TypeParser {
    private final Namer namer;

    public TypeParser(Namer namer) {
        this.namer = namer;
    }

    public <X extends BinaryModelContext<X>> ContextPrepared in(TypeReferenceContext typeReferenceContext) {
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

        public UnboundTypeParameterList parseTypeParameters(String typeParametersString) {
            return new TypeParameterListParse(typeReferenceContext, tokenizeTypeString(typeParametersString)).perform();
        }

        public UnboundType parseType(String typeString) {
            return new TypeParse(typeReferenceContext, tokenizeTypeString(typeString)).perform();
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

        protected final UnboundType readAndResolveQualifiedTypeName() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            while (test(".")) {
                builder.add(tokens.get(index + 1));
                index += 2;
            }

            final Fqn fqn = Fqn.from(builder.build());

            return new UnboundType() {
                @Override
                public <X extends BinaryModelContext<X>> TypeMixin<X, ?> bind(X context) {
                    return context.resolveQualifiedTypeName(fqn);
                }
            };
        }

        protected final String advance() {
            ++index;
            return peek();
        }

        protected final boolean advanceIf(String expected) {
            if (!test(expected))
                return false;

            ++index;
            return true;
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

    private class TypeParse extends Parse<UnboundType> {

        public TypeParse(TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(typeReferenceContext, tokens);
        }

        @Override
        protected final UnboundType performInternal() {
            if (peek().equals("(")) {
                advance();
                UnboundType result = performInternal();
                assrt(")");
                advance();
                return result;
            } else {
                final UnboundType firstNamed = parseSingleNamedType();

                if (peek() == null || !peek().equals("|") && !peek().equals("&"))
                    return firstNamed;

                final String separator = read();
                final ImmutableList<UnboundType> elements = parseFurtherElements(firstNamed, separator);

                return new UnboundType() {
                    @Override
                    public <X extends BinaryModelContext<X>> TypeMixin<X, ?> bind(X context) {
                        final AbstractTypeList<X, ProperTypeMixin<X, ?>> boundElements = elements.stream()
                                .map(e -> (ProperTypeMixin<X, ?>) e.bind(context))
                                .collect(typeList());

                        return separator.equals("|")
                                ? AnonymousUnionType.create(context, boundElements)
                                : AnonymousIntersectionType.create(context, boundElements);
                    }
                };
            }
        }

        private ImmutableList<UnboundType> parseFurtherElements(UnboundType firstElement, String separator) {
            ImmutableList.Builder<UnboundType> builder = ImmutableList.builder();
            builder.add(firstElement);

            do
                builder.add(performInternal());
            while (advanceIf(separator));

            return builder.build();
        }

        private UnboundType parseSingleNamedType() {
            if (peek().equals(".")) {
                UnboundType resolved = readAndResolveQualifiedTypeName();
                return test("\u3008")
                        ? invoke(resolved)
                        : resolved;
            } else {
                return resolveTypeVariableName(read());
            }
        }

        private UnboundType invoke(UnboundType typeConstructor) {
            assrt("\u3008");
            advance();

            ImmutableList.Builder<UnboundType> builder = ImmutableList.builder();

            while (!test("\u3009")) {
                builder.add(performInternal());

                // This is lenient wrt. trailing commas..
                if (test(","))
                    advance();
            }
            advance();

            final ImmutableList<UnboundType> arguments = builder.build();
            return new UnboundType() {
                @Override
                public <X extends BinaryModelContext<X>> TypeMixin<X, ?> bind(X context) {
                    final TypeConstructorMixin<X, ?, ?> boundTypeConstructor = (TypeConstructorMixin<X, ?, ?>) typeConstructor.bind(context);
                    final AbstractTypeList<X, TypeMixin<X, ?>> boundArguments = arguments.stream().map(a -> a.<X>bind(context)).collect(typeList());
                    return boundTypeConstructor.apply(boundArguments);
                }
            };
        }

        private UnboundType resolveTypeVariableName(String name) {
            return new UnboundType() {
                @Override
                public <X extends BinaryModelContext<X>> TypeMixin<X, ?> bind(X context) {
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
            };
        }
    }

    private class TypeParameterListParse extends Parse<UnboundTypeParameterList> {
        public TypeParameterListParse(TypeReferenceContext typeReferenceContext, ImmutableList<String> tokens) {
            super(typeReferenceContext, tokens);
        }

        @Override
        protected final UnboundTypeParameterList performInternal() {
            return performInternalInternal();
        }

        private <Y extends BinaryModelContext<Y>> UnboundTypeParameterList performInternalInternal() {
            assrt("\u3008");
            advance();

            final ImmutableList<Pair<Variance, IndividualTags>> parameters = collectParameters().stream()
                    .map(p -> Pair.of(p.a, namer.name(p.b)))
                    .collect(list());

            final de.benshu.cofi.types.impl.UnboundTypeParameterList<Y> unboundTypeParameterList = TypeParameterListImpl.create(new TypeParameterListDeclaration<Y>() {
                @Override
                public <O> O supplyParameters(Y context, Interpreter<ImmutableList<Pair<Variance, IndividualTags>>, O> interpreter) {
                    return interpreter.interpret(parameters, context.getChecker());
                }

                @Override
                public <O> O supplyConstraints(Y context, TypeParameterListImpl<Y> bound, Interpreter<AbstractConstraints<Y>, O> interpreter) {
                    final AbstractConstraints<Y> contextualConstraints = typeReferenceContext.getOuterConstraints(context)
                            .getOrReturn(AbstractConstraints.none());

                    return interpreter.interpret(AbstractConstraints.trivial(context, contextualConstraints, bound), context.getChecker());
                }
            });

            if (test("|"))
                throw null;

            return new UnboundTypeParameterList() {
                @Override
                public <X extends BinaryModelContext<X>> TypeParameterListImpl<X> bind(X context) {
                    final de.benshu.cofi.types.impl.UnboundTypeParameterList<X> hack = (de.benshu.cofi.types.impl.UnboundTypeParameterList<X>) unboundTypeParameterList;
                    return hack.bind(context);
                }
            };

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

    public interface Namer {
        IndividualTags name(String name);

        String getNameOf(TypeParameterImpl<?> typeParameter);
    }
}
