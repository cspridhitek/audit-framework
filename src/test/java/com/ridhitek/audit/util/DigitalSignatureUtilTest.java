package com.ridhitek.audit.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DigitalSignatureUtilTest {

    @Test
    void testSignLog_GeneratesValidSignature() {
        String data = "Test audit log data";
        String signature = DigitalSignatureUtil.signLog(data);

        assertNotNull(signature, "Signature should not be null");
        assertFalse(signature.isEmpty(), "Signature should not be empty");
    }

    @Test
    void testSignLog_DifferentInputsGenerateDifferentSignatures() {
        String data1 = "Log Entry 1";
        String data2 = "Log Entry 2";

        String signature1 = DigitalSignatureUtil.signLog(data1);
        String signature2 = DigitalSignatureUtil.signLog(data2);

        assertNotEquals(signature1, signature2, "Different inputs should generate different signatures");
    }

    @Test
    void testSignLog_SameInputGeneratesSameSignature() {
        String data = "Consistent Log Data";

        String signature1 = DigitalSignatureUtil.signLog(data);
        String signature2 = DigitalSignatureUtil.signLog(data);

        assertEquals(signature1, signature2, "Same input should generate the same signature");
    }
}

