package de.benshu.cofi.cofic.frontend;

import de.benshu.cofi.cofic.Pass;
import de.benshu.cofi.types.impl.ProperTypeMixin;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.types.impl.members.AbstractMember;
import de.benshu.cofi.types.tags.DerivableTag;
import de.benshu.cofi.types.tags.DirectlyDerivableTag;
import de.benshu.cofi.types.tags.IndividualTags;
import de.benshu.cofi.types.tags.RefinementDerivableTag;
import de.benshu.cofi.types.tags.SubstitutionDerivableTag;
import de.benshu.cofi.types.tags.Tags;
import de.benshu.commons.core.Optional;

import java.util.stream.Stream;

import static de.benshu.cofi.types.impl.lists.AbstractTypeList.typeList;

public class Owners {
    public static final DerivableTag<Owners> TAG = Tag.INSTANCE;

    public static Owners create(ProperTypeMixin<Pass, ?> owner) {
        return new Owners(AbstractTypeList.of(owner));
    }

    private final AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> owners;

    private Owners(AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> owners) {
        this.owners = owners;
    }

    public AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> getOwners() {
        // TODO owners must always be proper types => fix types *everywhere*
        return owners;
    }

    private enum Tag implements DirectlyDerivableTag<Owners>, RefinementDerivableTag<Owners>, SubstitutionDerivableTag<Owners> {
        INSTANCE;

        @Override
        public String debug() {
            return "Owners";
        }

        @Override
        public Optional<Owners> tryDeriveDirectly(Tags tags) {
            return getTaggedMember(tags).map(m -> new Owners(AbstractTypeList.of(m.getOwner())));
        }

        @Override
        public Optional<Owners> tryDeriveFromRefinement(Tags unrefined, IndividualTags refinement, Tags refined) {
            return replaceOwner(unrefined, refined);
        }

        @Override
        public Optional<Owners> tryDeriveFromSubstitution(Tags unsubstituted, Tags substituted) {
            return replaceOwner(unsubstituted, substituted);
        }

        private Optional<Owners> replaceOwner(Tags toBeReplaced, Tags replacement) {
            return toBeReplaced.tryGet(this)
                    .map(o -> {
                        final ProperTypeMixin<Pass, ?> unsubstitutedOwner = getTaggedMember(toBeReplaced).get().getOwner();
                        final ProperTypeMixin<Pass, ?> substitutedOwner = getTaggedMember(replacement).get().getOwner();

                        final AbstractTypeList<Pass, ProperTypeMixin<Pass, ?>> newOwners = Stream.concat(
                                o.owners.stream().filter(x -> x != unsubstitutedOwner),
                                Stream.of(substitutedOwner)
                        ).collect(typeList());

                        return new Owners(newOwners);
                    });
        }

        private Optional<AbstractMember<Pass>> getTaggedMember(Tags tags) {
            return (Optional<AbstractMember<Pass>>) (Object) tags.getTagged(AbstractMember.class);
        }
    }
}
