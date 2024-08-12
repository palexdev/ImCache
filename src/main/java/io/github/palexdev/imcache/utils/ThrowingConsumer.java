package io.github.palexdev.imcache.utils;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
