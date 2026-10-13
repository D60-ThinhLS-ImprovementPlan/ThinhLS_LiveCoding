package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

public class CheckBlacklistStep extends AbstractConfigurableStep {

    public static final String NAME = "checkBlacklist";

    public CheckBlacklistStep() {
        super(NAME);
    }

    public CheckBlacklistStep(ValidationStatus status) {
        super(NAME, status);
    }
}
