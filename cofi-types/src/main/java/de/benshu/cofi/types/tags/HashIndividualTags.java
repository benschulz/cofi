package de.benshu.cofi.types.tags;

import com.google.common.collect.ImmutableMap;
import de.benshu.commons.core.Optional;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static de.benshu.commons.core.Optional.none;
import static de.benshu.commons.core.Optional.some;

public abstract class HashIndividualTags implements IndividualTags {
    private static final Empty EMPTY = new Empty();

    public static Collector<? super Entry<?>, ?, IndividualTags> collector() {
        return Collectors.reducing(empty(), HashIndividualTags::of, IndividualTags::setAll);
    }

    static HashIndividualTags empty() {
        return EMPTY;
    }

    public static HashIndividualTags of(Entry<?> entry) {
        return ofInternal(entry);
    }

    private static <T> HashIndividualTags ofInternal(Entry<T> entry) {
        return new SingletonHashIndividualTags<>(entry.getTag(), entry.getValue());
    }

    public static <T> HashIndividualTags of(IndividualTag<T> tag, T value) {
        return new SingletonHashIndividualTags<>(tag, value);
    }

    private HashIndividualTags() {}

    @Override
    public abstract <T> Optional<T> tryGet(Tag<T> tag);

    private static final class Big extends HashIndividualTags {
        private final ImmutableMap<IndividualTag<?>, Object> entries;

        public Big(ImmutableMap<IndividualTag<?>, Object> entries) {
            this.entries = entries;
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public IndividualTags setAll(IndividualTags tags) {
            final ImmutableMap.Builder<IndividualTag<?>, Object> builder = ImmutableMap.builder();
            entries.entrySet().stream()
                    .filter(e -> !tags.contains(e.getKey()))
                    .forEach(builder::put);
            tags.stream()
                    .forEach(e -> builder.put(e.getTag(), e.getValue()));
            return new Big(builder.build());
        }

        @Override
        public Stream<Entry<?>> stream() {
            return entries.entrySet().stream().map(this::toEntry);
        }

        private Entry<?> toEntry(Map.Entry<IndividualTag<?>, Object> mapEntry) {
            final IndividualTag<Object> tag = (IndividualTag<Object>) mapEntry.getKey();
            final Object value = mapEntry.getValue();

            return new Entry<Object>() {
                @Override
                public IndividualTag<Object> getTag() {
                    return tag;
                }

                @Override
                public Object getValue() {
                    return value;
                }
            };
        }

        @Override
        public <T> Optional<T> tryGet(Tag<T> tag) {
            return Optional.from((T) entries.get(tag));
        }
    }

    private static final class Empty extends HashIndividualTags {
        @Override
        public <T> HashIndividualTags set(IndividualTag<T> tag, T value) {
            return new SingletonHashIndividualTags<>(tag, value);
        }

        @Override
        public IndividualTags setAll(IndividualTags tags) {
            return tags;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Stream<Entry<?>> stream() {
            return Stream.empty();
        }

        @Override
        public <T> Optional<T> tryGet(Tag<T> tag) {
            return none();
        }
    }

    private static final class SingletonHashIndividualTags<T> extends HashIndividualTags {
        private final IndividualTag<T> tag;
        private final T value;

        public SingletonHashIndividualTags(IndividualTag<T> tag, T value) {
            this.tag = tag;
            this.value = value;
        }

        @Override
        public <U> IndividualTags set(IndividualTag<U> tag, U value) {
            return new Small(new Object[]{
                    this.tag, this.value,
                    tag, value
            });
        }

        @Override
        public IndividualTags setAll(IndividualTags tags) {
            return tags.set(tag, value);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Stream<Entry<?>> stream() {
            return Stream.of(new Entry<T>() {
                @Override
                public IndividualTag<T> getTag() {
                    return tag;
                }

                @Override
                public T getValue() {
                    return value;
                }
            });
        }

        @Override
        public <U> Optional<U> tryGet(Tag<U> tag) {
            return tag == this.tag || tag.equals(this.tag)
                    ? some((U) this.value)
                    : none();
        }
    }

    private static final class Small extends HashIndividualTags {
        public static final int THRESHOLD = 16;

        private final Object[] entries;

        private Small(Object[] entries) {
            checkArgument(entries.length / 2 <= THRESHOLD);

            this.entries = entries;
        }

        @Override
        public boolean contains(IndividualTag<?> tag) {
            return tryFind(tag) >= 0;
        }

        @Override
        public IndividualTags setAll(IndividualTags tags) {
            if (tags.size() > THRESHOLD)
                return toBig(tags);

            final Object[] newEntries = Arrays.copyOf(entries, entries.length + 2 * tags.size());

            final AtomicInteger i = new AtomicInteger(0);
            for (int j = 0; j < entries.length; j += 2) {
                final IndividualTag<?> tag = (IndividualTag<?>) entries[j];
                if (!tags.contains(tag)) {
                    newEntries[i.getAndIncrement()] = tag;
                    newEntries[i.getAndIncrement()] = entries[j + 1];
                }
            }

            if (i.get() / 2 + tags.size() > THRESHOLD) {
                ImmutableMap.Builder<IndividualTag<?>, Object> builder = ImmutableMap.builder();
                final int limit = i.get();
                for (int j = 0; j < limit; j += 2)
                    builder.put((IndividualTag<?>) entries[j], entries[j + 1]);
                toBig(tags, builder);
            }

            tags.stream().forEach(e -> {
                final int j = i.getAndAdd(2);
                newEntries[j] = e.getTag();
                newEntries[j + 1] = e.getValue();
            });

            return new Small(i.get() == newEntries.length ? newEntries : Arrays.copyOf(newEntries, i.get()));
        }

        private IndividualTags toBig(IndividualTags tags) {
            ImmutableMap.Builder<IndividualTag<?>, Object> builder = ImmutableMap.builder();

            for (int i = 0; i < entries.length; i += 2) {
                final IndividualTag<?> tag = (IndividualTag<?>) entries[i];
                if (!tags.contains(tag))
                    builder.put(tag, entries[i + 1]);
            }

            return toBig(tags, builder);
        }

        private IndividualTags toBig(IndividualTags tags, ImmutableMap.Builder<IndividualTag<?>, Object> builder) {
            tags.stream().forEach(e -> builder.put(e.getTag(), e.getValue()));
            return new Big(builder.build());
        }

        @Override
        public int size() {
            return entries.length / 2;
        }

        @Override
        public Stream<Entry<?>> stream() {
            return IntStream.range(0, size()).map(i -> 2 * i).mapToObj(this::getEntryAt);
        }

        private Entry<?> getEntryAt(int index) {
            return new Entry<Object>() {
                @Override
                public IndividualTag<Object> getTag() {
                    return (IndividualTag<Object>) entries[index];
                }

                @Override
                public Object getValue() {
                    return entries[index + 1];
                }
            };
        }

        @Override
        public <T> Optional<T> tryGet(Tag<T> tag) {
            final int index = tryFind(tag);

            return index >= 0 ? some((T) entries[index + 1]) : none();
        }

        private int tryFind(Tag<?> tag) {
            for (int i = 0; i < entries.length; i += 2)
                if (entries[i] == tag)
                    return i;

            final int hash = tag.hashCode();
            for (int i = 0; i < entries.length; i += 2)
                if (entries[i].hashCode() == hash && entries[i].equals(tag))
                    return i;

            return -1;
        }
    }
}
