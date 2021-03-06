package buls.util.concurrent.research;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Semaphore;

/**
 * Created by Alex on 22.06.2014.
 */
@Deprecated
public class ConcurrentArrayQueueWithSemaphore<E> extends ConcurrentArrayQueueWithStatistic<E> {
    @NotNull
    private final Semaphore setSemaphore;

    public ConcurrentArrayQueueWithSemaphore(int capacity, boolean writeStatistic, int concurrentSets) {
        super(capacity, writeStatistic);
        setSemaphore = new Semaphore(concurrentSets);
    }

    @Override
    protected boolean setElement(E e, long tail, long head) {
        try {
            setSemaphore.acquire();
            return super.setElement(e, tail, head);
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } finally {
            setSemaphore.release();
        }
    }
}
