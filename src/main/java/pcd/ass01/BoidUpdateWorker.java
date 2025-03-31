package pcd.ass01;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidUpdateWorker extends Thread{
    private int numberOfThreds;
    private int threadIndex;

    private BoidsModel model;

    private final MyCyclicBarrier velocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    public BoidUpdateWorker(int numberOfThreads, int threadIndex, BoidsModel model, MyCyclicBarrier velocityBarrier, MyCyclicBarrier positionBarrier) {
        super();
        this.numberOfThreds = numberOfThreads;
        this.threadIndex = threadIndex;
        this.model = model;
        this.velocityBarrier = velocityBarrier;
        this.positionBarrier = positionBarrier;
    }


    public void run() {
        boolean firstTime = true;
        while (true) {
            if (model.getIsRunning()) {
                var boidsNumber = model.getNboids();
                var boids = model.getBoids(threadIndex * (boidsNumber / numberOfThreds),(boidsNumber / numberOfThreds) * (threadIndex + 1));

                if (firstTime) {
                    for (var boid : boids)
                        boid.updateVelocity(model);
                    firstTime = false;
                } else {
                    try {
                        positionBarrier.await();
                        for (var boid : boids)
                            boid.updateVelocity(model);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    velocityBarrier.await();
                    for (var boid : boids)
                        boid.updatePos(model);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
