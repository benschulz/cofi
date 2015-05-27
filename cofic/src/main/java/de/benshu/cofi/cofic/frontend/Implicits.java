/**
 *
 */
package de.benshu.cofi.cofic.frontend;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.members.MethodImpl;
import de.benshu.cofi.types.impl.members.MethodSignatureImpl;
import de.benshu.cofi.types.tags.DefaultingIndividualTag;
import de.benshu.cofi.types.tags.IndividualTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;
import de.benshu.commons.core.Pair;

import java.util.Collections;
import java.util.SortedMap;

public abstract class Implicits {
    public static DefaultingIndividualTag<Implicits> TAG = IndividualTag.named("Implicits")
            .defaultingTo(NoImplicits.INSTANCE).inheritedEqually(Implicits::deriveFrom);

    private static Optional<Implicits> deriveFrom(Tags tags) {
        final Optional<MethodImpl<Pass>> method = (Optional<MethodImpl<Pass>>) (Object) tags.getTagged(MethodImpl.class);

        return method.flatMap(m -> {
            final ImmutableList<MethodSignatureImpl<Pass>> signatures = m.getSignatures();
            final Optional<Implicits> any = signatures.get(0).getTags().tryGet(TAG);

            return signatures.stream()
                    .map(s -> s.getTags().tryGet(TAG))
                    .reduce(any, (a, b) -> a); // TODO enforce equality
        });
    }

    public static Implicits none() {
        return NoImplicits.INSTANCE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public abstract int getParamCount();

    public abstract int getSize();

    public abstract Implicits getTail();

    public abstract int getTypeParamCount();

    public static class Builder {
        private final SortedMap<Integer, Pair<Integer, Integer>> data = Maps.newTreeMap(Collections.reverseOrder());
        private int index = 0;

        private Builder() {}

        public void add(int tpImplicits, int paramCount) {
            if (tpImplicits > 0 || paramCount > 0) {
                final Pair<Integer, Integer> p = Pair.of(tpImplicits, paramCount);
                data.put(index, p);
            }

            ++index;
        }

        public Implicits build() {
            if (data.isEmpty()) {
                return NoImplicits.INSTANCE;
            }

            Implicits tail = NoImplicits.INSTANCE;
            for (int i = data.firstKey(); i >= 0; --i) {
                final Pair<Integer, Integer> p = data.get(i);

                if (p == null) {
                    tail = new SomeImplicits(0, 0, tail);
                } else {
                    tail = new SomeImplicits(p.a, p.b, tail);
                }
            }

            return tail;
        }
    }

    private static final class NoImplicits extends Implicits {
        public static final Implicits INSTANCE = new NoImplicits();

        @Override
        public int getParamCount() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public Implicits getTail() {
            return this;
        }

        @Override
        public int getTypeParamCount() {
            return 0;
        }
    }

    private static class SomeImplicits extends Implicits {
        private final int typeParamCount;
        private final int paramCount;
        private final Implicits tail;

        public SomeImplicits(int typeParamCount, int paramCount, Implicits tail) {
            this.typeParamCount = typeParamCount;
            this.paramCount = paramCount;
            this.tail = tail;
        }

        @Override
        public int getParamCount() {
            return paramCount;
        }

        @Override
        public int getSize() {
            return 1 + getTail().getSize();
        }

        @Override
        public Implicits getTail() {
            return tail;
        }

        @Override
        public int getTypeParamCount() {
            return typeParamCount;
        }
    }
}