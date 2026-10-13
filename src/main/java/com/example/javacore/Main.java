package com.example.javacore;

import com.example.javacore.model.ValidationResult;
import com.example.javacore.model.ValidationStatus;
import com.example.javacore.pipeline.ValidationPipeline;
import com.example.javacore.step.CheckBalanceStep;
import com.example.javacore.step.CheckBlacklistStep;
import com.example.javacore.step.CheckCreditCardStep;
import com.example.javacore.step.ValidateUserInformationStep;

import java.util.List;

/**
 * Lop thuc thi 5 kich ban chinh (Scenarios 1 -> 5)
 */
public class Main {

    public static void main(String[] args) {
        runScenario1();
        runScenario2();
        runScenario3();
        runScenario4();
        runScenario5();
    }

    /**
     * Kich ban 1: Tat ca cac buoc deu PASS
     * Thu tu mac dinh: validateUserInformation -> checkCreditCard -> checkBalance
     * Ket qua ky vong: PASS, thuc thi du ca 3 buoc.
     */
    private static void runScenario1() {
        System.out.println(">>> [KICH BAN 1] - Tat ca cac buoc deu PASS");
        ValidationPipeline pipeline = ValidationPipeline.createDefault();

        ValidationResult result = pipeline.execute();
        System.out.println("   Thu tu cau hinh: " + pipeline.getExecutionOrder());
        System.out.println("   Ket qua tong the : " + result.getOverallStatus());
        System.out.println("   So buoc da chay  : " + result.getExecutedStepCount());
        System.out.println("   Chi tiet cac buoc: " + result.getExecutedStepNames());
        System.out.println("   Thong bao        : " + result.getMessage());

        assert result.isPass() : "Scenario 1 failed";
        assert result.getExecutedStepCount() == 3 : "Scenario 1 should execute 3 steps";
        System.out.println("   ==> KET QUA: DAT (PASS)\n");
    }

    /**
     * Kich ban 2: Buoc dau tien FAIL (Co che Fail-Fast)
     * Cau hinh validateUserInformation tra ve FAIL.
     * Ky vong: Chi buoc 1 chay, buoc 2 va 3 tuyet doi KHONG chay.
     */
    private static void runScenario2() {
        System.out.println(">>> [KICH BAN 2] - Buoc dau tien FAIL (Co che Fail-Fast)");
        ValidateUserInformationStep step1 = new ValidateUserInformationStep(ValidationStatus.FAIL);
        CheckCreditCardStep step2 = new CheckCreditCardStep(ValidationStatus.PASS);
        CheckBalanceStep step3 = new CheckBalanceStep(ValidationStatus.PASS);

        ValidationPipeline pipeline = new ValidationPipeline();
        pipeline.registerStep(step1);
        pipeline.registerStep(step2);
        pipeline.registerStep(step3);
        pipeline.reorder(List.of(step1.getName(), step2.getName(), step3.getName()));

        ValidationResult result = pipeline.execute();
        System.out.println("   Thu tu cau hinh: " + pipeline.getExecutionOrder());
        System.out.println("   Ket qua tong the : " + result.getOverallStatus());
        System.out.println("   Buoc bi FAIL     : " + result.getFailedStepName());
        System.out.println("   So buoc da chay  : " + result.getExecutedStepCount());
        System.out.println("   Cac buoc da chay : " + result.getExecutedStepNames());

        assert result.isFail() : "Scenario 2 failed";
        assert result.getExecutedStepCount() == 1 : "Only step 1 should execute";
        assert "validateUserInformation".equals(result.getFailedStepName()) : "Failed step name mismatch";
        System.out.println("   ==> KET QUA: DAT (Dung ngay o buoc 1, buoc 2 & 3 khong bi goi)\n");
    }

    /**
     * Kich ban 3: Buoc thu hai FAIL (Dung giua chung)
     * Cau hinh buoc 1 PASS, buoc 2 (checkCreditCard) FAIL.
     * Ky vong: Chay chinh xac 2 buoc, buoc 3 KHONG chay.
     */
    private static void runScenario3() {
        System.out.println(">>> [KICH BAN 3] - Buoc thu hai FAIL (Dung giua chung)");
        ValidateUserInformationStep step1 = new ValidateUserInformationStep(ValidationStatus.PASS);
        CheckCreditCardStep step2 = new CheckCreditCardStep(ValidationStatus.FAIL);
        CheckBalanceStep step3 = new CheckBalanceStep(ValidationStatus.PASS);

        ValidationPipeline pipeline = new ValidationPipeline();
        pipeline.registerStep(step1);
        pipeline.registerStep(step2);
        pipeline.registerStep(step3);
        pipeline.reorder(List.of(step1.getName(), step2.getName(), step3.getName()));

        ValidationResult result = pipeline.execute();
        System.out.println("   Ket qua tong the : " + result.getOverallStatus());
        System.out.println("   Buoc bi FAIL     : " + result.getFailedStepName());
        System.out.println("   So buoc da chay  : " + result.getExecutedStepCount());
        System.out.println("   Cac buoc da chay : " + result.getExecutedStepNames());

        assert result.isFail() : "Scenario 3 failed";
        assert result.getExecutedStepCount() == 2 : "Exactly 2 steps should execute";
        assert "checkCreditCard".equals(result.getFailedStepName()) : "Failed step name mismatch";
        System.out.println("   ==> KET QUA: DAT (Chay dung 2 buoc, buoc 3 khong bi goi)\n");
    }

    /**
     * Kich ban 4: Runtime Reorder (Thay doi thu tu dang chay)
     * Doi thanh: checkBalance -> validateUserInformation -> checkCreditCard
     * Ky vong: Ung dung thuc thi ngay lap tuc theo thu tu moi ma khong can restart.
     */
    private static void runScenario4() {
        System.out.println(">>> [KICH BAN 4] - Runtime Reorder (Thay doi thu tu dang chay)");
        ValidationPipeline pipeline = ValidationPipeline.createDefault();
        System.out.println("   Thu tu ban dau: " + pipeline.getExecutionOrder());

        List<String> newOrder = List.of(
                CheckBalanceStep.NAME,
                ValidateUserInformationStep.NAME,
                CheckCreditCardStep.NAME
        );
        pipeline.reorder(newOrder);
        System.out.println("   Thu tu sau reorder: " + pipeline.getExecutionOrder());

        ValidationResult result = pipeline.execute();
        System.out.println("   Ket qua tong the : " + result.getOverallStatus());
        System.out.println("   Thu tu thuc te chay: " + result.getExecutedStepNames());

        assert result.getExecutedStepNames().equals(newOrder) : "Execution order mismatch";
        System.out.println("   ==> KET QUA: DAT (Ung dung da chuyen sang thu tu moi ngay lap tuc)\n");
    }

    /**
     * Kich ban 5: Dang ky them buoc moi tai runtime (checkBlacklist)
     * Dang ky buoc thu 4 ma khong can sua thuat toan cot loi cua pipeline.
     */
    private static void runScenario5() {
        System.out.println(">>> [KICH BAN 5] - Dang ky them buoc moi tai runtime (checkBlacklist)");
        ValidationPipeline pipeline = ValidationPipeline.createDefault();

        CheckBlacklistStep blacklistStep = new CheckBlacklistStep(ValidationStatus.PASS);
        pipeline.registerStep(blacklistStep);
        System.out.println("   Da dang ky them: " + blacklistStep.getName());
        System.out.println("   Tap hop buoc trong registry: " + pipeline.getRegisteredSteps().keySet());

        List<String> fourStepOrder = List.of(
                CheckBlacklistStep.NAME,
                ValidateUserInformationStep.NAME,
                CheckCreditCardStep.NAME,
                CheckBalanceStep.NAME
        );
        pipeline.reorder(fourStepOrder);

        ValidationResult result = pipeline.execute();
        System.out.println("   Thu tu cau hinh 4 buoc: " + pipeline.getExecutionOrder());
        System.out.println("   Ket qua tong the      : " + result.getOverallStatus());
        System.out.println("   So buoc da chay       : " + result.getExecutedStepCount());
        System.out.println("   Chi tiet cac buoc     : " + result.getExecutedStepNames());

        assert result.getExecutedStepCount() == 4 : "Should execute 4 steps";
        assert result.getExecutedStepNames().get(0).equals("checkBlacklist") : "First step should be checkBlacklist";
        System.out.println("   ==> KET QUA: DAT (Buoc thu 4 da duoc them va chay thanh cong)\n");
    }
}
