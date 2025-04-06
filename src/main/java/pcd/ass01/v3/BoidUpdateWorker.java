package pcd.ass01.v3;

import pcd.ass01.commmon.Boid;
import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;
import pcd.ass01.v1.MyCyclicBarrier;

public class BoidUpdateWorker implements Runnable {


    private final BoidsModel model;
    private final Boid boid;
    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    private final Flag resetFlag;
    private final Flag pauseFlag;

    public BoidUpdateWorker(BoidsModel model, Boid boid,
                            MyCyclicBarrier computeVelocityBarrier,
                            MyCyclicBarrier updateVelocityBarrier,
                            MyCyclicBarrier positionBarrier,
                            Flag resetFlag, Flag pauseFlag) {
        this.model = model;
        this.boid = boid;
        this.computeVelocityBarrier = computeVelocityBarrier;
        this.updateVelocityBarrier = updateVelocityBarrier;
        this.positionBarrier = positionBarrier;
        this.resetFlag = resetFlag;
        this.pauseFlag = pauseFlag;
    }

    @Override
    public void run() {
        boolean firstTime = true;
        resetFlag.reset();
        while (!resetFlag.isSet()) {
            if (!this.pauseFlag.isSet()) {
                if (firstTime) {
                    boid.computeVelocity(model);
                    firstTime = false;
                } else {
                    try {
                        positionBarrier.await();
                        boid.computeVelocity(model);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    computeVelocityBarrier.await();
                    boid.updateVelocity(model);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    updateVelocityBarrier.await();
                    boid.updatePos(model);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
