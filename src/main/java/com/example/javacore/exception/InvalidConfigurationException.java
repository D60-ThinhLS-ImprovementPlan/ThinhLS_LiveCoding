package com.example.javacore.exception;

/**
 * Ngoai le nem ra khi cau hinh pipeline khong hop le (buoc khong ton tai, trung lap, rong...).
 * Dam bao tinh nguyen ven: Khi cau hinh sai bi tu choi, cau hinh hop le truoc do van duoc giu nguyen.
 */
public class InvalidConfigurationException extends RuntimeException {

    public InvalidConfigurationException(String message) {
        super(message);
    }
}
