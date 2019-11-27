package com.l1sk1sh.vladikbot.models;

/**
 * @author Oliver Johnson
 */
@FunctionalInterface
@Deprecated
// TODO Remove this interface
public interface LockService {
    void setLocked(Boolean available);
}
