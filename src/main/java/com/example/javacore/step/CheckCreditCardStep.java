package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

public class CheckCreditCardStep extends AbstractConfigurableStep {

    public static final String NAME = "checkCreditCard";

    public CheckCreditCardStep() {
        super(NAME);
    }

    public CheckCreditCardStep(ValidationStatus status) {
        super(NAME, status);
    }
}
