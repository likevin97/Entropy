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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.model.*;

/**
 * A sensor obstacle representing the end of the level
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.
 */
public class PowerModel extends PolygonObstacle {

    public float[] powerup;


    public PowerModel(float[] powerup) {
        super(powerup,0,0);
        this.powerup = powerup;

    }

    public PowerModel(float[] powerup, float x, float y) {
        super(powerup,x,y);
        this.powerup = powerup;
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

    private static final int FRAME_COLS = 7;
    private static final int FRAME_ROWS_WALK = 1;


    private Texture walkSheet = new Texture(Gdx.files.internal("powerup_float_spritesheet.png"));

    private TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth() /
            FRAME_COLS, walkSheet.getHeight() / FRAME_ROWS_WALK);

    private TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS_WALK];
    private Animation walkAnimation = new Animation<TextureRegion>(0.12f, walkFrames);

    private SpriteBatch spriteBatch = new SpriteBatch();;

    float stateTime = 0f;

    public void draw(GameCanvas canvas) {

        int index = 0;
        for (int i = 0; i < FRAME_ROWS_WALK; i++) {
            for (int j = 0; j < FRAME_COLS; j++) {
                walkFrames[index++] = tmp[i][j];
            }
        }

        //change the frameDuration to affect speed of animation




        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += Gdx.graphics.getDeltaTime();

        TextureRegion currentFrame = (TextureRegion)walkAnimation.getKeyFrame(stateTime, true);

        //adjust Coda's position based on angle to center it on the body, SUPER COMPLICATED
        canvas.draw(currentFrame, Color.WHITE, origin.x + getTexture().getRegionWidth()/4, origin.y + getTexture().getRegionHeight()/8 + 15,
                getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1*PlatformController.worldX,1 * PlatformController.worldY);
        //System.out.println(getX());

    }
}
