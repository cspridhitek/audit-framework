package com.ridhitek.audit.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DigitalSignatureUtilTest {

    private static final String TEST_SECRET_KEY = "test-secret-key";
    private static final String EXPECTED_SIGNATURE = "eZbBh/nI9f2l+SvO4y5AOOkIgYmCxC3JUnekBG9HpiM=";
    private static final String TEST_DATA = "Test audit log data";

    @BeforeEach
    void setUp() {
        System.setProperty("AUDIT_LOG_SECRET_KEY", TEST_SECRET_KEY);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("AUDIT_LOG_SECRET_KEY");
    }

    @Test
    void testSignLog_GeneratesValidSignature() {
        String data = TEST_DATA;
        String signature = DigitalSignatureUtil.signLog(data);

        assertNotNull(signature, "Signature should not be null");
        assertFalse(signature.isEmpty(), "Signature should not be empty");
        assertEquals(EXPECTED_SIGNATURE, signature, "Signature should match expected value");
    }

    @Test
    void testSignLog_DifferentInputsGenerateDifferentSignatures() {
        String data1 = "Log Entry 1";
        String data2 = "Log Entry 2";

        String signature1 = DigitalSignatureUtil.signLog(data1);
        String signature2 = DigitalSignatureUtil.signLog(data2);

        assertNotNull(signature1, "First signature should not be null");
        assertNotNull(signature2, "Second signature should not be null");
        assertNotEquals(signature1, signature2, "Different inputs should generate different signatures");
    }

    @Test
    void testSignLog_SameInputGeneratesSameSignature() {
        String data = "Consistent Log Data";

        String signature1 = DigitalSignatureUtil.signLog(data);
        String signature2 = DigitalSignatureUtil.signLog(data);

        assertNotNull(signature1, "First signature should not be null");
        assertNotNull(signature2, "Second signature should not be null");
        assertEquals(signature1, signature2, "Same input should generate the same signature");
    }

    @Test
    void testSignLog_WithNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            DigitalSignatureUtil.signLog(null);
        });
        assertNotNull(exception, "Exception should be thrown for null input");
    }

    @Test
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Boolean> failed = new AtomicReference<>(false);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // All threads use the same data to ensure signatures match
                    String signature = DigitalSignatureUtil.signLog(TEST_DATA);
                    if (!EXPECTED_SIGNATURE.equals(signature)) {
                        failed.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();
        
        assertFalse(failed.get(), "All signatures from multiple threads should match the expected value");
    }
}

