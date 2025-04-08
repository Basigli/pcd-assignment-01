package pcd.ass01.v1;

import pcd.ass01.commmon.Boid;
import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;

import java.util.List;

public class BoidUpdateWorker extends Thread{
    private Flag resetFlag;
    private Flag pauseFlag;
    private BoidsModel model;
    private volatile List<Boid> boidsPartition;

    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    public BoidUpdateWorker(    List<Boid> boidsPartition,
                                BoidsModel model,
                                MyCyclicBarrier computeVelocityBarrier,
                                MyCyclicBarrier updateVelocityBarrier,
                                MyCyclicBarrier positionBarrier,
                                Flag resetFlag,
                                Flag pauseFlag) {
        super();
        this.boidsPartition = boidsPartition;
        this.model = model;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
        this.resetFlag = resetFlag;
        this.pauseFlag = pauseFlag;
    }

    public void setBoidsPartition(List<Boid> boidsPartition) {
        this.boidsPartition = boidsPartition;
    }


    public void run() {
        boolean firstTime = true;
        resetFlag.reset();
        while (!resetFlag.isSet()) {
            if (!this.pauseFlag.isSet()) {
                var boids = this.boidsPartition;
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
