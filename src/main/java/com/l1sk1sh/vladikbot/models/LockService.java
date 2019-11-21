package com.l1sk1sh.vladikbot.models;

/**
 * @author Oliver Johnson
 */
@FunctionalInterface
public interface LockService {
    void setLocked(Boolean available);
}
