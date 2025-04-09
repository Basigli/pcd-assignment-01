package pcd.ass01.test.v2;

import pcd.ass01.commmon.Boid;
import pcd.ass01.commmon.BoidsModel;

import java.util.concurrent.Callable;

public class UpdatePositionTask implements Callable<Void> {

    private final Boid boid;
    private final BoidsModel model;

    public UpdatePositionTask(Boid boid, BoidsModel model) {
        this.boid = boid;
        this.model = model;
    }

    @Override
    public Void call() throws Exception {
        this.boid.updatePos(this.model);
        return null;
    }
}
