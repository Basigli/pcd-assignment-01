package pcd.ass01.test.v2;

import pcd.ass01.commmon.Boid;
import pcd.ass01.commmon.BoidsModel;

import java.util.concurrent.Callable;

public class ComputeVelocityTask implements Callable<Void> {
    private final Boid boid;
    private final BoidsModel model;

    public ComputeVelocityTask(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }
    @Override
    public Void call() throws Exception {
        this.boid.computeVelocity(this.model);
        return null;
    }
}
