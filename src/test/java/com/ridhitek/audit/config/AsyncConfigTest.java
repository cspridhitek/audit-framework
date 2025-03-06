package com.ridhitek.audit.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = AsyncConfig.class)
class AsyncConfigTest {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }

    @Test
    void testAsyncMethodExecution(ApplicationContext context) {
        TestAsyncService service = context.getBean(TestAsyncService.class);
        CompletableFuture<String> future = service.asyncMethod();
        assertNotNull(future);
    }

    @Service
    static class TestAsyncService {
        @Async
        public CompletableFuture<String> asyncMethod() {
            return CompletableFuture.completedFuture("Success");
        }
    }
}

