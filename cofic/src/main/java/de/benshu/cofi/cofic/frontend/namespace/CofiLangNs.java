package de.benshu.cofi.cofic.frontend.namespace;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.cofic.notes.Source;
import de.benshu.cofi.common.Fqn;
import de.benshu.cofi.model.impl.ExpressionNode;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.commons.core.streams.Collectors.list;

public class CofiLangNs extends AbstractNamespace {
    public static CofiLangNs create(ImmutableList<String> names, TypeMixin<Pass, ?> type) {
        return new CofiLangNs(names, type);
    }

    private final ImmutableList<String> names;
    private final TypeMixin<Pass, ?> type;

    public CofiLangNs(ImmutableList<String> names, TypeMixin<Pass, ?> type) {
        this.names = names;
        this.type = type;
    }

    @Override
    TypeMixin<Pass, ?> asType(LookUp lookUp) {
        return type;
    }

    @Override
    ExpressionNode<Pass> getAccessor(LookUp lookUp) {
        return getAccessor(Fqn.from("cofi", "lang").getDescendant(names));
    }

    @Override
    Optional<AbstractNamespace> tryResolveNamespaceLocally(LookUp lookUp, String name, Source.Snippet src) {
        final ImmutableList<String> combined = Stream.concat(names.stream(), Stream.of(name)).collect(list());
        return lookUp.tryLookUpLangType(combined).map(t -> new CofiLangNs(combined, t));
    }

    @Override
    Optional<AbstractResolution> tryResolveLocally(LookUp lookUp, AbstractNamespace fromNamespace, String name) {
        return lookUp.tryLookUpLangMember(name)
                .map(m -> new DefaultResolution(m.getType(), getAccessor(lookUp), m));
    }
}
