package de.benshu.cofi.types.impl.interpreters;

import com.google.common.collect.ImmutableList;
import de.benshu.cofi.cofic.notes.CofiNote;
import de.benshu.cofi.types.impl.TypeMixin;
import de.benshu.cofi.types.impl.TypeSystemContext;
import de.benshu.cofi.types.impl.declarations.Interpreter;
import de.benshu.cofi.types.impl.declarations.source.SourceType;
import de.benshu.cofi.types.impl.lists.AbstractTypeList;
import de.benshu.cofi.cofic.notes.ImmutableNote;
import de.benshu.cofi.cofic.notes.async.Checker;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.collect.Maps.immutableEntry;
import static de.benshu.commons.core.streams.Collectors.map;

public class HierarchyInterpreter<X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> implements Interpreter<ImmutableList<SourceType<X>>, AbstractTypeList<X, T>> {
    public static <X extends TypeSystemContext<X>, T extends TypeMixin<X, ?>> HierarchyInterpreter<X, T> of(Predicate<? super TypeMixin<X, ?>> sortChecker, Function<? super TypeMixin<X, ?>, ? extends T> typer) {
        return new HierarchyInterpreter<>(sortChecker, typer);
    }

    private final Predicate<? super TypeMixin<X, ?>> sortChecker;
    private final Function<? super TypeMixin<X, ?>, ? extends T> typer;

    public HierarchyInterpreter(Predicate<? super TypeMixin<X, ?>> sortChecker, Function<? super TypeMixin<X, ?>, ? extends T> typer) {
        this.sortChecker = sortChecker;
        this.typer = typer;
    }

    @Override
    public AbstractTypeList<X, T> interpret(ImmutableList<SourceType<X>> input, Checker checker) {
        checker.submit(() -> input.stream().filter(st -> !sortChecker.test(st.getType()))
                .map(st -> immutableEntry(st.getSource(), ImmutableNote.create(CofiNote.INVALID_TYPE_SORT, "Invalid type sort.")))
                .collect(map()));

        return input.stream().map(SourceType::getType).filter(sortChecker).map(typer).collect(AbstractTypeList.typeList());
    }
}
