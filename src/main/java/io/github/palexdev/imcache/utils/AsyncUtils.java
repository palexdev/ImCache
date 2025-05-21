/*
 * Copyright (C) 2025 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ImCache (https://github.com/palexdev/imcache)
 *
 * ImCache is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ImCache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ImCache. If not, see <http://www.gnu.org/licenses/>.
 */

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
