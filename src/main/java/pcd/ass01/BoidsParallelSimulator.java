package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsParallelSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int CORES = Runtime.getRuntime().availableProcessors();

    private final CyclicBarrier velocityBarrier = new CyclicBarrier(CORES/*,
            () -> System.out.println("Velocity updated!")*/);
    private final CyclicBarrier positionBarrier = new CyclicBarrier(CORES,
            this::updateView);
    private static final int FRAMERATE = 25; //25
    private int framerate;
    private long t0 = System.currentTimeMillis();

    public BoidsParallelSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {

        List<BoidUpdateWorker> boidUpdateWorkers = new ArrayList<BoidUpdateWorker>();
        var boids = model.getBoids();
        for (int i = 0; i < CORES; i++) {
            boidUpdateWorkers.add(new BoidUpdateWorker(CORES, i, model, velocityBarrier, positionBarrier));
        }
        for (var worker : boidUpdateWorkers) {
            worker.start();
        }

        for (var worker : boidUpdateWorkers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
