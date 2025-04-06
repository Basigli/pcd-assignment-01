package pcd.ass01.v1;
import java.util.concurrent.Semaphore;

public class MyCyclicBarrier {
    private final int parties;
    private int count = 0;
    private Semaphore mutex = new Semaphore(1);
    private Semaphore barrier = new Semaphore(0);
    private Semaphore reset = new Semaphore(1);
    private final Runnable barrierAction;

    public MyCyclicBarrier(int parties, Runnable barrierAction) {
        this.parties = parties;
        this.barrierAction = barrierAction;
    }
    public MyCyclicBarrier(int parties) {
        this.parties = parties;
        this.barrierAction = null;
    }

    public void await() throws InterruptedException {
        mutex.acquire();
        count++;
        if (count == parties) {
            reset.acquire();
            if (barrierAction != null) {
                barrierAction.run();
            }
            barrier.release(parties);
            count = 0;
            reset.release();
        }
        mutex.release();
        barrier.acquire();
    }
}
