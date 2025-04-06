package pcd.ass01.v1;

import pcd.ass01.commmon.BoidsSimulator;
import pcd.ass01.commmon.BoidsView;
import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.Flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsParallelSimulator implements BoidsSimulator {
    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int THREAD_NUMBER = Runtime.getRuntime().availableProcessors() + 1;
    private final MyCyclicBarrier computeVelocityBarrier = new MyCyclicBarrier(THREAD_NUMBER/*,
            () -> System.out.println("Velocity computed!")*/);
    private final MyCyclicBarrier updateVelocityBarrier = new MyCyclicBarrier(THREAD_NUMBER/*,
            () -> System.out.println("Velocity updated!")*/);
    private final MyCyclicBarrier positionBarrier = new MyCyclicBarrier(THREAD_NUMBER,
            this::updateView);
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private long t0 = System.currentTimeMillis();

    private Flag resetFlag;
    private Flag pauseFlag;

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
        List<BoidUpdateWorker> boidUpdateWorkers = new ArrayList<BoidUpdateWorker>();
        int nBoids = model.getNboids();
        model.setNboids(0);
        model.setNboids(nBoids);
        var boids = model.getBoids();
        pauseFlag.reset();
        for (int i = 0; i < THREAD_NUMBER; i++) {
            boidUpdateWorkers.add(new BoidUpdateWorker(THREAD_NUMBER, i, model, computeVelocityBarrier, updateVelocityBarrier, positionBarrier, resetFlag, pauseFlag));
        }
        for (var worker : boidUpdateWorkers) {
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
