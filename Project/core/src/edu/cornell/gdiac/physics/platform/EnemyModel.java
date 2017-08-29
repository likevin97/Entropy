/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.model.*;

/**
 * Player avatar for the platform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class EnemyModel extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float DUDE_DENSITY = 0.1f;
    /** The factor to multiply by the input */
    private static final float DUDE_FORCE = 40.0f;
    /** The amount to slow the character down */
    private static final float DUDE_DAMPING = 10.0f;
    /** The dude is a slippery one */
    private static final float DUDE_FRICTION = 0.0f;
    /** The maximum character speed */
    private static final float DUDE_MAXSPEED = 5.0f;
    /** The impulse for the character jump */
    private static final float DUDE_JUMP = 5.5f;
    /** Cooldown (in animation frames) for jumping */
    private static final int JUMP_COOLDOWN = 30;
    /** Cooldown (in animation frames) for shooting */
    private static final int SHOOT_COOLDOWN = 2;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float DUDE_VSHRINK = 0.66f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float DUDE_HSHRINK = 1.0f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUDE_SSHRINK = 1.0f;

    /** The current horizontal movement of the character */
    private float   movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    private boolean isGrounded;

    /** Enemy attributes*/
    private String type;
    private float markLeft;
    private float markRight;
    private boolean goingRight;
    private float lastXLocation;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    private Fixture sensorFixture2;
    private PolygonShape sensorShape2;
    private Fixture sensorFixture3;
    private PolygonShape sensorShape3;
    private float angle = getAngle();
    private static final String SENSOR_NAME2 = "EnemyLeftSensor";
    private static final String SENSOR_NAME3 = "EnemyRightSensor";

    public float getLeftMark(){return markLeft;}
    public float getRightMark(){return markRight;}
    public float getLastXLocation(){return lastXLocation;}
    public boolean getGoingRight(){return goingRight;}
    public void setGoingRight(boolean b){goingRight=b;}
    public void setLastXLocation(float f){lastXLocation=f;}
    public void swapDirections(){ goingRight = !goingRight; }
    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float a) {
        angle = a;
    }

    public void setType(String t) {
        type = t;
    }



    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return DUDE_FORCE;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return DUDE_DAMPING;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return DUDE_MAXSPEED;
    }


    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    /**
     * Creates a new dude at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public EnemyModel(float width, float height) {
        this(0,0,width,height,0,0);
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public EnemyModel(float x, float y, float width, float height, float mark1, float mark2) {
        super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(DUDE_DENSITY);
        setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setGravityScale(0);

        // Gameplay attributes
        isGrounded = false;
        goingRight=true;
        markLeft=mark1;
        markRight=mark2;
        setName("enemy");

        this.fixture.filter.categoryBits = Constants.HAZARD_ENTITY;
        this.fixture.filter.maskBits = Constants.HAZARD_ENTITY | Constants.PLAYER_ENTITY | Constants.PHYSICS_ENTITY;

    }

    public float getForceDirection() {
        return goingRight ? 1.0f : -1.0f;
    }


    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        //left sensor

        Vector2 sensorCenter2 = new Vector2(-getWidth()/2, 0);

        forceCache.set(-getWidth()/2, 0);
        FixtureDef sensorDef2 = new FixtureDef();
        sensorDef2.density = DUDE_DENSITY;
        sensorDef2.isSensor = true;
        sensorShape2 = new PolygonShape();
        sensorShape2.setAsBox(DUDE_SSHRINK*getWidth()/100.0f, SENSOR_HEIGHT, sensorCenter2, 0.0f);
        sensorShape2.setAsBox(DUDE_SSHRINK*getWidth()/100.0f, SENSOR_HEIGHT, forceCache, 0.0f);
        sensorDef2.shape = sensorShape2;
        sensorFixture2 = body.createFixture(sensorDef2);
        sensorFixture2.setUserData(getSensor2Name());


        //right sensor
        Vector2 sensorCenter3 = new Vector2(getWidth()/2, 0);
        forceCache.set(-getWidth()/2, 0);
        FixtureDef sensorDef3 = new FixtureDef();
        sensorDef3.density = DUDE_DENSITY;
        sensorDef3.isSensor = true;
        sensorShape3 = new PolygonShape();
        sensorShape3.setAsBox(DUDE_SSHRINK*getWidth()/100.0f, SENSOR_HEIGHT, sensorCenter3, 0.0f);
        sensorShape3.setAsBox(DUDE_SSHRINK*getWidth()/100.0f, SENSOR_HEIGHT, forceCache, 0.0f);
        sensorDef3.shape = sensorShape3;
        sensorFixture3 = body.createFixture(sensorDef3);
        sensorFixture3.setUserData(getSensor3Name());

        return true;
    }

    public String getSensor2Name() {
        return SENSOR_NAME2;
    }
    public String getSensor3Name() {
        return SENSOR_NAME3;
    }

    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        // Don't want to be moving. Damp out player motion
        if (getMovement() == 0f) {
            forceCache.set(-getDamping()*getVX(),0);
            body.applyForce(forceCache,getPosition(),true);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= getMaxSpeed()) {
            setVX(Math.signum(getVX())*getMaxSpeed());
        } else {
            forceCache.set(getMovement()*(float)Math.cos(getAngle()),getMovement()*(float)Math.sin(getAngle()));
            body.applyForce(forceCache,getPosition(),true);
            //setVX(0);
            //setVY(0);
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns
//        if (isJumping()) {
//            jumpCooldown = JUMP_COOLDOWN;
//        } else {
//            jumpCooldown = Math.max(0, jumpCooldown - 1);
//        }
//
//        if (isShooting()) {
//            shootCooldown = SHOOT_COOLDOWN;
//        } else {
//            shootCooldown = Math.max(0, shootCooldown - 1);
//        }


        if (this.getX() < 0) {
            this.setX(0);
        }
        if (this.getX() > WorldController.DEFAULT_WIDTH/PlatformController.worldX) {
            this.setX(WorldController.DEFAULT_WIDTH/PlatformController.worldX);
        }
        if (this.getY() < 0) {
            this.setY(0);
            this.setVY(this.getVY() * -1);
        }
        if (this.getY() > WorldController.DEFAULT_HEIGHT/PlatformController.worldY) {
            this.setY(WorldController.DEFAULT_HEIGHT/PlatformController.worldY);
            this.setVY(this.getVY() * -1);
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */

    //more animation code
    private static final int FRAME_COLS = 11;
    private static final int FRAME_ROWS_WALK = 1;


    private Texture walkSheet = new Texture(Gdx.files.internal("fungus_ray_small.png"));

    private TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth() /
            FRAME_COLS, walkSheet.getHeight() / FRAME_ROWS_WALK);

    private TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS_WALK];
    private Animation walkAnimation = new Animation<TextureRegion>(0.08f, walkFrames);

    private SpriteBatch spriteBatch = new SpriteBatch();

    float stateTime = 0f;

    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;



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
        canvas.draw(currentFrame, Color.WHITE, origin.x, origin.y + 15,(getX() + (float)Math.sin(getAngle())*getWidth()/4 - (float)Math.cos(getAngle())*(getWidth()/2 - (faceRight ? 0 : getWidth())))*drawScale.x,
                (getY() - (float)Math.cos(getAngle())*getWidth()/4 - (float)Math.sin(getAngle())*(getHeight()/2 + 0.3f - (faceRight ? 0 : getWidth() - 0.3f)))*drawScale.y, getAngle(), effect*0.6f*PlatformController.worldX,0.6f*PlatformController.worldY);

    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape2,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(sensorShape3,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}