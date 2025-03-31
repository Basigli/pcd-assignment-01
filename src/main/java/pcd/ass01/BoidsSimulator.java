package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25; //25
    private int framerate;
    
    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() {
    	while (true) {
            if (model.getIsRunning()) {
                var t0 = System.currentTimeMillis();
                var boids = model.getBoids();


                int cores = Runtime.getRuntime().availableProcessors();
                //int cores = 1;
                var velocityWorkers = new ArrayList<BoidVelocityWorker>();
                var positionWorkers = new ArrayList<BoidPositionWorker>();

                List<List<Boid>> partitions = new ArrayList<>();

                for (int i = 0; i < cores; i++) {
                    partitions.add(boids.subList(i * (boids.size() / cores), (boids.size() / cores) * (i + 1)));
                }

                for (List<Boid> partition : partitions) {
                    var velocityWorker = new BoidVelocityWorker(partition, model);
                    velocityWorker.start();
                    velocityWorkers.add(velocityWorker);
                }
                for (var worker : velocityWorkers) {
                    try {
                        worker.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                for (List<Boid> partition : partitions) {
                    var positionWorker = new BoidPositionWorker(partition, model);
                    positionWorker.start();
                    positionWorkers.add(positionWorker);
                }
                for (var worker : positionWorkers) {
                    try {
                        worker.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


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
            }
    	}
    }
}
