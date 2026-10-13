package com.example.javacore.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Dai dien cho ket qua thuc thi cua toan bo pipeline (Overall Pipeline Result).
 * Luu y: Khong nham lan giua ket qua cua 1 buoc don le voi ket qua tong the cua pipeline.
 */
public final class ValidationResult {

    private final ValidationStatus overallStatus;
    private final String failedStepName;
    private final int executedStepCount;
    private final List<String> executedStepNames;
    private final String message;

    public ValidationResult(
            ValidationStatus overallStatus,
            String failedStepName,
            int executedStepCount,
            List<String> executedStepNames,
            String message
    ) {
        this.overallStatus = Objects.requireNonNull(overallStatus, "overallStatus cannot be null");
        this.failedStepName = failedStepName;
        this.executedStepCount = executedStepCount;
        this.executedStepNames = (executedStepNames == null) ? Collections.emptyList() : List.copyOf(executedStepNames);
        this.message = message;
    }

    public static ValidationResult pass(int executedStepCount, List<String> executedStepNames, String message) {
        return new ValidationResult(ValidationStatus.PASS, null, executedStepCount, executedStepNames, message);
    }

    public static ValidationResult fail(String failedStepName, int executedStepCount, List<String> executedStepNames, String message) {
        return new ValidationResult(ValidationStatus.FAIL, failedStepName, executedStepCount, executedStepNames, message);
    }

    public ValidationStatus getOverallStatus() {
        return overallStatus;
    }

    public boolean isPass() {
        return overallStatus == ValidationStatus.PASS;
    }

    public boolean isFail() {
        return overallStatus == ValidationStatus.FAIL;
    }

    public String getFailedStepName() {
        return failedStepName;
    }

    public int getExecutedStepCount() {
        return executedStepCount;
    }

    public List<String> getExecutedStepNames() {
        return executedStepNames;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "overallStatus=" + overallStatus +
                ", failedStepName='" + failedStepName + '\'' +
                ", executedStepCount=" + executedStepCount +
                ", executedStepNames=" + executedStepNames +
                ", message='" + message + '\'' +
                '}';
    }
}
