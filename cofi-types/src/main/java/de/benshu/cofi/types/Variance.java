package de.benshu.cofi.types;

public enum Variance {
    BIVARIANT {
        @Override
        public boolean isContravariant() {
            return true;
        }

        @Override
        public boolean isCovariant() {
            return true;
        }

        @Override
        public Variance and(Variance variance) {
            return BIVARIANT;
        }
    },
    CONTRAVARIANT {
        @Override
        public boolean isContravariant() {
            return true;
        }

        @Override
        public boolean isCovariant() {
            return false;
        }

        @Override
        public Variance and(Variance variance) {
            return variance == COVARIANT || variance == BIVARIANT ? BIVARIANT : this;
        }
    },
    COVARIANT {
        @Override
        public boolean isContravariant() {
            return false;
        }

        @Override
        public boolean isCovariant() {
            return true;
        }

        @Override
        public Variance and(Variance variance) {
            return variance == CONTRAVARIANT || variance == BIVARIANT ? BIVARIANT : this;
        }
    },
    INVARIANT {
        @Override
        public boolean isContravariant() {
            return false;
        }

        @Override
        public boolean isCovariant() {
            return false;
        }

        @Override
        public Variance and(Variance variance) {
            return variance;
        }
    };

    public abstract boolean isContravariant();

    public abstract boolean isCovariant();

    public abstract Variance and(Variance variance);
}
