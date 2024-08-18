package io.github.palexdev.imcache.utils;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalWrapper<T> {
    //================================================================================
    // Properties
    //================================================================================
    private final Optional<T> optional;

    //================================================================================
    // Constructors
    //================================================================================
    public OptionalWrapper(Optional<T> optional) {this.optional = optional;}

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

    public Optional<T> optional() {
        return optional;
    }
}
