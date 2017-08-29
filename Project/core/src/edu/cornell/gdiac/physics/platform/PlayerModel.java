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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.model.*;
import edu.cornell.gdiac.util.JsonAssetManager;
import sun.management.Sensor;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class PlayerModel extends CapsuleObstacle {
	// Physics constants
	/** The density of the character */
	private static final float DUDE_DENSITY = 0.1f;
	/** The factor to multiply by the input */
	private static final float DUDE_FORCE = 20.0f;
	/** The amount to slow the character down */
	private static final float DUDE_DAMPING = 10.0f;
	/** The dude is a slippery one */
	private static final float DUDE_FRICTION = 0.0f;
	/** The maximum character speed */
	private static final float DUDE_MAXSPEED = 5.0f;
	private static final float FREEFALL_MAXSPEED = 20.0f;
	/** The impulse for the character jump */
	private static final float DUDE_JUMP = 5.5f;
	/** Cooldown (in animation frames) for jumping */
	private static final int JUMP_COOLDOWN = 30;
	/** Cooldown (in animation frames) for shooting */
	private static final int SHOOT_COOLDOWN = 6;
	/** Height of the sensor attached to the player's feet */
	private static final float SENSOR_HEIGHT = 0.5f;
	/** Identifier to allow us to track the sensor in ContactListener */


	protected static final int FLIP_COOLDOWN = 9;

	// This is to fit the image to a tigher hitbox
	/** The amount to shrink the body fixture (vertically) relative to the image */
	private static final float DUDE_VSHRINK = 0.6f;
	/** The amount to shrink the body fixture (horizontally) relative to the image */
	private static final float DUDE_HSHRINK = 0.9f;
	/** The amount to shrink the sensor fixture (horizontally) relative to the image */
	private static final float DUDE_SSHRINK = 1.0f;

	/** The current horizontal movement of the character */
	private float   movement;
	/** Which direction is the character facing */
	protected boolean faceRight;
	/** How long until we can jump again */
	private int jumpCooldown;
	/** Whether we are actively jumping */
	private boolean isJumping;
	/** How long until we can shoot again */
	protected int shootCooldown;
	/** Whether our feet are on the ground */
	private boolean isGrounded;
	/** Whether we are actively shooting */
	private boolean isShooting;
	/** Ground sensor to represent our feet */
	private Fixture sensorFixture;
	private PolygonShape sensorShape;

	public static int energy = 7;
	public static boolean fail = false;

	public static int TOTAL_FINAL_ENERGY = 18;

	protected int flipCooldown;

	public static boolean can_flip = true;


	/** Cache for internal force calculations */
	private Vector2 forceCache = new Vector2();

	private Fixture sensorFixture2;
	private PolygonShape sensorShape2;
	private Fixture sensorFixture3;
	private PolygonShape sensorShape3;
	private Fixture sensorFixture4;
	private PolygonShape sensorShape4;
	private Fixture sensorFixture5;
	private PolygonShape sensorShape5;
	public Fixture sensorFixture6;
	private PolygonShape sensorShape6;
    public Fixture sensorFixture7;
    private PolygonShape sensorShape7;
	public Fixture sensorFixture8;
	private PolygonShape sensorShape8;
	private float angle = getAngle();
	private static final String SENSOR_NAME = "DudeGroundSensor";
	private static final String SENSOR_NAME2 = "DudeLeftSensor";
	private static final String SENSOR_NAME3 = "DudeRightSensor";
	private static final String SENSOR_NAME4 = "DudeFlipSensor";
	private static final String SENSOR_NAME5 = "DudeShrinkSensor";
	private static final String SENSOR_NAME6 = "SecondFlipSensor";
    private static final String SENSOR_NAME7 = "CollisionSensor";
	private static final String SENSOR_NAME8 = "InvertedSensor";

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

	public String getSensorName() { return SENSOR_NAME; }

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

	public boolean isUpsideDown(){
		return (getAngle() > Math.PI && getAngle() < 2*Math.PI);
	}

	public void setAngle(float a) {
		angle = a;
	}

	/**
	 * Returns true if the dude is actively firing.
	 *
	 * @return true if the dude is actively firing.
	 */
	public boolean isShooting() {
		//return isShooting && shootCooldown <= 0;
		return ((((Gdx.input.isKeyPressed(Input.Keys.UP)) || (Gdx.input.isKeyPressed(Input.Keys.LEFT)) ||
				(Gdx.input.isKeyPressed(Input.Keys.DOWN)) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT))))) &&
				shootCooldown <= 0;
	}

	public boolean isFlipping() {
		return flipCooldown > 0;
	}


	int numArrowKeys;

	public boolean isJustShooting() {
		//return isShooting && shootCooldown <= 0;
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) numArrowKeys++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) numArrowKeys++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) numArrowKeys++;
		if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) numArrowKeys++;

		return ((Gdx.input.isKeyJustPressed(Input.Keys.UP)) || (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) ||
				(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) || (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))) &&
				numArrowKeys == 1 && shootCooldown <= 0;

	}



	/**
	 * Sets whether the dude is actively firing.
	 *
	 * @param value whether the dude is actively firing.
	 */
	public void setShooting(boolean value) {
		isShooting = value;
	}

	/**
	 * Returns true if the dude is actively jumping.
	 *
	 * @return true if the dude is actively jumping.
	 */
	public boolean isJumping() {
		return isJumping && isGrounded && jumpCooldown <= 0;
	}

	/**
	 * Sets whether the dude is actively jumping.
	 *
	 * @param value whether the dude is actively jumping.
	 */
	public void setJumping(boolean value) {
		isJumping = value;
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

	public float getFreefallMaxspeed() { return FREEFALL_MAXSPEED; }


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
	public PlayerModel(float width, float height) {
		this(0,0,width,height);
	}

	public PlayerModel() {this(0,0,0,0);}

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
	public PlayerModel(float x, float y, float width, float height) {
		super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
		setDensity(DUDE_DENSITY);
		setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		// Gameplay attributes
		isGrounded = false;
		isShooting = false;
		isJumping = false;
		faceRight = true;

		shootCooldown = 0;
		jumpCooldown = 0;

		flipCooldown = 0;

		setName("dude");

		this.fixture.filter.categoryBits = Constants.PLAYER_ENTITY;
		this.fixture.filter.maskBits = Constants.HAZARD_ENTITY | Constants.PHYSICS_ENTITY;

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
		//Vector2 sensorCenter2 = new Vector2(-getWidth()/2, 0);

		forceCache.set(-getWidth()/2, 0);
		FixtureDef sensorDef2 = new FixtureDef();
		sensorDef2.density = DUDE_DENSITY;
		sensorDef2.isSensor = true;
		sensorShape2 = new PolygonShape();
		//sensorShape2.setAsBox(DUDE_SSHRINK*getWidth()/2, SENSOR_HEIGHT, sensorCenter2, 0.0f);
		sensorShape2.setAsBox(DUDE_SSHRINK*getWidth()/4, SENSOR_HEIGHT, forceCache, 0.0f);

		sensorDef2.shape = sensorShape2;
		sensorFixture2 = body.createFixture(sensorDef2);
		sensorFixture2.setUserData(getSensor2Name());


		//right sensor
		//Vector2 sensorCenter3 = new Vector2(getWidth()/2, 0);

		forceCache.set(getWidth()/2, 0);
		FixtureDef sensorDef3 = new FixtureDef();
		sensorDef3.density = DUDE_DENSITY;
		sensorDef3.isSensor = true;
		sensorShape3 = new PolygonShape();
		//sensorShape3.setAsBox(DUDE_SSHRINK*getWidth()/2, SENSOR_HEIGHT, sensorCenter3, 0.0f);

		sensorShape3.setAsBox(DUDE_SSHRINK*getWidth()/4, SENSOR_HEIGHT, forceCache, 0.0f);

		sensorDef3.shape = sensorShape3;
		sensorFixture3 = body.createFixture(sensorDef3);
		sensorFixture3.setUserData(getSensor3Name());

		//green movement sensor
		forceCache.set(0, 0);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = DUDE_DENSITY;
		sensorDef.isSensor = true;
		sensorShape = new PolygonShape();
		sensorShape.setAsBox(DUDE_SSHRINK*getWidth()/1.3f, SENSOR_HEIGHT*1.5f, forceCache, 0.0f);
		sensorDef.shape = sensorShape;

		sensorFixture = body.createFixture(sensorDef);
		sensorFixture.setUserData(getSensorName());


		//small flip sensor
		forceCache.set(0, 0);
		FixtureDef sensorDef4 = new FixtureDef();
		sensorDef4.density = DUDE_DENSITY;
		sensorDef4.isSensor = true;
		sensorShape4 = new PolygonShape();
		sensorShape4.setAsBox(DUDE_SSHRINK*getWidth()/4.0f, SENSOR_HEIGHT*2.5f, forceCache, 0.0f);
		sensorDef4.shape = sensorShape4;

		sensorFixture4 = body.createFixture(sensorDef4);
		sensorFixture4.setUserData(getSensor4Name());


		//Shrinking sensor
		forceCache.set(0, 0);
		FixtureDef sensorDef5 = new FixtureDef();
		sensorDef5.density = DUDE_DENSITY;
		sensorDef5.isSensor = true;
		sensorShape5 = new PolygonShape();
		sensorShape5.setAsBox(DUDE_SSHRINK*getWidth()/1.3f, SENSOR_HEIGHT*2.5f, forceCache, 0.0f);
		sensorDef5.shape = sensorShape5;

		sensorFixture5 = body.createFixture(sensorDef5);
		sensorFixture5.setUserData(getSensor5Name());

		forceCache.set(0,-2.3f);
		FixtureDef sensorDef6 = new FixtureDef();
		sensorDef6.density = DUDE_DENSITY;
		sensorDef6.isSensor = true;
		sensorShape6 = new PolygonShape();
		sensorShape6.setAsBox(DUDE_SSHRINK*getWidth()/2.5f, SENSOR_HEIGHT/1.0f, forceCache, 0.0f);
		sensorDef6.shape = sensorShape6;

		sensorFixture6 = body.createFixture(sensorDef6);
		sensorFixture6.setUserData(getSensor6Name());

        forceCache.set(0,0);
        FixtureDef sensorDef7 = new FixtureDef();
        sensorDef7.density = DUDE_DENSITY;
        sensorDef7.isSensor = true;
        sensorShape7 = new PolygonShape();
        sensorShape7.setAsBox(DUDE_SSHRINK*getWidth()/1.9f, SENSOR_HEIGHT*1.2f, forceCache, 0.0f);
        sensorDef7.shape = sensorShape7;

        sensorFixture7 = body.createFixture(sensorDef7);
        sensorFixture7.setUserData(getSensor7Name());



		return true;
	}

	public String getSensor2Name() {
		return SENSOR_NAME2;
	}
	public String getSensor3Name() {
		return SENSOR_NAME3;
	}
	public String getSensor4Name() {
		return SENSOR_NAME4;
	}
	public String getSensor5Name() {
		return SENSOR_NAME5;
	}
	public String getSensor6Name() { return SENSOR_NAME6; }
    public String getSensor7Name() { return SENSOR_NAME7; }
	public String getSensor8Name() { return SENSOR_NAME8; }
	private static final float rotationRate = 100.0f;

	Timer timer = new Timer();

	public void rotate(float a, float b) {
		//normalize angles to between 0 and 2PI
		if (a > 2 * Math.PI) a = a % (2 * (float) Math.PI);
		if (a < 0) a = (a + 10 * (float) Math.PI) % (2 * (float) Math.PI);
		if (b > 2 * Math.PI) b = b % (2 * (float) Math.PI);
		if (b < 0) b = (b + 10 * (float) Math.PI) % (2 * (float) Math.PI);

		UpdateAngle ua = new UpdateAngle();
		float delta = (b - a) / rotationRate;
		ua.addAngle(a);
		ua.setDelta(delta);
		ua.setEnd(b);
		try {
			timer.schedule(ua, 0, 1);
		} catch (Exception E) {
		}
	}

	class UpdateAngle extends TimerTask {

		float currAngle;
		float delta;
		float endAngle;
		boolean hasStarted = false;

		public void addAngle(float newAngle) {currAngle = newAngle; }
		public void setDelta(float d) {delta = d; }
		public void setEnd(float e) {endAngle = e; }
		public float getAngle() {return currAngle; }
		public boolean hasStarted() {return hasStarted; }

		public void run() {
			hasStarted = true;

			//take the shorter rotation path
			if (Math.abs(currAngle - endAngle) > Math.PI) {
				if (currAngle > endAngle) endAngle += 2*Math.PI;
				else currAngle += 2*Math.PI;
				delta = (endAngle-currAngle)/rotationRate;
			}
			currAngle += delta;
			setAngle(currAngle);
			checkReset();
		}

		public void checkReset() {
			if(Math.abs(currAngle - endAngle) < 0.05) {
				setAngle(endAngle);
				timer.cancel();
				timer.purge();

				//timer = null;
				timer = new Timer();
			}
		}
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

		if (getMovement() == 0f && isGrounded()) {
			forceCache.set(0,-getDamping()*getVY());
			body.applyForce(forceCache,getPosition(),true);
		}

		// Velocity too high, clamp it
		if (Math.abs(getVX()) >= getMaxSpeed()) {
			setVX(Math.signum(getVX())*getMaxSpeed());
		}
		if (Math.abs(getVY()) >= getMaxSpeed() && isGrounded()) {
			setVY(Math.signum(getVY())*getMaxSpeed());
		}
		if (Math.abs(getVY()) >= getFreefallMaxspeed() && !isGrounded()){
			setVY(Math.signum(getVY())*getFreefallMaxspeed());
		}
		else {
			forceCache.set(getMovement() * (float) Math.cos(getAngle()), getMovement() * (float) Math.sin(getAngle()));
			body.applyForce(forceCache, getPosition(), true);
			//setVX(0);
			//setVY(0);
		}


		// Jump!
		if (isJumping()) {
			forceCache.set(0, DUDE_JUMP);
			body.applyLinearImpulse(forceCache,getPosition(),true);
		}
	}

	public void applyForceNow() {
		body.applyForce(new Vector2(getMovement() * (float) Math.cos(getAngle()),getMovement() * (float) Math.sin(getAngle())),getPosition(),true);
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

		if (this.getX() <= 0) {
			this.setX(0);
		}
		if (this.getX() >= WorldController.DEFAULT_WIDTH/PlatformController.worldX) {
			this.setX(WorldController.DEFAULT_WIDTH/PlatformController.worldX);
		}
		if (this.getY() >= WorldController.DEFAULT_HEIGHT/PlatformController.worldY) {
			this.setY(WorldController.DEFAULT_HEIGHT/PlatformController.worldY);
		}



		if (isJumping()) {
			jumpCooldown = JUMP_COOLDOWN;
		} else {
			jumpCooldown = Math.max(0, jumpCooldown - 1);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			flipCooldown = FLIP_COOLDOWN;
		} else {
			flipCooldown = Math.max(0, flipCooldown - 1);
		}

		if (isShooting() && shootCooldown == 0) {
			shootCooldown = SHOOT_COOLDOWN;
		} else {
			numArrowKeys = 0;
			shootCooldown = Math.max(0, shootCooldown - 1);
		}

		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */

	//more animation code
	private static final int FRAME_COLS_IDLE = 8;
	private static final int FRAME_ROWS_WALK = 1;
	private static final int FRAME_ROWS_IDLE = 1;
	private static final int FRAME_COLS_WALK = 11;
	private static final int FRAME_DIE = 4;




	private Texture walkSheet = new Texture(Gdx.files.internal("walkstrip.png"));

	private TextureRegion[][] walktmp = TextureRegion.split(walkSheet, walkSheet.getWidth() /
			FRAME_COLS_WALK, walkSheet.getHeight() / FRAME_ROWS_WALK);

	private TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS_WALK * FRAME_ROWS_WALK];
	private Animation walkAnimation = new Animation<TextureRegion>(0.08f, walkFrames);



	private Texture idleSheet = new Texture(Gdx.files.internal("IdleStrip.png"));
	private SpriteBatch spriteBatch = new SpriteBatch();

	private TextureRegion[][] idletmp = TextureRegion.split(idleSheet, idleSheet.getWidth() /
			FRAME_COLS_IDLE, idleSheet.getHeight() / FRAME_ROWS_IDLE);

	private TextureRegion[] idleFrames = new TextureRegion[FRAME_COLS_IDLE * FRAME_ROWS_IDLE];
	private Animation idleAnimation = new Animation<TextureRegion>(0.08f, idleFrames);



	private Texture dieSheet = new Texture(Gdx.files.internal("jelly_die_spritesheet.png"));

	private TextureRegion[][] dietmp = TextureRegion.split(dieSheet, dieSheet.getWidth() /
			FRAME_DIE, dieSheet.getHeight());

	private TextureRegion[] dieFrames = new TextureRegion[FRAME_DIE];
	private Animation dieAnimation = new Animation<TextureRegion>(0.1f, dieFrames);

	private Texture flipSheet = new Texture(Gdx.files.internal("flip_sheet.png"));

	private TextureRegion[][] fliptmp = TextureRegion.split(flipSheet, flipSheet.getWidth() /1,
			flipSheet.getHeight() / 1);

	private TextureRegion[] flipFrames = new TextureRegion[1];
	private Animation flipAnimation = new Animation<TextureRegion>(30.0f, flipFrames);

	float stateTime = 0f;



	public static TextureRegion energy_line = JsonAssetManager.getInstance().getEntry("energy_line", TextureRegion.class);
	public static TextureRegion energy_fill = JsonAssetManager.getInstance().getEntry("energy_fill", TextureRegion.class);
	public static TextureRegion energy_fill_back = JsonAssetManager.getInstance().getEntry("energy_fill_back", TextureRegion.class);

	public static TextureRegion powerleaf1 = JsonAssetManager.getInstance().getEntry("powerleaf1", TextureRegion.class);
	public static TextureRegion powerleaf2 = JsonAssetManager.getInstance().getEntry("powerleaf2", TextureRegion.class);
	public static TextureRegion powerleaf3 = JsonAssetManager.getInstance().getEntry("powerleaf3", TextureRegion.class);
	public static TextureRegion powerleaf4 = JsonAssetManager.getInstance().getEntry("powerleaf4", TextureRegion.class);
	public static TextureRegion powerleaf5 = JsonAssetManager.getInstance().getEntry("powerleaf5", TextureRegion.class);

	public static TextureRegion powerfillback = JsonAssetManager.getInstance().getEntry("powerfillback", TextureRegion.class);
	public static TextureRegion powerline = JsonAssetManager.getInstance().getEntry("powerline", TextureRegion.class);


	TextureRegion currentFrame;

	public static boolean firstDeath = false;
	public void draw(GameCanvas canvas) {
		
		int effect = faceRight ? 1: -1;
		canvas.draw(energy_fill_back, canvas.camera.position.x - canvas.getCamWidth()/2 - 10, canvas.camera.position.y + canvas.getCamHeight()/2.75f + 3);


		canvas.draw(energy_fill.getTexture(), canvas.camera.position.x - canvas.getCamWidth()/2 - 10, canvas.camera.position.y + canvas.getCamHeight()/2.75f,
				0, 0, energy_fill.getTexture().getWidth()*energy/TOTAL_FINAL_ENERGY, energy_fill.getTexture().getHeight());
		canvas.draw(energy_line, canvas.camera.position.x - canvas.getCamWidth()/2 - 9, canvas.camera.position.y + canvas.getCamHeight()/2.75f);

		canvas.draw(powerfillback, canvas.camera.position.x - canvas.getCamWidth()/2 + 60, canvas.camera.position.y + 175);
		canvas.draw(powerline, canvas.camera.position.x - canvas.getCamWidth()/2 + 60, canvas.camera.position.y + 175);


		for (int i = 0; i < PlatformController.num_power; i++){
			if (i == 0){
				canvas.draw(powerleaf1, canvas.camera.position.x - canvas.getCamWidth()/2 + 86, canvas.camera.position.y + canvas.getCamHeight()/2.75f - 32);
			}
			if (i == 1){
				canvas.draw(powerleaf3, canvas.camera.position.x - canvas.getCamWidth()/2 + 102, canvas.camera.position.y + canvas.getCamHeight()/2.75f - 20);
			}
			if (i == 2){
				canvas.draw(powerleaf2, canvas.camera.position.x - canvas.getCamWidth()/2 + 69, canvas.camera.position.y + canvas.getCamHeight()/2.75f - 20);
			}
			if (i == 3){
				canvas.draw(powerleaf5, canvas.camera.position.x - canvas.getCamWidth()/2 + 110, canvas.camera.position.y + canvas.getCamHeight()/2.75f - 6);
			}
			if (i == 4){
				canvas.draw(powerleaf4, canvas.camera.position.x - canvas.getCamWidth()/2 + 59, canvas.camera.position.y + canvas.getCamHeight()/2.75f - 8);
			}
		}
		int index = 0;
		if (dieFrames[0] == null){
			for (int i = 0; i < FRAME_DIE; i++) {
				dieFrames[index++] = dietmp[0][i];

			}
		}

		index = 0;
		if (walkFrames[0] == null){
			for (int i = 0; i < FRAME_ROWS_WALK; i++) {
				for (int j = 0; j < FRAME_COLS_WALK; j++) {
					walkFrames[index] = walktmp[i][j];
					index++;
				}
			}
		}


		index = 0;
		if (idleFrames[0] == null){
			for (int i = 0; i < FRAME_ROWS_IDLE; i++) {
				for (int j = 0; j < FRAME_COLS_IDLE; j++) {
					idleFrames[index] = idletmp[i][j];
					index++;
				}
			}
		}

		index = 0;
		if (flipFrames[0] == null){
			for (int i = 0; i < 1; i++) {
				for (int j = 0; j < 1; j++) {
					flipFrames[index] = fliptmp[i][j];
					index++;
				}
			}
		}

		stateTime += Gdx.graphics.getDeltaTime();

		if (isFlipping() && can_flip){
			currentFrame = (TextureRegion)flipAnimation.getKeyFrame(stateTime, false);
			canvas.draw(currentFrame, Color.WHITE, origin.x, origin.y,(getX() + (float)Math.sin(getAngle())*getWidth()/4 - (float)Math.cos(getAngle())*(getWidth()/2 - (faceRight ? 0 : getWidth())) + 2.5f*(float)Math.sin(getAngle()))*drawScale.x,
					(getY() - (float)Math.cos(getAngle())*getWidth()/4 - (float)Math.sin(getAngle())*(getHeight()/2 + 0.3f - (faceRight ? 0 : getWidth() - 0.3f)) - 2.5f*(float)Math.cos(getAngle()))*drawScale.y, getAngle(), effect*.6f * PlatformController.worldX,.6f *PlatformController.worldY);

		}

		else if (fail) {

			if (firstDeath == false){
				stateTime = 0;
				firstDeath = true;
			}


			currentFrame = (TextureRegion)dieAnimation.getKeyFrame(stateTime, false);


			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			//adjust Coda's position based on angle to center it on the body, SUPER COMPLICATED
			canvas.draw(currentFrame, Color.WHITE, origin.x, origin.y,(getX() + (float)Math.sin(getAngle())*getWidth()/4 - (float)Math.cos(getAngle())*(getWidth()/2 - (faceRight ? 0 : getWidth())))*drawScale.x,
					(getY() - (float)Math.cos(getAngle())*getWidth()/4 - (float)Math.sin(getAngle())*(getHeight()/2 + 0.3f - (faceRight ? 0 : getWidth() - 0.3f)))*drawScale.y, getAngle(), effect*.6f * PlatformController.worldX,.6f *PlatformController.worldY);

		}

		else if (getMovement() != 0){



			//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



			currentFrame = (TextureRegion)walkAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			//adjust Coda's position based on angle to center it on the body, SUPER COMPLICATED
			canvas.draw(currentFrame, Color.WHITE, origin.x, origin.y,(getX() + (float)Math.sin(getAngle())*getWidth()/4 - (float)Math.cos(getAngle())*(getWidth()/2 - (faceRight ? 0 : getWidth())) + 0.35f*(float)Math.sin(getAngle()))*drawScale.x,
					(getY() - (float)Math.cos(getAngle())*getWidth()/4 - (float)Math.sin(getAngle())*(getHeight()/2 + 0.3f - (faceRight ? 0 : getWidth() - 0.3f)) - 0.35f*(float)Math.cos(getAngle()))*drawScale.y , getAngle(), effect*.6f* PlatformController.worldX,.6f * PlatformController.worldY);


		}
		else{



			//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



			currentFrame = (TextureRegion)idleAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(currentFrame, Color.WHITE, origin.x, origin.y,(getX() + (float)Math.sin(getAngle())*getWidth()/4 - (float)Math.cos(getAngle())*(getWidth()/2 - (faceRight ? 0 : getWidth())) + 0.35f*(float)Math.sin(getAngle()))*drawScale.x,
					(getY() - (float)Math.cos(getAngle())*getWidth()/4 - (float)Math.sin(getAngle())*(getHeight()/2 + 0.3f - (faceRight ? 0 : getWidth() - 0.3f)) - 0.35f*(float)Math.cos(getAngle()))*drawScale.y, getAngle(), effect * 0.6f * PlatformController.worldX, 0.6f * PlatformController.worldY );


		}

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
		canvas.drawPhysics(sensorShape,Color.GREEN,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
		canvas.drawPhysics(sensorShape4,Color.BLUE,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
		canvas.drawPhysics(sensorShape5,Color.PURPLE,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
		canvas.drawPhysics(sensorShape6, Color.BROWN, getX(), getY(), getAngle(),drawScale.x,drawScale.y);
        canvas.drawPhysics(sensorShape7, Color.SALMON, getX(), getY(), getAngle(),drawScale.x,drawScale.y);
	}

	/**
	 * Initializes the Player Model using a JSON value.
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

		// Get the sensor information
		Vector2 sensorCenter = new Vector2(0, -getHeight()/2);
		float[] sSize = json.get("sensorsize").asFloatArray();
		sensorShape = new PolygonShape();
		sensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);


	}
}