package com.example.javacore.step;

import com.example.javacore.model.ValidationStatus;

/**
 * Interface dai dien cho mot buoc validation don le trong pipeline.
 * Ap dung Strategy Pattern va Open/Closed Principle.
 */
public interface ValidationStep {

    /**
     * Ten dinh danh duy nhat cua buoc validation (vi du: "validateUserInformation").
     *
     * @return ten buoc validation
     */
    String getName();

    /**
     * Thuc thi logic kiem tra nghiep vu cua buoc nay.
     *
     * @return PASS hoac FAIL
     */
    ValidationStatus execute();
}
