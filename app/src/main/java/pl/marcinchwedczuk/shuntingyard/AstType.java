package pl.marcinchwedczuk.shuntingyard;

public enum AstType {
    UNARY_PLUS {
        @Override
        int priority() {
            return 15;
        }
    },

    UNARY_MINUS {
        @Override
        int priority() {
            return 15;
        }
    },

    PLUS {
        @Override
        int priority() {
            return 5;
        }
    },

    MINUS {
        @Override
        int priority() {
            return 5;
        }
    },

    MULTIPLY {
        @Override
        int priority() {
            return 10;
        }
    },

    DIVIDE {
        @Override
        int priority() {
            return 10;
        }
    },

    POWER {
        @Override
        int priority() {
            return 20;
        }
    },

    CALL_FUNCTION {
        @Override
        int priority() {
            return 100;
        }
    },

    NUMBER {
        @Override
        int priority() {
            return 1000;
        }
    };

    abstract int priority();
}
