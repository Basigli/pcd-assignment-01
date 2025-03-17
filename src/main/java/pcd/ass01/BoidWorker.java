package pcd.ass01;

import java.util.List;
import java.util.Optional;

public class BoidWorker extends Thread{
    private List<Boid> boids;
    private BoidsModel model;



    public BoidWorker(List<Boid> boids, BoidsModel model) {
        super();
        this.boids = boids;
        this.model = model;
    }


    public void run() {
        for (Boid boid : boids) {
            boid.update(model);
        }
    }


}
