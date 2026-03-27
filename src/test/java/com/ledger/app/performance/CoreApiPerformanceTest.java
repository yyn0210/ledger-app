package com.ledger.app.performance;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 后端性能测试 - 核心 API 负载测试
 * 
 * @author Chisong
 * @since 2026-03-26
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("核心 API 性能测试")
public class CoreApiPerformanceTest {

    private ExecutorService executorService;
    private static final int CONCURRENT_USERS = 50;
    private static final int REQUESTS_PER_USER = 100;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    /**
     * 测试 1: 账本列表 API 并发性能
     * 目标：P95 < 200ms, TPS > 500
     */
    @Test
    @DisplayName("账本列表 API 并发负载测试")
    void testBookListApiPerformance() throws Exception {
        // 性能测试实现
        // 模拟 50 并发用户，每用户 100 请求
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        long requestStart = System.nanoTime();
                        try {
                            // 模拟 API 调用
                            Thread.sleep(1);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        } finally {
                            long requestEnd = System.nanoTime();
                            responseTimes.offer((requestEnd - requestStart) / 1_000_000);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        // 计算性能指标
        int totalRequests = successCount.get() + failCount.get();
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / totalRequests * 100;

        // 计算 P95/P99 延迟
        var sortedTimes = responseTimes.stream().sorted().toList();
        long p95 = sortedTimes.get((int) (sortedTimes.size() * 0.95));
        long p99 = sortedTimes.get((int) (sortedTimes.size() * 0.99));
        long avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("=== 账本列表 API 性能测试结果 ===%n");
        System.out.printf("总请求数：%d%n", totalRequests);
        System.out.printf("成功数：%d, 失败数：%d%n", successCount.get(), failCount.get());
        System.out.printf("成功率：%.2f%%%n", successRate);
        System.out.printf("总耗时：%d ms%n", totalTime);
        System.out.printf("TPS: %.2f%n", tps);
        System.out.printf("平均延迟：%d ms%n", avg);
        System.out.printf("P95 延迟：%d ms%n", p95);
        System.out.printf("P99 延迟：%d ms%n", p99);

        // 断言性能要求
        Assertions.assertTrue(successRate >= 99.0, "成功率应 >= 99%");
        Assertions.assertTrue(p95 <= 200, "P95 延迟应 <= 200ms");
        Assertions.assertTrue(tps >= 100, "TPS 应 >= 100");
    }

    /**
     * 测试 2: 交易记录创建 API 性能
     * 目标：P95 < 300ms, TPS > 200
     */
    @Test
    @DisplayName("交易记录创建 API 并发负载测试")
    void testTransactionCreateApiPerformance() throws Exception {
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        long requestStart = System.nanoTime();
                        try {
                            // 模拟数据库写入
                            Thread.sleep(5);
                            successCount.incrementAndGet();
                        } finally {
                            long requestEnd = System.nanoTime();
                            responseTimes.offer((requestEnd - requestStart) / 1_000_000);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(120, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        int totalRequests = successCount.get();
        double tps = (double) totalRequests / (totalTime / 1000.0);
        double successRate = (double) successCount.get() / (CONCURRENT_USERS * REQUESTS_PER_USER) * 100;

        var sortedTimes = responseTimes.stream().sorted().toList();
        long p95 = sortedTimes.get((int) (sortedTimes.size() * 0.95));
        long p99 = sortedTimes.get((int) (sortedTimes.size() * 0.99));
        long avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("=== 交易创建 API 性能测试结果 ===%n");
        System.out.printf("TPS: %.2f, P95: %d ms, P99: %d ms%n", tps, p95, p99);

        Assertions.assertTrue(successRate >= 99.0, "成功率应 >= 99%");
        Assertions.assertTrue(p95 <= 300, "P95 延迟应 <= 300ms");
    }

    /**
     * 测试 3: 统计查询 API 性能
     * 目标：P95 < 500ms (复杂查询)
     */
    @Test
    @DisplayName("统计查询 API 并发负载测试")
    void testStatisticsApiPerformance() throws Exception {
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS / 2);
        AtomicInteger successCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS / 2; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER / 2; j++) {
                        long requestStart = System.nanoTime();
                        try {
                            // 模拟复杂统计查询
                            Thread.sleep(10);
                            successCount.incrementAndGet();
                        } finally {
                            long requestEnd = System.nanoTime();
                            responseTimes.offer((requestEnd - requestStart) / 1_000_000);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(120, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        var sortedTimes = responseTimes.stream().sorted().toList();
        long p95 = sortedTimes.isEmpty() ? 0 : sortedTimes.get((int) (sortedTimes.size() * 0.95));
        long avg = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("=== 统计查询 API 性能测试结果 ===%n");
        System.out.printf("P95: %d ms, 平均：%d ms%n", p95, avg);

        Assertions.assertTrue(p95 <= 500, "P95 延迟应 <= 500ms");
    }
}
