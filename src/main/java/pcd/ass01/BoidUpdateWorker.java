package pcd.ass01;

import pcd.ass01.model.BoidsModel;
import pcd.ass01.model.Flag;

public class BoidUpdateWorker extends Thread{
    private int numberOfThreds;
    private int threadIndex;
    private Flag resetFlag;
    private Flag pauseFlag;
    private BoidsModel model;

    private final MyCyclicBarrier velocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    public BoidUpdateWorker(int numberOfThreads,
                                int threadIndex,
                                BoidsModel model,
                                MyCyclicBarrier velocityBarrier,
                                MyCyclicBarrier positionBarrier,
                                Flag resetFlag,
                                Flag pauseFlag) {
        super();
        this.numberOfThreds = numberOfThreads;
        this.threadIndex = threadIndex;
        this.model = model;
        this.velocityBarrier = velocityBarrier;
        this.positionBarrier = positionBarrier;
        this.resetFlag = resetFlag;
        this.pauseFlag = pauseFlag;
    }


    public void run() {
        boolean firstTime = true;
        resetFlag.reset();
        while (!resetFlag.isSet()) {
            if (!this.pauseFlag.isSet()) {
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
