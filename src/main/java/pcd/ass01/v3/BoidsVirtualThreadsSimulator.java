package pcd.ass01.v3;

import pcd.ass01.commmon.BoidsModel;
import pcd.ass01.commmon.BoidsSimulator;
import pcd.ass01.commmon.BoidsView;
import pcd.ass01.commmon.Flag;
import pcd.ass01.v1.MyCyclicBarrier;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class BoidsVirtualThreadsSimulator implements BoidsSimulator {

    private final BoidsModel model;
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private long t0 = System.currentTimeMillis();
    private Optional<BoidsView> view;
    private List<Thread> workers = new ArrayList<>();
    private final MyCyclicBarrier computeVelocityBarrier;
    private final MyCyclicBarrier updateVelocityBarrier;
    private final MyCyclicBarrier positionBarrier;
    private Flag resetFlag;
    private Flag pauseFlag;

    public BoidsVirtualThreadsSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();
        this.resetFlag = new Flag();
        this.pauseFlag = new Flag();
        this.pauseFlag.set();
        int virtualThreadsNumber = model.getNboids();
        computeVelocityBarrier = new MyCyclicBarrier(virtualThreadsNumber);
        updateVelocityBarrier = new MyCyclicBarrier(virtualThreadsNumber);
        positionBarrier = new MyCyclicBarrier(virtualThreadsNumber, this::updateView);
    }
    @Override
    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    @Override
    public synchronized void notifyStarted() {
        pauseFlag.reset();
    }

    @Override
    public synchronized void notifyStopped() {
        pauseFlag.set();
    }

    @Override
    public synchronized void notifyResumed() {
        pauseFlag.reset();
    }

    @Override
    public synchronized void notifyResetted() {
        resetFlag.set();
    }

    @Override
    public synchronized void notifyBoidsChanged() {
        int virtualThreadsNumber = model.getNboids();
        this.computeVelocityBarrier.setParties(virtualThreadsNumber);
        this.updateVelocityBarrier.setParties(virtualThreadsNumber);
        this.positionBarrier.setParties(virtualThreadsNumber);
        this.createVirtualThreads();
    }
    public void runSimulation() {
        createVirtualThreads();
        while (true) {
            if (resetFlag.isSet()) {
                waitForThreadsToTerminate();
                resetFlag.reset();
                pauseFlag.set();
                int nBoids = model.getNboids();
                this.model.setNboids(0);
                this.model.setNboids(nBoids);
                this.createVirtualThreads();
            }
            /*
            if (!this.pauseFlag.isSet()) {
                this.updateView();
            }
            */
        }
    }

    private void createVirtualThreads() {
        var boids = model.getBoids();
        if(!this.workers.isEmpty()) {
            this.workers = new ArrayList<>();
        }
        boids.forEach(boid -> {
            Thread worker = Thread.ofVirtual().unstarted(new BoidUpdateWorker(model,
                    boid,
                    computeVelocityBarrier,
                    updateVelocityBarrier,
                    positionBarrier,
                    resetFlag,
                    pauseFlag
            ));
            workers.add(worker);
        });
        workers.forEach(Thread::start);
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
    private void waitForThreadsToTerminate() {
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
