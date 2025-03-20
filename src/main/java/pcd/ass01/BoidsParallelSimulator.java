package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;

public class BoidsParallelSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    private final int CORES = Runtime.getRuntime().availableProcessors();

    private final CyclicBarrier velocityBarrier = new CyclicBarrier(CORES,
            () -> System.out.println("Velocity updated!"));
    private final CyclicBarrier positionBarrier = new CyclicBarrier(CORES,
            () -> System.out.println("Position updated!"));
    private static final int FRAMERATE = 25; //25
    private int framerate;

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
            boidUpdateWorkers.add(new BoidUpdateWorker(boids.subList(i * (boids.size() / CORES), (boids.size() / CORES) * (i + 1)), model, velocityBarrier, positionBarrier));
        }
        for (var worker : boidUpdateWorkers) {
            worker.attachView(view.isPresent() ? view.get() : null);
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
}
