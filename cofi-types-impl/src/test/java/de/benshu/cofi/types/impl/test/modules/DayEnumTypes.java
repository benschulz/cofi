package de.benshu.cofi.types.impl.test.modules;

import de.benshu.cofi.types.impl.unions.ConstructedUnionTypeImpl;
import de.benshu.cofi.types.impl.templates.TemplateTypeImpl;
import de.benshu.cofi.types.impl.test.TestContext;
import de.benshu.cofi.types.impl.test.TypeBuilder;

public class DayEnumTypes {
    private final TemplateTypeImpl<TestContext> monday;
    private final TemplateTypeImpl<TestContext> tuesday;
    private final TemplateTypeImpl<TestContext> wednesday;
    private final TemplateTypeImpl<TestContext> thursday;
    private final TemplateTypeImpl<TestContext> friday;
    private final TemplateTypeImpl<TestContext> saturday;
    private final TemplateTypeImpl<TestContext> sunday;

    private final ConstructedUnionTypeImpl<TestContext> workday;
    private final ConstructedUnionTypeImpl<TestContext> weekday;

    public DayEnumTypes(TypeBuilder typeBuilder) {
        monday = typeBuilder.createTemplateType().called("foo", "bar", "Monday").extendingTop().applyTrivially();
        tuesday = typeBuilder.createTemplateType().called("foo", "bar", "Tuesday").extendingTop().applyTrivially();
        wednesday = typeBuilder.createTemplateType().called("foo", "bar", "Wednesday").extendingTop().applyTrivially();
        thursday = typeBuilder.createTemplateType().called("foo", "bar", "Thursday").extendingTop().applyTrivially();
        friday = typeBuilder.createTemplateType().called("foo", "bar", "Friday").extendingTop().applyTrivially();
        saturday = typeBuilder.createTemplateType().called("foo", "bar", "Saturday").extendingTop().applyTrivially();
        sunday = typeBuilder.createTemplateType().called("foo", "bar", "Sunday").extendingTop().applyTrivially();

        workday = typeBuilder.createUnionType().called("foo", "bar", "Workday")
                .of(monday, tuesday, wednesday, thursday, friday).applyTrivially();
        weekday = typeBuilder.createUnionType().called("foo", "bar", "Workday")
                .of(monday, tuesday, wednesday, thursday, friday, saturday, sunday).applyTrivially();
    }

    public TemplateTypeImpl<TestContext> getMonday() {
        return monday;
    }

    public TemplateTypeImpl<TestContext> getTuesday() {
        return tuesday;
    }

    public TemplateTypeImpl<TestContext> getWednesday() {
        return wednesday;
    }

    public TemplateTypeImpl<TestContext> getThursday() {
        return thursday;
    }

    public TemplateTypeImpl<TestContext> getFriday() {
        return friday;
    }

    public TemplateTypeImpl<TestContext> getSaturday() {
        return saturday;
    }

    public TemplateTypeImpl<TestContext> getSunday() {
        return sunday;
    }

    public ConstructedUnionTypeImpl<TestContext> getWorkday() {
        return workday;
    }

    public ConstructedUnionTypeImpl<TestContext> getWeekday() {
        return weekday;
    }
}
