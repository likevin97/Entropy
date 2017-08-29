package edu.cornell.gdiac.physics.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.JsonAssetManager;

/**
 * Created by Kevin on 4/23/17.
 */
public class JointObstacle extends CapsuleObstacle{
    public static final int GROW_TOP_LEFT = 1;
    public static final int GROW_UP = 2;
    public static final int GROW_TOP_RIGHT = 3;
    public static final int GROW_LEFT = 4;
    public static final int GROW_RIGHT = 5;
    public static final int GROW_BOTTOM_LEFT = 6;
    public static final int GROW_DOWN = 7;
    public static final int GROW_BOTTOM_RIGHT = 8;

    public int direction;
    private Vector2 rootSize;
    float offset;

    public float[] locs;

    public RootObstacle previous;

    public int type;

    public JointObstacle (float x, float y, float width, float height, int dir, RootObstacle prev, int type ) {
        super(x,y, width, height);
        this.direction = dir;
        this.rootSize = new Vector2(width, height);
        this.offset = (width/4.0f) / (float) Math.sqrt(4.0f);
        this.previous = prev;
        this.type = type;
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("Root part");
    }


    public Vector2 jointOffset(int dir) {


        Vector2 jointoff = new Vector2();


        if (this.direction == GROW_RIGHT) {
            jointoff.set(getWidth() - rootSize.y/2, -rootSize.y/2);
        }
        else if (this.direction == GROW_TOP_RIGHT) {
            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/2);
        }
        else if (this.direction == GROW_UP) {
            jointoff.set(rootSize.y/4, getWidth() - rootSize.y/2);
        }
        else if (this.direction == GROW_TOP_LEFT) {
            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/2);
        }
        else if (this.direction == GROW_LEFT) {
            jointoff.set(-getWidth() + rootSize.y/3, rootSize.y/2);
        }
        else if (this.direction == GROW_BOTTOM_LEFT) {
            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, -getWidth()*0.7071f + 0.7071f*rootSize.y/2);
        }
        else if (this.direction == GROW_DOWN) {
            jointoff.set(-rootSize.y/2 + 0.7071f*rootSize.y/4, -getWidth() + rootSize.y/2);
        }
        else {
            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/2, -getWidth()*0.7071f + 0.7071f*rootSize.y/2);
        }


        return jointoff;


    }

    public void initialize(JsonValue json) {
        setName(json.name());

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing

        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        // Now get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
    }
}
