package pcd.ass01.test.v1;

import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;

public class BoidUpdateWorker extends Thread{
    private int numberOfThreds;
    private int threadIndex;
    private Flag resetFlag;
    private Flag pauseFlag;
    private BoidsModel model;
    private final int ITERATIONS = 1000;

    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    public BoidUpdateWorker(int numberOfThreads,
                                int threadIndex,
                                BoidsModel model,
                                MyCyclicBarrier computeVelocityBarrier,
                                MyCyclicBarrier updateVelocityBarrier,
                                MyCyclicBarrier positionBarrier,
                                Flag resetFlag,
                                Flag pauseFlag) {
        super();
        this.numberOfThreds = numberOfThreads;
        this.threadIndex = threadIndex;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
        this.resetFlag = resetFlag;
        this.pauseFlag = pauseFlag;
    }


    public void run() {
        boolean firstTime = true;
        resetFlag.reset();
        int currentIter = 0;
        while (currentIter < ITERATIONS) {
            System.out.println("iteration: " + currentIter);
            currentIter++;
            if (!this.pauseFlag.isSet()) {
                var boidsNumber = model.getNboids();
                var boids = model.getBoids(threadIndex * (boidsNumber / numberOfThreds),(boidsNumber / numberOfThreds) * (threadIndex + 1));

                if (firstTime) {
                    boids.forEach(boid -> boid.computeVelocity(model));
                    firstTime = false;
                } else {
                    try {
                        positionBarrier.await();
                        boids.forEach(boid -> boid.computeVelocity(model));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    computeVelocityBarrier.await();
                    boids.forEach(boid -> boid.updateVelocity(model));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    updateVelocityBarrier.await();
                    boids.forEach(boid -> boid.updatePos(model));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
