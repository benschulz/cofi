package de.benshu.cofi.types.tags;

import de.benshu.commons.core.Optional;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.benshu.commons.core.Optional.none;

public class HashTags implements Tags {
    public static final java.util.stream.Collector<Entry<?>, ?, ConcurrentMap<Tag<?>, Object>> CONCURRENT_MAP = Collectors.toConcurrentMap(Tags.Entry::getTag, Tags.Entry::getValue);

    public static HashTags createEmpty(Object tagged) {
        return create(tagged, IndividualTags.empty());
    }

    public static HashTags create(Object tagged, IndividualTags individualTags) {
        return create(tagged, DefaultDeriver.INSTANCE, individualTags);
    }

    public static HashTags create(Object tagged, Deriver deriver) {
        return create(tagged, deriver, IndividualTags.empty());
    }

    public static HashTags create(Object tagged, Deriver deriver, IndividualTags individualTags) {
        return new HashTags(tagged, deriver, individualTags.stream().collect(CONCURRENT_MAP));
    }

    private final Object tagged;
    private final ConcurrentMap<Tag<?>, Object> entries;
    private final Deriver deriver;

    private HashTags(Object tagged, Deriver deriver, ConcurrentMap<Tag<?>, Object> entries) {
        this.tagged = tagged;
        this.deriver = deriver;
        this.entries = entries;
    }

    @Override
    public <T> Optional<T> getTagged(Class<? extends T> expectedType) {
        return Optional.cast(expectedType, tagged);
    }

    public Deriver getDeriver() {
        return deriver;
    }

    @Override
    public <T> HashTags set(IndividualTag<T> tag, T value) {
        return setAll(HashIndividualTags.of(tag, value));
    }

    @Override
    public HashTags setAll(IndividualTags tags) {
        return new HashTags(tagged, deriver, Stream.concat(
                stream().filter(e -> e.getTag() instanceof IndividualTag<?>),
                tags.stream()
        ).collect(CONCURRENT_MAP));
    }

    @Override
    public Stream<Entry<?>> stream() {
        return entries.entrySet().stream().map(this::toTagEntry);
    }

    private <T> Entry<T> toTagEntry(Map.Entry<Tag<?>, T> mapEntry) {
        return new Entry<T>() {
            @Override
            public Tag<T> getTag() {
                return (Tag<T>) mapEntry.getKey();
            }

            @Override
            public T getValue() {
                return mapEntry.getValue();
            }
        };
    }

    @Override
    public <T> Optional<T> tryGet(Tag<T> tag) {
        return Optional.from((T) entries.get(tag))
                .or(() -> tryGetDerivable(tag));
    }

    private <T> Optional<T> tryGetDerivable(Tag<T> tag) {
        return Optional.cast(DerivableTag.class, tag)
                .flatMap(t -> deriver.tryDerive(this, (DerivableTag<T>) tag));
    }

    public static abstract class Deriver {
        public <T> Optional<T> tryDerive(HashTags tags, DerivableTag<T> tag) {
            return tag instanceof DirectlyDerivableTag<?>
                    ? ((DirectlyDerivableTag<T>) tag).tryDeriveDirectly(tags).or(() -> tryDeriveIndirectly(tags, tag))
                    : tryDeriveIndirectly(tags, tag);
        }

        public <T> Optional<T> tryDeriveIndirectly(HashTags tags, DerivableTag<T> tag) {
            return none();
        }
    }

    private static class DefaultDeriver extends Deriver {
        private static final DefaultDeriver INSTANCE = new DefaultDeriver();
    }
}
