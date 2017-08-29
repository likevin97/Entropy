package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.physics.model.PolygonObstacle;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.lang.reflect.Field;

/**
 * Created by Geehyun on 2017. 4. 8..
 * A "vine" is essentially a root that players cannot grow or shrink.
 */
public class LeftVineModel extends PolygonObstacle {


    /**
     * Create a new VineModel with degenerate settings
     */
    public LeftVineModel() {
        super(new float[]{0,0,1,0,1,1,0,1},0,0);
    }

    public LeftVineModel(float[] points, float x, float y) { super(points,x,y);}

    /**
     * Initializes the wall via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the wall subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json) {
        setName(json.name());

        // Technically, we should do error checking here.
        // A JSON field might accidentally be missing
        float[] verts = json.get("boundary").asFloatArray();
        initShapes(verts);

        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());

        // Now get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);
    }


    public void draw(GameCanvas canvas) {
        //canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1,1);

            canvas.draw(getTexture(),Color.WHITE,9,4,getX()*drawScale.x,getY()*drawScale.y,getAngle(), 0.55f * PlatformController.worldX,0.55f * PlatformController.worldY);


        //canvas.draw(getTexture(),Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1);

    }
}
