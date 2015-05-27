package de.benshu.cofi.runtime;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.runtime.internal.Ancestry;
import de.benshu.cofi.runtime.internal.Constructor;
import de.benshu.cofi.runtime.internal.TypeReference;
import de.benshu.cofi.types.ProperType;
import de.benshu.cofi.types.Type;
import de.benshu.cofi.types.TypeList;
import de.benshu.jswizzle.data.Data;

import java.util.function.Supplier;

public class MemberAccess implements Expression, MemberAccessAccessors {
    @Data
    final Expression primary;
    @Data
    final String memberName;

    final Supplier<TypeList<Type>> typeArguments;

    final Supplier<ProperType> type;

    public MemberAccess(
            Ancestry ancestry,
            Constructor<Expression> primary,
            String memberName,
            ImmutableList<TypeReference<?>> typeArguments,
            TypeReference<ProperType> type) {

        final Ancestry ancestryIncludingMe = ancestry.append(this);

        this.primary = ancestryIncludingMe.construct(primary);
        this.memberName = memberName;
        this.typeArguments = ancestry.resolveList(typeArguments);
        this.type = ancestryIncludingMe.resolve(type);
    }

    @Override
    public <R> R accept(ExpressionVisitor<R> visitor) {
        return visitor.visitMemberAccess(this);
    }

    @Override
    public ProperType getType() {
        return type.get();
    }

    public TypeList<?> getTypeArguments() {
        return typeArguments.get();
    }

    @Override
    public String debug() {
        final String arguments = typeArguments.get().isEmpty() ? "" : typeArguments.get().debug();
        return getPrimary().debug() + "." + getMemberName() + arguments;
    }
}
