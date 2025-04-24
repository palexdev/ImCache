package io.github.palexdev.imcache.utils;

@FunctionalInterface
public interface ThrowingTriConsumer<T, U, V> {
    void accept(T t, U u, V v) throws Exception;
}
