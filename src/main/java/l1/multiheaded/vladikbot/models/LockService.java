package l1.multiheaded.vladikbot.models;

/**
 * @author Oliver Johnson
 */
@FunctionalInterface
public interface LockService {
    void setLocked(Boolean available);
}
