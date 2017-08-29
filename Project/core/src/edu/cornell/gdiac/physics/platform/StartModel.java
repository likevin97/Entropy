package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
//import com.sun.tools.javadoc.Start;
import edu.cornell.gdiac.physics.model.PolygonObstacle;

/**
 * Created by Geehyun on 2017. 4. 9..
 */
public class StartModel extends PolygonObstacle {


    public float[] growable;

    public StartModel() {
        super(new float[]{0,0,1,0,1,1,0,1},0,0);
    }

    public StartModel(float[] growable) {
        super(growable,0,0);
        this.growable = growable;

    }


    public boolean inBounds(PlayerModel avatar) {


        if (avatar.getX() < growable[0] || avatar.getY() > growable[1] || avatar.getX() > growable[2] ||
                avatar.getY() < growable[growable.length - 1]) {
            return false;
        }

        return true;

    }
}
