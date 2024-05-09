package com.l1sk1sh.vladikbot.models.queue;

/**
 * @author Wolfgang Schwendtbauer
 */
@FunctionalInterface
public interface QueueSupplier {
    <T extends Queueable> AbstractQueue<T> apply(AbstractQueue<T> queue);
}