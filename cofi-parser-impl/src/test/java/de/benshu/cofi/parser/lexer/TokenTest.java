package de.benshu.cofi.parser.lexer;

import com.google.common.collect.ImmutableSet;
import de.benshu.cofi.parser.lexer.Token.Kind;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TokenTest {
    public static class KindTest {
        private static final ImmutableSet<Kind> GENERIC = ImmutableSet.of(Kind.ANY, Kind.LITERAL, Kind.MODIFIER,
                Kind.SKIPPABLE);

        @Test
        public void any() {
            assertTrue(Kind.ANY.isGeneric());
            assertEquals(ImmutableSet.copyOf(Kind.values()), Kind.ANY.getSpecificKinds());
        }

        @Test
        public void isGenericFalse() {
            // check a few manually
            assertFalse(Kind.SYMBOL.isGeneric());
            assertFalse(Kind.IDENTIFIER.isGeneric());
            assertFalse(Kind.CHARACTER_LITERAL.isGeneric());
            assertFalse(Kind.STRING_LITERAL.isGeneric());

            // check the rest
            for (Kind k : Kind.values()) {
                if (!GENERIC.contains(k)) {
                    assertFalse(k.isGeneric());
                }
            }
        }

        @Test
        public void isGenericTrue() {
            for (Kind k : GENERIC) {
                assertTrue(k.isGeneric());
            }
        }

        @Test
        public void literal() {
            assertTrue(Kind.LITERAL.isGeneric());

            assertEquals(ImmutableSet.of(Kind.NUMERICAL_LITERAL, Kind.CHARACTER_LITERAL, Kind.FALSE, Kind.NIL,
                    Kind.STRING_LITERAL, Kind.TRUE), Kind.LITERAL.getSpecificKinds());
        }

        @Test
        public void skippable() {
            assertTrue(Kind.SKIPPABLE.isGeneric());

            assertEquals(ImmutableSet.of(Kind.WHITESPACE), Kind.SKIPPABLE.getSpecificKinds());
        }
    }

}
