package com.ridhitek.audit.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigitalSignatureUtil {
    private static final Logger logger = Logger.getLogger(DigitalSignatureUtil.class.getName());
    private static final String SECRET_KEY;

    static {
        String secretKey = System.getProperty("AUDIT_LOG_SECRET_KEY", "default-secret-key");
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("System property AUDIT_LOG_SECRET_KEY is not set or empty");
        }
        SECRET_KEY = secretKey;
    }

    private static final ThreadLocal<Mac> threadLocalMac = ThreadLocal.withInitial(() -> createMacInstance());

    private static Mac createMacInstance() {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.severe("Failed to initialize Mac instance: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Mac instance", e);
        }
    }

    public static String signLog(String data) {
        if (data == null) {
            throw new NullPointerException("Input data cannot be null");
        }
        
        try {
            Mac hmacSHA256 = threadLocalMac.get();

            // 🔹 Always reinitialize before use to prevent corruption
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);

            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sign the log entry", e);
            return "SIGNATURE_FAILED";  // Return safe fallback instead of throwing
        }
    }
}
