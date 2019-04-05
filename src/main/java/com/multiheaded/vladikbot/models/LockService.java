package com.multiheaded.vladikbot.models;

/**
 * @author Oliver Johnson
 */
@FunctionalInterface
public interface LockService {
    void setAvailable(Boolean available);
}
