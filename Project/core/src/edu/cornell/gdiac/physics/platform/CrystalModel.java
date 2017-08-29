/*
 * ExitModel.java
 *
 * This is a refactored version of the exit door from Lab 4.  We have made it a specialized
 * class so that we can import its properties from a JSON file.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.physics.model.PolygonObstacle;
import edu.cornell.gdiac.util.JsonAssetManager;

/**
 * A sensor obstacle representing the end of the level
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class CrystalModel extends PolygonObstacle {


    public float dScale;
    /**
     * Create a new ExitModel with degenerate settings
     */
    public CrystalModel(float[] exit_coor, float x, float y) {
        super(exit_coor,x, y);
        setSensor(true);

        dScale = 1;
    }

    /**
     * Initializes the exit door via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the exit subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json) {
        setName(json.name());
        float[] pos  = json.get("pos").asFloatArray();
        float[] size = json.get("size").asFloatArray();
        setPosition(pos[0],pos[1]);
        setDimension(size[0],size[1]);

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
    public void draw(GameCanvas canvas) {
//        canvas.draw(getTexture(), this.getPosition().x, this.getPosition().y);
        canvas.draw(getTexture(),Color.WHITE, origin.x, origin.y + 10, getX()*drawScale.x, getY()*drawScale.y, 0, dScale * PlatformController.worldX,
                dScale * PlatformController.worldY);

        //System.out.println(getX());
    }
}
