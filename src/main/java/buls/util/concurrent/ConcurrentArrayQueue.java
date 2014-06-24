package buls.util.concurrent;

import sun.misc.Contended;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Bulgakov Alex on 31.05.2014.
 */
public class ConcurrentArrayQueue<E> extends AbstractConcurrentArrayQueue<E> {

    @Contended
    protected final AtomicLong successSet = new AtomicLong();
    @Contended
    protected final AtomicLong failSet = new AtomicLong();
    @Contended
    protected final AtomicLong failLockedSet = new AtomicLong();
    @Contended
    protected final AtomicLong successGet = new AtomicLong();
    @Contended
    protected final AtomicLong failGet = new AtomicLong();

    @Contended
    protected final AtomicLong failNextTail = new AtomicLong();
    @Contended
    protected final AtomicLong failNextHead = new AtomicLong();

    protected final boolean writeStatistic;

    public ConcurrentArrayQueue(int capacity, boolean writeStatistic) {
        super(capacity);
        this.writeStatistic = writeStatistic;
    }

    @Override
    protected boolean setElement(final E e, final long tail, long head) {
        if (e == null) {
            throw new NullPointerException("e cannot be null");
        }
        long currentTail = tail;
        int capacity = capacity();

        long attempt = 0;
        while (true) {
            if (set(e, tail, currentTail, head, ++attempt)) {

                checkHeadTailConsistency();

                successSet();

                return true;
            } else {

                failSet();

                currentTail = computeTail(currentTail);
                if (checkTailOverflow(currentTail, capacity)) {
                    return false;
                }
            }
        }
        //throw new IllegalStateException("setElement");
    }

    protected final void checkHeadTailConsistency() {
        long h = headSequence.get();
        long t = tailSequence.get();
        assert h <= t;
    }

    protected final boolean checkTailOverflow(long tail, int capacity) {
        long head = getHead();
        long amount = tail - head;
        return amount >= capacity;
    }

    protected long computeTail(long tail) {
        //long currentTail = getTail();
        //if (tail < currentTail) {
        //    tail = currentTail;
        //} else {
            tail++;
        //}
        return tail;
    }

    @Override
    protected E getElement(final long head, long tail) {
        long currentHead = head;
        long attempts = 0;
        while (true) {
            E e;
            if ((e = get(head, currentHead, tail, ++attempts)) != null) {
                checkHeadTailConsistency();
                successGet();
                return e;
            } else {
                failGet();

                currentHead = computeHead(currentHead);
                if (checkHeadOverflow(currentHead)) {
                    return null;
                }
            }
        }
        //throw new IllegalStateException("getElement");
    }

    protected final void failGet() {
        if (writeStatistic) failGet.incrementAndGet();
    }

    protected final void successGet() {
        if (writeStatistic) successGet.incrementAndGet();
    }


    protected final void failSet() {
        if (writeStatistic) failSet.incrementAndGet();
    }

    protected final void successSet() {
        if (writeStatistic) {
            long s = successSet.incrementAndGet();
            long t = getTail();
        }
    }

    protected final boolean checkHeadOverflow(long newHead) {
        long tail = getTail();
        if (newHead >= tail) {
            assert newHead == tail;
            return true;
        }
        return false;
    }

    protected long computeHead(long head) {
//        long currentHead = getHead();
//        if (head < currentHead) {
//            head = currentHead;
//        } else {
            head++;
//        }
        return head;
    }


    @Override
    protected final boolean setNextHead(long oldHead, long currentHead) {
        boolean result = super.setNextHead(oldHead, currentHead);
        if (!result) {
            failNextHead.incrementAndGet();
        }
        return result;
    }

    public void printStatistic(PrintStream printStream) {
        if (writeStatistic) {
            printStream.println("success sets " + successSet.get());
            printStream.println("fail sets " + failSet.get());
            printStream.println("fail locked sets " + failLockedSet.get());
            printStream.println("success gets " + successGet.get());
            printStream.println("fail gets " + failGet.get());

            printStream.println("fail next tail " + failNextTail.get());
            printStream.println("fail next head " + failNextHead.get());
        }
    }

}
