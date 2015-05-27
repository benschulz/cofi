package de.benshu.cofi.cofic.notes.async;

import com.google.common.collect.ImmutableMap;
import de.benshu.cofi.cofic.notes.Note;
import de.benshu.cofi.cofic.notes.Source;

public interface Check {
    ImmutableMap<Source.Snippet, ? extends Note> check();
}
