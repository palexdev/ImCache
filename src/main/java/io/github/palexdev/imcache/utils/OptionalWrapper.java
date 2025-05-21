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
