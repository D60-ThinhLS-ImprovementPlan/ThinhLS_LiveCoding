package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

public class ValidateUserInformationStep extends AbstractConfigurableStep {

    public static final String NAME = "validateUserInformation";

    public ValidateUserInformationStep() {
        super(NAME);
    }

    public ValidateUserInformationStep(ValidationStatus status) {
        super(NAME, status);
    }
}
