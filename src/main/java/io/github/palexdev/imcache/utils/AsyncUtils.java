package io.github.palexdev.imcache.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/// A utility class for asynchronous operations on virtual threads.
public class AsyncUtils {
    //================================================================================
    // Static Properties
    //================================================================================
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    //================================================================================
    // Constructors
    //================================================================================
    private AsyncUtils() {}

    //================================================================================
    // Static Methods
    //================================================================================
    public static Future<?> runAsync(Runnable runnable) {
        return executor.submit(runnable);
    }

    public static <T> Future<T> runAsync(Callable<T> callable) {
        return executor.submit(callable);
    }
}
