package de.benshu.cofi.cofic.frontend;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.types.tags.IndividualTag;

/**
 * <em>Tuplage</em> reflects the parameter count for a parameter list. Specifically
 * {@code mark(coords : (Int, Int)) : Unit} and {@code mark(x : Int, y : Int) : Unit} both have the
 * signature {@code Function<(Int, Int), Unit>}. However the first has a tuplage of {@code [true]}
 * while the second has {@code [false]}. Tuplage impacts which invocations are valid,
 * {@code mark((x, y))} or {@code mark(x, y)}. The tuplage for a parameter list is {@code true} when
 * it has the length of one.
 */

public class Tuplage {
    public static final IndividualTag<Tuplage> TAG = IndividualTag.named("Tuplage");

    private final ImmutableList<Boolean> tuplage;

    public Tuplage(ImmutableList<Boolean> tuplage) {
        this.tuplage = tuplage;
    }
}
