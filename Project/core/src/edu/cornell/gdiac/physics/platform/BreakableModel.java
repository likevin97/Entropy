package edu.cornell.gdiac.physics.platform;

import edu.cornell.gdiac.physics.model.PolygonObstacle;

/**
 * Created by Kevin on 4/9/17.
 */
public class BreakableModel extends PolygonObstacle {
    public float[] breakable;

    public BreakableModel() {
        super(new float[]{0,0,1,0,1,1,0,1},0,0);
    }

    public BreakableModel(float[] breakable, float x, float y) {
        super(breakable,x,y);
        this.breakable = breakable;

    }
}
