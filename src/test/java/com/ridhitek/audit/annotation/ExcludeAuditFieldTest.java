package com.ridhitek.audit.annotation;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class ExcludeAuditFieldTest {

    private static class TestClass {

        @ExcludeAuditField
        private String excludedField;

        private String includedField;
    }

    @Test
    void testAnnotationIsPresent() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("excludedField");
        assertTrue(field.isAnnotationPresent(ExcludeAuditField.class), "ExcludeAuditField annotation should be present");
    }

    @Test
    void testAnnotationIsNotPresentOnOtherFields() throws NoSuchFieldException {
        Field field = TestClass.class.getDeclaredField("includedField");
        assertFalse(field.isAnnotationPresent(ExcludeAuditField.class), "ExcludeAuditField annotation should not be present");
    }
}
