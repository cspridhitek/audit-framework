package com.example.audit.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DigitalSignatureUtil {
    private static final String SECRET_KEY = "auditLogsRidhitek";

    public static String signLog(String action, String username, String timestamp) {
        try {
            String data = action + username + timestamp;
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            hmacSHA256.init(secretKeySpec);
            byte[] hash = hmacSHA256.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign the log entry", e);
        }
    }
}
