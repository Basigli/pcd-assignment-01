package pcd.ass01.jpf.v1;

import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.BoidsSimulator;
import pcd.ass01.commmon.BoidsView;
import pcd.ass01.commmon.Flag;
import pcd.ass01.v1.MyCyclicBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsParallelSimulator implements BoidsSimulator {
    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int THREAD_NUMBER = Runtime.getRuntime().availableProcessors() + 1;
    private final pcd.ass01.v1.MyCyclicBarrier computeVelocityBarrier = new pcd.ass01.v1.MyCyclicBarrier(THREAD_NUMBER);
    private final pcd.ass01.v1.MyCyclicBarrier updateVelocityBarrier = new pcd.ass01.v1.MyCyclicBarrier(THREAD_NUMBER);
    private final pcd.ass01.v1.MyCyclicBarrier positionBarrier = new MyCyclicBarrier(THREAD_NUMBER,
            this::updateView);
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private long t0 = System.currentTimeMillis();

    private Flag resetFlag;
    private Flag pauseFlag;
    List<pcd.ass01.v1.BoidUpdateWorker> boidUpdateWorkers;

    public BoidsParallelSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();
        this.resetFlag = new Flag();
        this.pauseFlag = new Flag();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public synchronized void notifyStarted() {
        this.boidUpdateWorkers = new ArrayList<pcd.ass01.v1.BoidUpdateWorker>();
        var boids = model.getBoids();
        pauseFlag.reset();
        int boidsNumber = boids.size();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            var boidsPartition = model.getBoids(i * (boidsNumber / THREAD_NUMBER),(boidsNumber / THREAD_NUMBER) * (i + 1));
            this.boidUpdateWorkers.add(new pcd.ass01.v1.BoidUpdateWorker(boidsPartition, model, computeVelocityBarrier, updateVelocityBarrier, positionBarrier, resetFlag, pauseFlag));
        }
        for (var worker : this.boidUpdateWorkers) {
            worker.start();
        }
    }

    public synchronized void notifyStopped() {
        pauseFlag.set();
    }

    public synchronized void notifyResumed() {
        pauseFlag.reset();
    }

    public synchronized void notifyResetted() {
        resetFlag.set();
    }

    @Override
    public synchronized void notifyBoidsChanged() {}

    private void updateView(){}

}
