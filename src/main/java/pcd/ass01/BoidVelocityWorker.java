package pcd.ass01;

import java.util.List;

public class BoidVelocityWorker extends Thread{
    private List<Boid> boids;
    private BoidsModel model;



    public BoidVelocityWorker(List<Boid> boids, BoidsModel model) {
        super();
        this.boids = boids;
        this.model = model;
    }


    public void run() {
        for (Boid boid : boids) {
            boid.updateVelocity(model);
        }
    }


}
