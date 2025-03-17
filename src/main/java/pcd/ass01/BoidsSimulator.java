package pcd.ass01;

import java.util.ArrayList;
import java.util.Optional;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;

    private static final int FRAMERATE = 25;
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
                var workers = new ArrayList<BoidWorker>();

                for (int i = 0; i < cores; i++) {
                    var worker = new BoidWorker(boids.subList(i * (boids.size() / cores), (boids.size() / cores) * (i + 1) - 1), model);
                    worker.start();
                    workers.add(worker);
                }


                for (var worker : workers) {
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
