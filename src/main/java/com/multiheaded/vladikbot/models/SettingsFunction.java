package com.multiheaded.vladikbot.models;

/**
 * @author Oliver Johnson
 */
@FunctionalInterface
public interface SettingsFunction<T> {
    void set(T t);
}
