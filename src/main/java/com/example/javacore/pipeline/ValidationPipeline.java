package com.example.javacore.pipeline;

import com.example.javacore.config.PipelineConfiguration;
import com.example.javacore.exception.InvalidConfigurationException;
import com.example.javacore.model.ValidationResult;
import com.example.javacore.model.ValidationStatus;
import com.example.javacore.step.CheckBalanceStep;
import com.example.javacore.step.CheckCreditCardStep;
import com.example.javacore.step.ValidateUserInformationStep;
import com.example.javacore.step.ValidationStep;

import java.util.*;

/**
 * Pipeline xu ly validation co the cau hinh thu tu tai runtime bang Java Core.
 */
public class ValidationPipeline {

    private final Object writeLock = new Object();
    private volatile PipelineConfiguration currentConfig;
    private volatile boolean requireAllSteps;

    public ValidationPipeline() {
        this(false);
    }

    public ValidationPipeline(boolean requireAllSteps) {
        this.requireAllSteps = requireAllSteps;
        this.currentConfig = new PipelineConfiguration(Collections.emptyMap(), Collections.emptyList());
    }

    /**
     * Khoi tao pipeline mac dinh voi 3 buoc:
     * validateUserInformation -> checkCreditCard -> checkBalance
     */
    public static ValidationPipeline createDefault() {
        ValidationPipeline pipeline = new ValidationPipeline();
        pipeline.registerStep(new ValidateUserInformationStep());
        pipeline.registerStep(new CheckCreditCardStep());
        pipeline.registerStep(new CheckBalanceStep());
        pipeline.reorder(List.of(
                ValidateUserInformationStep.NAME,
                CheckCreditCardStep.NAME,
                CheckBalanceStep.NAME
        ));
        return pipeline;
    }

    /**
     * Thuc thi tuan tu cac buoc trong pipeline theo co che Fail-Fast (Requirements R1, R3, R5).
     *
     * Dac tinh:
     * - Hoan toan khong dung khoa (Lock-free), thong luong doc toi da.
     * - Dat Snapshot Isolation: Dung ban chup bat bien tai thoi diem bat dau goi phuong thuc.
     * - Dung ngay lap tuc khi gap buoc dau tien tra ve FAIL, khong chay cac buoc tiep theo.
     *
     * @return ValidationResult chua ket qua tong the, so buoc da chay va ten buoc that bai (neu co).
     */
    public ValidationResult execute() {
        // Chup snapshot bat bien cua cau hinh hien tai vao stack cuc bo
        PipelineConfiguration snapshot = this.currentConfig;
        List<ValidationStep> stepsToExecute = snapshot.getResolvedSteps();

        if (stepsToExecute.isEmpty()) {
            return ValidationResult.fail(null, 0, Collections.emptyList(), "Pipeline has no configured steps to execute");
        }

        List<String> executedStepNames = new ArrayList<>(stepsToExecute.size());

        for (ValidationStep step : stepsToExecute) {
            executedStepNames.add(step.getName());

            ValidationStatus status;
            try {
                status = step.execute();
            } catch (Exception ex) {
                // Tach biet loi ky thuat voi ket qua nghiep vu binh thuong
                return ValidationResult.fail(
                        step.getName(),
                        executedStepNames.size(),
                        executedStepNames,
                        "Technical exception in step '" + step.getName() + "': " + ex.getMessage()
                );
            }

            // Co che Fail-Fast (R3): Dung ngay lap tuc khi buoc hien tai FAIL
            if (status == ValidationStatus.FAIL) {
                return ValidationResult.fail(
                        step.getName(),
                        executedStepNames.size(),
                        executedStepNames,
                        "Validation failed at step: " + step.getName()
                );
            }
        }

        // Tat ca cac buoc deu PASS
        return ValidationResult.pass(
                executedStepNames.size(),
                executedStepNames,
                "All " + executedStepNames.size() + " validation steps passed successfully"
        );
    }

    /**
     * Dang ky mot buoc validation moi vao registry (Requirement R4).
     *
     * @param step Buoc validation can dang ky.
     * @throws InvalidConfigurationException neu step null, ten rong hoac trung lap ten da co.
     */
    public void registerStep(ValidationStep step) {
        if (step == null) {
            throw new InvalidConfigurationException("Validation step cannot be null");
        }
        if (step.getName() == null || step.getName().isBlank()) {
            throw new InvalidConfigurationException("Validation step name cannot be null or blank");
        }

        synchronized (writeLock) {
            Map<String, ValidationStep> existingSteps = currentConfig.getRegisteredSteps();
            if (existingSteps.containsKey(step.getName())) {
                throw new InvalidConfigurationException("Duplicate step name: '" + step.getName() + "' is already registered");
            }

            Map<String, ValidationStep> newStepMap = new LinkedHashMap<>(existingSteps);
            newStepMap.put(step.getName(), step);

            // Cong bo snapshot moi, giu nguyen executionOrder hien tai
            this.currentConfig = new PipelineConfiguration(newStepMap, currentConfig.getExecutionOrder());
        }
    }

    /**
     * Thay doi thu tu thuc thi cac buoc tai runtime (Requirement R2).
     *
     * @param newOrder Danh sach ten cac buoc theo thu tu moi.
     * @throws InvalidConfigurationException neu thu tu khong hop le (trung lap, ten la, vi pham policy...).
     */
    public void reorder(List<String> newOrder) {
        if (newOrder == null || newOrder.isEmpty()) {
            throw new InvalidConfigurationException("Execution order cannot be null or empty");
        }

        synchronized (writeLock) {
            Map<String, ValidationStep> registered = currentConfig.getRegisteredSteps();
            Set<String> seen = new HashSet<>();

            for (String stepName : newOrder) {
                if (stepName == null || stepName.isBlank()) {
                    throw new InvalidConfigurationException("Step name in order cannot be null or blank");
                }
                // Kiem tra trung lap trong thu tu yeu cau
                if (!seen.add(stepName)) {
                    throw new InvalidConfigurationException("Duplicate step name in execution order: '" + stepName + "'");
                }
                // Kiem tra buoc chua dang ky
                if (!registered.containsKey(stepName)) {
                    throw new InvalidConfigurationException(
                            "Unknown step name: '" + stepName + "'. Available steps: " + registered.keySet()
                    );
                }
            }

            // Kiem tra chinh sach bat buoc tat ca cac buoc da dang ky phai xuat hien
            if (requireAllSteps && seen.size() < registered.size()) {
                Set<String> missing = new TreeSet<>(registered.keySet());
                missing.removeAll(seen);
                throw new InvalidConfigurationException(
                        "Configuration policy requires all registered steps to appear in order. Missing: " + missing
                );
            }

            // Tao va cong bo cau hinh bat bien moi
            this.currentConfig = new PipelineConfiguration(registered, newOrder);
        }
    }

    /**
     * Lay danh sach ten cac buoc trong thu tu thuc thi hien tai (Ban sao phong ve).
     */
    public List<String> getExecutionOrder() {
        return currentConfig.getExecutionOrder();
    }

    /**
     * Lay tap hop cac buoc da dang ky (Ban sao phong ve).
     */
    public Map<String, ValidationStep> getRegisteredSteps() {
        return currentConfig.getRegisteredSteps();
    }

    /**
     * Cau hinh chinh sach co bat buoc toan bo buoc da dang ky phai co mat trong order hay khong.
     */
    public void setRequireAllSteps(boolean requireAllSteps) {
        synchronized (writeLock) {
            this.requireAllSteps = requireAllSteps;
        }
    }

    public boolean isRequireAllSteps() {
        return requireAllSteps;
    }
}
