package pcd.ass01.test.v3B;

import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.BoidsSimulator;
import pcd.ass01.commmon.BoidsView;
import pcd.ass01.commmon.Flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsParallelSimulator implements BoidsSimulator {
    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int THREAD_NUMBER = 80; //40
    private final MyCyclicBarrier computeVelocityBarrier = new MyCyclicBarrier(THREAD_NUMBER);
    private final MyCyclicBarrier updateVelocityBarrier = new MyCyclicBarrier(THREAD_NUMBER);
    private final MyCyclicBarrier positionBarrier = new MyCyclicBarrier(THREAD_NUMBER,
            this::updateView);
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private long t0 = System.currentTimeMillis();

    private Flag resetFlag;
    private Flag pauseFlag;
    List<BoidUpdateWorker> boidUpdateWorkers;

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
        this.boidUpdateWorkers = new ArrayList<BoidUpdateWorker>();
        if (resetFlag.isSet()) {
            int nBoids = model.getNboids();
            model.setNboids(0);
            model.setNboids(nBoids);
        }
        var boids = model.getBoids();
        pauseFlag.reset();
        int boidsNumber = boids.size();

        var threads = new ArrayList<Thread>();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            var boidsPartition = model.getBoids(i * (boidsNumber / THREAD_NUMBER),(boidsNumber / THREAD_NUMBER) * (i + 1));
            var worker = new BoidUpdateWorker(boidsPartition, model, computeVelocityBarrier, updateVelocityBarrier, positionBarrier, resetFlag, pauseFlag);
            threads.add(Thread.ofVirtual().unstarted(worker));
            this.boidUpdateWorkers.add(worker);
        }
        for (var thread : threads) {
            thread.start();
        }
        for (var thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
    public synchronized void notifyBoidsChanged() {
        int boidsNumber = model.getNboids();
        int i = 0;
        if (boidUpdateWorkers == null || boidUpdateWorkers.isEmpty())
            return;
        for (var worker : this.boidUpdateWorkers) {
            var boidsPartition = model.getBoids(i * (boidsNumber / THREAD_NUMBER),(boidsNumber / THREAD_NUMBER) * (i + 1));
            worker.setBoidsPartition(boidsPartition);
            i++;
        }
    }

    private void updateView(){
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
        t0 = System.currentTimeMillis();
    }

}
