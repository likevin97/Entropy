package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.model.*;

/**
 * Created by Kevin on 3/2/17.
 */
public class RootModel extends ComplexObstacle {


    public static final float rootWidth = 1.0f;
    public static final float rootHeight = 1.0f;


    protected Vector2 rootSize;
    protected float offset;

    protected RootObstacle tail;



    public RootModel(float x0, float y0) {
        super(x0, y0);
        rootSize = new Vector2(rootWidth, rootHeight);
        offset = 0.0f;
        setName("Roots");
        setBodyType(BodyDef.BodyType.StaticBody);
        tail = null;


    }

    /**
     * Sets the texture for the individual planks
     *
     * @param texture the texture for the individual planks
     */
    public void setTexture(TextureRegion texture) {
        for(Obstacle body : bodies) {
            body.setPosition(body.getX(), body.getY());
            ((SimpleObstacle)body).setTexture(texture);
        }
    }
    protected boolean createJoints(World world) {
        return true;
    }

    public RootObstacle getTail() { return tail;}

    public void setTail(RootObstacle root) {
        tail = root;
    }

    public void add(RootObstacle root) {
        bodies.add(root);
    }

}
