package pcd.ass01.jpf.v1;

import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;
import pcd.ass01.commmon.Boid;
import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;
import pcd.ass01.v1.MyCyclicBarrier;

import java.util.List;

public class BoidUpdateWorker extends Thread{
    private Flag resetFlag;
    private Flag pauseFlag;
    private BoidsModel model;
    private volatile List<Boid> boidsPartition;

    private final pcd.ass01.v1.MyCyclicBarrier computeVelocityBarrier;
    private final pcd.ass01.v1.MyCyclicBarrier updateVelocityBarrier;
    private final pcd.ass01.v1.MyCyclicBarrier positionBarrier;
    public BoidUpdateWorker(    List<Boid> boidsPartition,
                                BoidsModel model,
                                pcd.ass01.v1.MyCyclicBarrier computeVelocityBarrier,
                                pcd.ass01.v1.MyCyclicBarrier updateVelocityBarrier,
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


    public void run() {
        boolean firstTime = true;
        resetFlag.reset();

        while (true) {
            if (true) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    try {
                        positionBarrier.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    computeVelocityBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    updateVelocityBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void log(String msg) {
        System.out.println("[ " + getName()+ " ] " + msg);
    }


}
