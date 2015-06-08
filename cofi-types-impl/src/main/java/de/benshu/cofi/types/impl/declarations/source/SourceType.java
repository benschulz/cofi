package de.benshu.cofi.types.impl.declarations.source;

import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.cofic.notes.Source;

import static com.google.common.base.Preconditions.checkArgument;

public class SourceType<X extends TypeSystemContext<X>> {
    public static <X extends TypeSystemContext<X>> SourceType<X> of(TypeMixin<X, ?> type) {
        return of(type, null);
    }

    public static <X extends TypeSystemContext<X>> SourceType<X> of(TypeMixin<X, ?> type, Source.Snippet source) {
        return new SourceType<>(type, source);
    }

    private final TypeMixin<X, ?> type;
    private final Source.Snippet source;

    private SourceType(TypeMixin<X, ?> type, Source.Snippet source) {
        checkArgument(type != null);

        this.type = type;
        this.source = source;
    }

    public TypeMixin<X, ?> getType() {
        return type;
    }

    public Source.Snippet getSource() {
        return source;
    }

}
