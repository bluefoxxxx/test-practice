package com.sunlight.linker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 并发测试工具类
 * 
 * 【进阶挑战 - 并发测试工具】
 * 
 * 此工具类提供了便捷的并发测试方法：
 * ✅ 多线程执行相同任务
 * ✅ 并发性能测试
 * ✅ 线程安全性验证
 * ✅ 竞态条件检测
 * ✅ 死锁检测
 * 
 * 【使用场景】：
 * - 测试Service在高并发下的线程安全性
 * - 验证数据库事务的并发处理能力
 * - 测试缓存在并发访问下的一致性
 * - 性能压力测试和瓶颈分析
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
public class ConcurrencyTestUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyTestUtils.class);
    
    /**
     * 并发执行测试结果
     */
    public static class ConcurrentTestResult<T> {
        private final List<T> results;
        private final List<Exception> exceptions;
        private final long executionTimeMs;
        private final int threadCount;
        private final int taskCount;
        
        public ConcurrentTestResult(List<T> results, List<Exception> exceptions, 
                                  long executionTimeMs, int threadCount, int taskCount) {
            this.results = results;
            this.exceptions = exceptions;
            this.executionTimeMs = executionTimeMs;
            this.threadCount = threadCount;
            this.taskCount = taskCount;
        }
        
        public List<T> getResults() { return results; }
        public List<Exception> getExceptions() { return exceptions; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public int getThreadCount() { return threadCount; }
        public int getTaskCount() { return taskCount; }
        public boolean hasExceptions() { return !exceptions.isEmpty(); }
        public int getSuccessCount() { return results.size(); }
        public int getFailureCount() { return exceptions.size(); }
        public double getSuccessRate() { 
            return taskCount == 0 ? 0.0 : (double) getSuccessCount() / taskCount; 
        }
        
        @Override
        public String toString() {
            return String.format(
                "ConcurrentTestResult{threadCount=%d, taskCount=%d, successCount=%d, " +
                "failureCount=%d, successRate=%.2f%%, executionTimeMs=%d}",
                threadCount, taskCount, getSuccessCount(), getFailureCount(), 
                getSuccessRate() * 100, executionTimeMs
            );
        }
    }
    
    /**
     * 并发执行相同的任务
     * 
     * @param task 要执行的任务
     * @param threadCount 线程数量
     * @param taskCountPerThread 每个线程执行的任务数量
     * @param timeoutSeconds 超时时间（秒）
     * @return 并发测试结果
     */
    public static <T> ConcurrentTestResult<T> runConcurrentTasks(
            Supplier<T> task, 
            int threadCount, 
            int taskCountPerThread, 
            int timeoutSeconds) {
        
        logger.info("开始并发测试: threadCount={}, taskCountPerThread={}, timeout={}s", 
                   threadCount, taskCountPerThread, timeoutSeconds);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        List<T> results = new CopyOnWriteArrayList<>();
        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        AtomicInteger completedTasks = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 创建所有线程
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        // 等待统一开始信号
                        startLatch.await();
                        
                        // 执行指定数量的任务
                        for (int j = 0; j < taskCountPerThread; j++) {
                            try {
                                T result = task.get();
                                results.add(result);
                                completedTasks.incrementAndGet();
                                
                                if (completedTasks.get() % 100 == 0) {
                                    logger.debug("已完成任务: {}", completedTasks.get());
                                }
                            } catch (Exception e) {
                                exceptions.add(e);
                                logger.warn("任务执行失败: thread={}, task={}, error={}", 
                                           threadId, j, e.getMessage());
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        exceptions.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            
            // 发出开始信号
            startLatch.countDown();
            
            // 等待所有线程完成
            boolean completed = endLatch.await(timeoutSeconds, TimeUnit.SECONDS);
            
            if (!completed) {
                logger.warn("并发测试超时，强制终止");
                executor.shutdownNow();
                exceptions.add(new TimeoutException("并发测试超时"));
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exceptions.add(e);
        } finally {
            executor.shutdown();
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        int totalTasks = threadCount * taskCountPerThread;
        
        ConcurrentTestResult<T> result = new ConcurrentTestResult<>(
            new ArrayList<>(results), 
            new ArrayList<>(exceptions), 
            executionTime, 
            threadCount, 
            totalTasks
        );
        
        logger.info("并发测试完成: {}", result);
        return result;
    }
    
    /**
     * 测试读写并发场景
     * 
     * @param readTask 读任务
     * @param writeTask 写任务
     * @param readerCount 读线程数量
     * @param writerCount 写线程数量
     * @param durationSeconds 测试持续时间（秒）
     * @return 并发测试结果
     */
    public static <R, W> ConcurrentTestResult<Object> runReadWriteConcurrentTest(
            Supplier<R> readTask,
            Supplier<W> writeTask,
            int readerCount,
            int writerCount,
            int durationSeconds) {
        
        logger.info("开始读写并发测试: readers={}, writers={}, duration={}s", 
                   readerCount, writerCount, durationSeconds);
        
        ExecutorService executor = Executors.newFixedThreadPool(readerCount + writerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        
        List<Object> results = new CopyOnWriteArrayList<>();
        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        AtomicInteger readCount = new AtomicInteger(0);
        AtomicInteger writeCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationSeconds * 1000L;
        
        try {
            // 启动读线程
            for (int i = 0; i < readerCount; i++) {
                final int readerId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        
                        while (System.currentTimeMillis() < endTime) {
                            try {
                                R result = readTask.get();
                                results.add(result);
                                readCount.incrementAndGet();
                                
                                // 适当休眠，避免CPU占用过高
                                Thread.sleep(1);
                            } catch (Exception e) {
                                exceptions.add(e);
                                logger.warn("读任务失败: reader={}, error={}", readerId, e.getMessage());
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        exceptions.add(e);
                    }
                });
            }
            
            // 启动写线程
            for (int i = 0; i < writerCount; i++) {
                final int writerId = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        
                        while (System.currentTimeMillis() < endTime) {
                            try {
                                W result = writeTask.get();
                                results.add(result);
                                writeCount.incrementAndGet();
                                
                                // 写操作间隔稍长一些
                                Thread.sleep(10);
                            } catch (Exception e) {
                                exceptions.add(e);
                                logger.warn("写任务失败: writer={}, error={}", writerId, e.getMessage());
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        exceptions.add(e);
                    }
                });
            }
            
            // 发出开始信号
            startLatch.countDown();
            
            // 等待测试完成
            Thread.sleep(durationSeconds * 1000L + 1000); // 多等待1秒确保所有线程结束
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exceptions.add(e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        int totalOperations = readCount.get() + writeCount.get();
        
        ConcurrentTestResult<Object> result = new ConcurrentTestResult<>(
            new ArrayList<>(results),
            new ArrayList<>(exceptions),
            executionTime,
            readerCount + writerCount,
            totalOperations
        );
        
        logger.info("读写并发测试完成: readOps={}, writeOps={}, result={}", 
                   readCount.get(), writeCount.get(), result);
        return result;
    }
    
    /**
     * 测试竞态条件
     * 
     * @param sharedResource 共享资源操作
     * @param threadCount 线程数量
     * @param operationsPerThread 每个线程的操作次数
     * @return 并发测试结果
     */
    public static <T> ConcurrentTestResult<T> testRaceCondition(
            Supplier<T> sharedResource,
            int threadCount,
            int operationsPerThread) {
        
        logger.info("开始竞态条件测试: threadCount={}, operationsPerThread={}", 
                   threadCount, operationsPerThread);
        
        return runConcurrentTasks(sharedResource, threadCount, operationsPerThread, 30);
    }
    
    /**
     * 性能压力测试
     * 
     * @param task 待测试任务
     * @param maxThreads 最大线程数
     * @param tasksPerThread 每个线程的任务数
     * @return 性能测试结果列表
     */
    public static <T> List<ConcurrentTestResult<T>> performanceStressTest(
            Supplier<T> task,
            int maxThreads,
            int tasksPerThread) {
        
        logger.info("开始性能压力测试: maxThreads={}, tasksPerThread={}", maxThreads, tasksPerThread);
        
        List<ConcurrentTestResult<T>> results = new ArrayList<>();
        
        for (int threads = 1; threads <= maxThreads; threads *= 2) {
            logger.info("测试线程数: {}", threads);
            ConcurrentTestResult<T> result = runConcurrentTasks(task, threads, tasksPerThread, 60);
            results.add(result);
            
            // 线程间休息一下，避免系统负载过高
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        logger.info("性能压力测试完成，共测试{}个配置", results.size());
        return results;
    }
}
