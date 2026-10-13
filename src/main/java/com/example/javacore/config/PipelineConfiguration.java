package com.example.javacore.config;

import com.example.javacore.step.ValidationStep;

import java.util.*;

/**
 * Dai dien cho cau hinh bat bien (Immutable Configuration) cua pipeline tai mot thoi diem.
 *
 * Chua ca hai thanh phan gan ket chat che:
 * 1. Bang cac buoc da dang ky (Registered Steps Map).
 * 2. Thu tu thuc thi hien tai (Execution Order List & Resolved Steps).
 *
 * Viec gom ca hai thanh phan vao mot doi tuong bat bien giup loai bo hoan toan nguy co
 * Thread doc nhin thay registry moi nhung order cu (hoac nguoc lai - Split-Brain state).
 */
public final class PipelineConfiguration {

    private final Map<String, ValidationStep> registeredSteps;
    private final List<String> executionOrder;
    private final List<ValidationStep> resolvedSteps;

    public PipelineConfiguration(Map<String, ValidationStep> steps, List<String> order) {
        Objects.requireNonNull(steps, "steps map cannot be null");
        Objects.requireNonNull(order, "order list cannot be null");

        // Sao chep phong ve (Defensive Copy) de dam bao tinh bat bien
        Map<String, ValidationStep> stepCopy = new LinkedHashMap<>(steps);
        this.registeredSteps = Collections.unmodifiableMap(stepCopy);

        this.executionOrder = List.copyOf(order);

        // Phan giai truoc (Pre-resolve) danh sach cac buoc thuc thi
        List<ValidationStep> resolved = new ArrayList<>(order.size());
        for (String stepName : order) {
            ValidationStep step = stepCopy.get(stepName);
            if (step == null) {
                throw new IllegalStateException("Step in execution order not found in registry: " + stepName);
            }
            resolved.add(step);
        }
        this.resolvedSteps = List.copyOf(resolved);
    }

    public Map<String, ValidationStep> getRegisteredSteps() {
        return registeredSteps;
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }

    public List<ValidationStep> getResolvedSteps() {
        return resolvedSteps;
    }

    @Override
    public String toString() {
        return "PipelineConfiguration{" +
                "executionOrder=" + executionOrder +
                ", registeredSteps=" + registeredSteps.keySet() +
                '}';
    }
}
