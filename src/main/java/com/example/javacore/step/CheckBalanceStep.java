package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

public class CheckBalanceStep extends AbstractConfigurableStep {

    public static final String NAME = "checkBalance";

    public CheckBalanceStep() {
        super(NAME);
    }

    public CheckBalanceStep(ValidationStatus status) {
        super(NAME, status);
    }
}
