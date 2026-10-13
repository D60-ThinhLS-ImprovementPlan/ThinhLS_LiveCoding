package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

import java.util.Objects;

/**
 * Lop truu tuong ho tro cau hinh ket qua linh hoat cho muc dich kiem thu va demo.
 * Cho phep thiet lap PASS/FAIL va gan hook thuc thi (dung cho CountDownLatch trong concurrency test).
 */
public abstract class AbstractConfigurableStep implements ValidationStep {

    private final String name;
    private volatile ValidationStatus status = ValidationStatus.PASS;
    private volatile Runnable executionHook;

    protected AbstractConfigurableStep(String name) {
        this.name = Objects.requireNonNull(name, "Step name cannot be null");
    }

    protected AbstractConfigurableStep(String name, ValidationStatus status) {
        this.name = Objects.requireNonNull(name, "Step name cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    @Override
    public String getName() {
        return name;
    }

    public void setStatus(ValidationStatus status) {
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public void setExecutionHook(Runnable executionHook) {
        this.executionHook = executionHook;
    }

    @Override
    public ValidationStatus execute() {
        if (executionHook != null) {
            executionHook.run();
        }
        return status;
    }

    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}
