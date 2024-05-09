package com.l1sk1sh.vladikbot.models.queue;

import java.util.HashSet;
import java.util.Set;

/**
 * @param <T>
 * @author John Grosh (jagrosh)
 */
public class FairQueue<T extends Queueable> extends AbstractQueue<T> {
    public FairQueue(AbstractQueue<T> queue) {
        super(queue);
    }

    protected final Set<Long> set = new HashSet<>();

    @Override
    public int add(T item) {
        int lastIndex;
        for (lastIndex = list.size() - 1; lastIndex > -1; lastIndex--) {
            if (list.get(lastIndex).getIdentifier() == item.getIdentifier()) {
                break;
            }
        }
        lastIndex++;
        set.clear();
        for (; lastIndex < list.size(); lastIndex++) {
            if (set.contains(list.get(lastIndex).getIdentifier())) {
                break;
            }
            set.add(list.get(lastIndex).getIdentifier());
        }
        list.add(lastIndex, item);
        return lastIndex;
    }
}
