package de.benshu.cofi.types.impl.members;

enum MethodSignatureRelation {
    EQUAL(0),
    SUBSIGNATURE(1),
    SUPERSIGNATURE(-1),
    UNRELATED(0);

    private final int comparatorValue;

    MethodSignatureRelation(int comparatorValue) {
        this.comparatorValue = comparatorValue;
    }

    public MethodSignatureRelation reverse() {
        switch (this) {
            case SUBSIGNATURE:
                return SUPERSIGNATURE;
            case SUPERSIGNATURE:
                return SUBSIGNATURE;
            default:
                return this;
        }
    }

    public int asComparatorValue() {
        return comparatorValue;
    }
}
