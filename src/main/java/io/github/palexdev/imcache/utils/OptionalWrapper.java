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

import java.util.Optional;
import java.util.function.Consumer;

/// A wrapper class for [Optional] to adapt [Optional#ifPresent(Consumer)] and [Optional#ifPresentOrElse(Consumer, Runnable)]
/// to fluent API.
public record OptionalWrapper<T>(Optional<T> optional) {

    //================================================================================
    // Static Methods
    //================================================================================
    public static <T> OptionalWrapper<T> of(T val) {
        return wrap(Optional.of(val));
    }

    public static <T> OptionalWrapper<T> ofNullable(T val) {
        return wrap(Optional.ofNullable(val));
    }

    public static <T> OptionalWrapper<T> wrap(Optional<T> optional) {
        return new OptionalWrapper<>(optional);
    }

    //================================================================================
    // Methods
    //================================================================================
    public Optional<T> ifPresent(Consumer<T> consumer) {
        optional.ifPresent(consumer);
        return optional;
    }

    public Optional<T> ifPresentOrElse(Consumer<T> consumer, Runnable emptyAction) {
        optional.ifPresentOrElse(consumer, emptyAction);
        return optional;
    }
}
