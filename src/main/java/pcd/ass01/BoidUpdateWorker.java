package pcd.ass01;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidUpdateWorker extends Thread{
    private int numberOfThreds;
    private int threadIndex;

    private BoidsModel model;
    Optional<BoidsView> view;
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    public BoidUpdateWorker(int numberOfThreads, int threadIndex, BoidsModel model, CyclicBarrier velocityBarrier, CyclicBarrier positionBarrier) {
        super();
        this.numberOfThreds = numberOfThreads;
        this.threadIndex = threadIndex;
        this.model = model;
        this.velocityBarrier = velocityBarrier;
        this.positionBarrier = positionBarrier;
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }



    public void run() {
        boolean firstTime = true;
        while (true) {
            if (model.getIsRunning()) {
                var boidsNumber = model.getNboids();
                var boids = model.getBoids(threadIndex * (boidsNumber / numberOfThreds),(boidsNumber / numberOfThreds) * (threadIndex + 1));
                var t0 = System.currentTimeMillis();

                if (firstTime) {
                    for (var boid : boids)
                        boid.updateVelocity(model);
                    firstTime = false;
                } else {
                    try {
                        positionBarrier.await();
                        for (var boid : boids)
                            boid.updateVelocity(model);
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    velocityBarrier.await();
                    for (var boid : boids)
                        boid.updatePos(model);
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }


                if (view.isPresent()) {
                    view.get().update(framerate);
                    var t1 = System.currentTimeMillis();
                    var dtElapsed = t1 - t0;
                    var frameratePeriod = 1000/FRAMERATE;

                    if (dtElapsed < frameratePeriod) {
                        try {
                            Thread.sleep(frameratePeriod - dtElapsed);
                        } catch (Exception ex) {}
                        framerate = FRAMERATE;
                    } else {
                        framerate = (int) (1000/dtElapsed);
                    }
                }
            }
        }
    }


}
