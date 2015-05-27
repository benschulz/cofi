package de.benshu.cofi.types.impl.test.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IterableMatchers {
    public static Matcher<Iterable<?>> empty() {
        return new TypeSafeMatcher<Iterable<?>>() {
            @Override
            protected boolean matchesSafely(Iterable<?> iterable) {
                return !iterable.iterator().hasNext();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("|°|=0");
            }
        };
    }
}
