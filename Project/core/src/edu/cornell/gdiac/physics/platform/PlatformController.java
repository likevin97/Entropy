/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import com.sun.tools.hat.internal.model.Root;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.model.*;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {

	public static TextureRegion retrieveTexture(String name) {
		return JsonAssetManager.getInstance().getEntry(name, TextureRegion.class);
	}

	public static Sound retrieveSound(String name) {
		return JsonAssetManager.getInstance().getEntry(name, Sound.class);
	}

	public void loadSounds() {
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), GROW_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), SHRINK_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), FAIL_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), START_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), BREAK_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), POWERUP_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), WIN_FILE);
		SoundController.getInstance().allocate(JsonAssetManager.getInstance(), MOVE_FILE);
	}

	/** Track asset loading from all instances and subclasses */
	private AssetState platformAssetState = AssetState.EMPTY;

	private int rowSize;
	private int colSize;
	private boolean can_grow = true;

	private PooledList<BreakableModel> breakable;
	private PowerModel powerup;

	private static String DEFAULT_PATH = "json/level.json";
	private String path;
	private JsonReader jsonReader;


	public int shrink_cooldown = 0;

	public static int num_power;
	private RootModel rmStandingOn = null;
	public static float worldX = 32.0f;
	public static float worldY = 18.0f;

	protected boolean gotPowerUp;
	protected boolean usedPowerUp;

	protected boolean mousePressed;
	protected float mouseX;
	protected float mouseY;
	protected float mouseAngle;
	protected int mouseDir;

	protected Vector2 startingpos = new Vector2(0, 0);

	private static final String GROW_FILE = "platform/growing.mp3"; //grow
	private static final String SHRINK_FILE = "platform/shrink.mp3"; //shrink
	private static final String FAIL_FILE = "platform/plop.mp3"; //failure
	private static final String START_FILE = "platform/plop.mp3"; //start or reset level
	private static final String MOVE_FILE = "platform/walking.mp3"; //moving
	private static final String WIN_FILE = "platform/victory.mp3"; //beat level
	private static final String POWERUP_FILE = "platform/jump.mp3"; //got powerup
	private static final String BREAK_FILE = "platform/break.mp3"; //broke through
	private static final String CANTGROW_FILE = "platform/pew.mp3"; //no more energy to grow roots

	// Physics constants for initialization
	/** The new heavier gravity for this world (so it is not so floaty) */
	private static final float  DEFAULT_GRAVITY = -14.7f;
	/** The density for most physics objects */
	private static final float  BASIC_DENSITY = 0.0f;
	/** The density for a bullet */
	private static final float  HEAVY_DENSITY = 10.0f;
	/** Friction of most platforms */
	private static final float  BASIC_FRICTION = 0.4f;
	/** The restitution for all physics objects */
	private static final float  BASIC_RESTITUTION = 0.1f;
	/** The volume for sound effects */
	public static float EFFECT_VOLUME = 0.5f;

	private int level_max_energy;
	private PhysicsController pc;

	// Other game objects



	// Physics objects for the game
	/** Reference to the character avatar */
	public static PlayerModel avatar;

	/** Reference to the goalDoor (for collision detection) */
	private ExitModel goalDoor;

	private boolean something_below = false;

	private EnemyModel e1;

	private LandEnemyModel e2;

	/** The initial position of the enemy */
	private static Vector2 gravityVector = new Vector2(12.5f, 7.5f);


	private PooledList<LandEnemyModel> landEnemies;
	private PooledList<EnemyModel> enemyModels;
	private ObjectMap<Integer,RootModel> roots;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;
	protected ObjectSet<RootObstacle> rootsInContact;


	private Vector2 gravity = new Vector2(0, -14.7f);

	public static final int GROW_TOP_LEFT = 0;
	public static final int GROW_UP = 1;
	public static final int GROW_TOP_RIGHT = 2;
	public static final int GROW_LEFT = 7;
	public static final int GROW_RIGHT = 3;
	public static final int GROW_BOTTOM_LEFT = 6;
	public static final int GROW_DOWN = 5;
	public static final int GROW_BOTTOM_RIGHT = 4;

	private boolean sensor6;

	protected static final int PREGROWN_ROOT = 0;
    protected static final int INGAME_ROOT = 1;


	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY * 2);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		rootsInContact = new ObjectSet<RootObstacle>();
		enemyModels = new PooledList<EnemyModel>();
		landEnemies = new PooledList<LandEnemyModel>();
		this.rowSize = 2;
		this.colSize = 2;
		breakable = new PooledList<BreakableModel>();
		jsonReader = new JsonReader();
		path = DEFAULT_PATH;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset(int num) {

	    InputController.leftInput = Input.Keys.A;
        InputController.rightInput = Input.Keys.D;

		avatar.energy = 7;
		avatar.fail = false;

		PlayerModel.firstDeath = false;


		gotPowerUp = false;
		usedPowerUp = false;
		avatar.can_flip = false;
		can_grow = true;

		num_power = 0;
		rmStandingOn = null;
		if (roots != null) roots.clear();
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		landEnemies.clear();
		enemyModels.clear();

		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel(num);
		loadSounds();
		SoundController.getInstance().play(START_FILE,START_FILE,false,EFFECT_VOLUME);
		something_below = false;
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel(int num) {

        InputController.leftInput = Input.Keys.A;
        InputController.rightInput = Input.Keys.D;

//		if (num > 8){
//			num = 8;
//		}
		float dwidth;
		float dheight;
		dwidth = 2.0f;
		dheight = 2.0f;
		levelNum = num;
		//System.out.println(num);


		//System.out.println("json/level" + Integer.toString(num) + ".json");
		path = "json/level" + Integer.toString(num) + ".json";


		JsonValue level = jsonReader.parse(Gdx.files.internal(path));
		float[] psize = level.get("physicsSize").asFloatArray();
		float[] gsize = level.get("graphicSize").asFloatArray();
		scale.x = gsize[0]/psize[0];
		scale.y = gsize[1]/psize[1];
		worldX = 32/psize[0];
		worldY = 18/psize[1];
		this.bounds = new Rectangle(0,0,psize[0],psize[1]);
		// create exit door
		roots = new ObjectMap<Integer,RootModel>();
		JsonValue exit = level.get("exit");
		float[] verts = exit.get("points").asFloatArray();
		float[] pos = exit.get("pos").asFloatArray();
		goalDoor = new ExitModel(verts,pos[0],pos[1]);

		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setDensity(0.0f);
		goalDoor.setFriction(0.0f);
		goalDoor.setRestitution(0.0f);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);

		goalDoor.setTexture(retrieveTexture("exit"));
		goalDoor.setName("goal");
		addObject(goalDoor);

		initialize("powerup",level);
		initialize("checkpoint",level);
		initialize("platform",level);
		initialize("vine",level);
		initialize("breakable",level);
		initialize("Spike",level);
		initialize("RightSpike",level);
		initialize("LeftSpike",level);


		JsonValue character = level.get("avatar");
		float[] pos3 = character.get("pos").asFloatArray();
		avatar = new PlayerModel(pos3[0], pos3[1], dwidth, dheight);
		avatar.setDrawScale(scale);
		avatar.setTexture(retrieveTexture("dude"));
		avatar.setMass(30.0f);
		addObject(avatar);

		avatar.energy = 100;
		level_max_energy = 100;



		JsonValue enemy = level.get("enemy").child();
		float ewidth = 1.7f;
		float eheight = 1.9f;
		// create enemy

		while (enemy != null) {
			float[] pos1 = enemy.get("pos").asFloatArray();

			if (enemy.get("type").asInt() == 0) {
				EnemyModel em = new EnemyModel(pos1[0],pos1[1],dwidth,dheight, pos1[0]+1, pos1[0]+4);
				em.setDrawScale(scale);
				em.setTexture(retrieveTexture("dude"));
				addObject(em);
				enemyModels.add(em);
				enemy = enemy.next();
			} else {
				LandEnemyModel em = new LandEnemyModel(pos1[0],pos1[1],dwidth,dheight, pos1[0]+1, pos1[0]+4);
				em.setDrawScale(scale);
				em.setTexture(retrieveTexture("dude"));
				addObject(em);
				landEnemies.add(em);
				enemy = enemy.next();
			}

		}

		initroots(level);


		pc = new PhysicsController(world,avatar);
		level_max_energy = 18;
		avatar.energy = level.get("energy").asInt();




//		for (int i = 0; i < 100; i++) {
//			roots.put(new Vector2(avatar.getX(),avatar.getY()), new RootModel(avatar.getX(),avatar.getY()));
//		}
	}

	private void initroots(JsonValue level) {
		JsonValue json = level.get("root").child();
		while (json != null) {
			int[] pos = json.get("pos").asIntArray();
			int x = pos[0];
			int y = pos[1];
			int key = 32*y + x;

			RootModel pregrown = new RootModel(x,y);
			pregrown.setDensity(HEAVY_DENSITY);
			pregrown.setFriction(0.0f);
			pregrown.setRestitution(0.0f);
			pregrown.setSensor(true);
			pregrown.setDrawScale(scale);
			pregrown.setBodyType(BodyDef.BodyType.StaticBody);
			pregrown.setName("Root part");
			roots.put(key, pregrown);

			int dir = json.get("direction").asInt();
			int len = json.get("length").asInt();
			for (int i = 0; i < len; i++) {
				grow(pregrown,dir, PREGROWN_ROOT, x, y);
			}



			pregrown.setTexture(retrieveTexture("rootpiece_l"));

			RootObstacle jointPointer = pregrown.getTail();
			while (jointPointer.previous != null) {
				if (jointPointer.type == RootObstacle.JOINT) {
					jointPointer.setTexture(retrieveTexture("joint"));
				}
				if (jointPointer.half == RootObstacle.LEFT_PIECE) {
					jointPointer.setTexture(retrieveTexture("rootpiece_l"));
				} else if (jointPointer.half == RootObstacle.RIGHT_PIECE){
					jointPointer.setTexture(retrieveTexture("rootpiece_r"));
				}

				jointPointer = jointPointer.previous;
			}
			if (jointPointer.type == RootObstacle.ANCHOR) {
				jointPointer.setTexture(retrieveTexture("rootanchor"));
			}
			if (pregrown.getTail().previous != null) {
				pregrown.getTail().setTexture(retrieveTexture("endroot_r"));
				if (pregrown.getTail().previous != null && pregrown.tail.previous.type != RootObstacle.ANCHOR
						&& pregrown.tail.previous.type != RootObstacle.JOINT) {
					pregrown.getTail().previous.setTexture(retrieveTexture("endroot_l"));
				}
			}

			json = json.next();

		}

	}


	private void initialize(String name, JsonValue level) {

		JsonValue json = level.get(name).child();
		while (json != null) {
			PolygonObstacle obj;
			float[] verts = json.get("points").asFloatArray();
			float[] pos = json.get("pos").asFloatArray();
			if (name.equals("powerup")) {
				PowerModel pow = new PowerModel(verts, pos[0],pos[1]);
				pow.setDensity(BASIC_DENSITY);
				pow.setFriction(BASIC_FRICTION);
				pow.setRestitution(BASIC_RESTITUTION);
				pow.setDrawScale(scale);
				pow.setBodyType(BodyDef.BodyType.StaticBody);
				pow.setTexture(retrieveTexture(json.get("texture").asString()));
				powerup = pow;

				pow.setName(name);
				addObject(pow);
				json = json.next();
			}
			else if (name.equals("breakable")) {
				BreakableModel bre = new BreakableModel(verts, pos[0],pos[1]);
				bre.setDensity(0);
				bre.setFriction(0);
				bre.setRestitution(0);
				bre.setDrawScale(scale);
				bre.setBodyType(BodyDef.BodyType.KinematicBody);
				System.out.println(json.get("texture"));
				bre.setTexture(retrieveTexture(json.get("texture").asString()));
				breakable.add(bre);

				bre.setName(name);
				addObject(bre);
				json = json.next();
			}

			else if (name.equals("vine")){
				if (json.get("texture").asString().equals("LeftVineEnd")){
					LeftVineModel vin = new LeftVineModel(verts, pos[0], pos[1]);
					vin.setDensity(BASIC_DENSITY);
					vin.setFriction(BASIC_FRICTION);
					vin.setRestitution(BASIC_RESTITUTION);
					vin.setDrawScale(scale);
					vin.setBodyType(BodyDef.BodyType.StaticBody);
					vin.setTexture(retrieveTexture(json.get("texture").asString()));
					vin.setName(name);
					addObject((vin));
					json = json.next();

				}
				else if (json.get("texture").asString().equals("LeftVineTaper")){
					LeftVineTaper vin = new LeftVineTaper(verts, pos[0], pos[1]);
					vin.setDensity(BASIC_DENSITY);
					vin.setFriction(BASIC_FRICTION);
					vin.setRestitution(BASIC_RESTITUTION);
					vin.setDrawScale(scale);
					vin.setBodyType(BodyDef.BodyType.StaticBody);
					vin.setTexture(retrieveTexture(json.get("texture").asString()));
					vin.setName(name);
					addObject((vin));
					json = json.next();

				}
				else if (json.get("texture").asString().equals("RightVineTaper")){
					RightVineTaper vin = new RightVineTaper(verts, pos[0], pos[1]);
					vin.setDensity(BASIC_DENSITY);
					vin.setFriction(BASIC_FRICTION);
					vin.setRestitution(BASIC_RESTITUTION);
					vin.setDrawScale(scale);
					vin.setBodyType(BodyDef.BodyType.StaticBody);
					vin.setTexture(retrieveTexture(json.get("texture").asString()));
					vin.setName(name);
					addObject((vin));
					json = json.next();
				}
				else if (json.get("texture").asString().equals("RightVineEnd")){
					RightVineModel vin = new RightVineModel(verts, pos[0], pos[1]);
					vin.setDensity(BASIC_DENSITY);
					vin.setFriction(BASIC_FRICTION);
					vin.setRestitution(BASIC_RESTITUTION);
					vin.setDrawScale(scale);
					vin.setBodyType(BodyDef.BodyType.StaticBody);
					vin.setTexture(retrieveTexture(json.get("texture").asString()));
					vin.setName(name);
					addObject((vin));
					json = json.next();

				}

				else {
					VineModel vin = new VineModel(verts, pos[0], pos[1]);
					vin.setDensity(BASIC_DENSITY);
					vin.setFriction(BASIC_FRICTION);
					vin.setRestitution(BASIC_RESTITUTION);
					vin.setDrawScale(scale);
					vin.setBodyType(BodyDef.BodyType.StaticBody);
					vin.setTexture(retrieveTexture(json.get("texture").asString()));
					vin.setName(name);
					addObject((vin));
					json = json.next();
				}
			}


			else {
				obj = new PolygonObstacle(verts, pos[0], pos[1]);
				//System.out.println(verts);
				obj.setDensity(BASIC_DENSITY);
				obj.setFriction(BASIC_FRICTION);
				obj.setRestitution(BASIC_RESTITUTION);
				obj.setDrawScale(scale);
				obj.setBodyType(BodyDef.BodyType.StaticBody);
				obj.setTexture(retrieveTexture(json.get("texture").asString()));

				obj.setName(name);



				if (name.contains("Spike") || name.contains("LeftSpike") || name.contains("RightSpike")) {
					obj.setJSONSpikeAngle(json.get("direction").asInt());
					obj.fixture.filter.categoryBits = Constants.HAZARD_ENTITY;
					obj.fixture.filter.maskBits = Constants.HAZARD_ENTITY | Constants.PLAYER_ENTITY;
				}

				if (json.has("rotation")) {
					obj.setJSONSpikeDirtAngle(json.get("rotation").asInt());
				}

				addObject(obj);
				json = json.next();
			}
		}
	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!super.preUpdate(dt)) {
			return false;
		}

		if (!isFailure() && avatar.getY() < -1 && !isComplete()) {
			setFailure(true);
			avatar.setVX(0);
			avatar.setVY(0);
			return false;
		}

		return true;
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */


	public void update(float dt) {

		if (pauseOn) {
			return;
		}

		if (avatar.getAngle() >= Math.PI*2){
			world.setGravity(gravity);

		}


		if (gotPowerUp) {
			num_power = num_power + 1;
			gotPowerUp = false;
		}
		if (usedPowerUp) {
			num_power = num_power - 1;
			usedPowerUp = false;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.S)) {

            InputController.leftInput = Input.Keys.A;
            InputController.rightInput = Input.Keys.D;


			world.setGravity(gravity);
			//avatar.setAngle(0);
			avatar.rotate(avatar.getAngle(),0);
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
		    System.out.println("Flip Cooldown: " + avatar.flipCooldown);
            System.out.println("Is Failing: " + avatar.fail);
            System.out.println("Standing on Root Model?: " + rmStandingOn);
            System.out.println("Is able to flip: " + avatar.can_flip);
            System.out.println("Something below: " + something_below);
        }

		//sensor disabled right now add "&& !sensor6" to add it back
		if (avatar.flipCooldown == 0 && Gdx.input.isKeyJustPressed(Input.Keys.E)  && !avatar.fail && avatar.isGrounded() &&
				(rmStandingOn != null || avatar.can_flip) && something_below == false) {
			avatar.faceRight = !avatar.faceRight;
			gravityVector.set(-world.getGravity().x,-world.getGravity().y);
			world.setGravity(gravityVector);
			if (InputController.leftInput == Input.Keys.A) {
                InputController.leftInput = Input.Keys.D;
            }
            else {
                InputController.leftInput = Input.Keys.A;
            }
            if (InputController.rightInput == Input.Keys.A) {
                InputController.rightInput = Input.Keys.D;
            }
            else {
                InputController.rightInput = Input.Keys.A;
            }
            gravityVector.set(RootModel.rootWidth*2.2f * MathUtils.cos(avatar.getAngle()-(float)Math.PI/2),
					RootModel.rootWidth*2.2f * MathUtils.sin(avatar.getAngle()-(float)Math.PI/2));
			//avatar.sensorFixture6.getBody().destroyFixture(avatar.sensorFixture6);
			avatar.setPosition(avatar.getPosition().add(gravityVector));
			avatar.setAngle((avatar.getAngle() + MathUtils.PI)% (2 * MathUtils.PI));
//			sensor6 = false;
			//avatar.sensorFixture6.getBody().createFixture(avatar.sensorDef6);
		}



		// Process actions in object model
		if (!avatar.isGrounded()) {
			avatar.rotate(avatar.getAngle(), 0);
			world.setGravity(gravity);
		}

		if (!avatar.fail && !isComplete()) {
			avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
		}
		else{
			can_grow = false;
			avatar.setMovement(0.0f);
		}
		avatar.applyForceNow();
		avatar.setJumping(false);
		avatar.setShooting(InputController.getInstance().didSecondary());

		for (EnemyModel en : enemyModels) {
			if (en.getX() <= en.getLeftMark()) {
				en.setGoingRight(true);
			} else if (en.getX() >= en.getRightMark()) {
				en.setGoingRight(false);
			}
			else if(Math.abs(en.getX()-en.getLastXLocation())<0.009){
				en.swapDirections();
			}

			en.setVX(en.getForceDirection() * en.getMaxSpeed() * 0.5f);
			en.setMovement(en.getForceDirection());
			en.setLastXLocation(en.getX());
		}
		for (LandEnemyModel en : landEnemies) {
			if (en.getX() <= en.getLeftMark()) {
				en.setGoingRight(true);
			} else if (en.getX() >= en.getRightMark()) {
				en.setGoingRight(false);
			}
			else if(Math.abs(en.getX()-en.getLastXLocation())<0.009){
				en.swapDirections();
			}

			en.setVX(en.getForceDirection() * en.getMaxSpeed() * 0.5f);
			en.setMovement(en.getForceDirection());
			en.setLastXLocation(en.getX());
		}


		boolean isJustShooting = avatar.isJustShooting();
		if (isJustShooting && rmStandingOn == null && !isShrinking()) {
			avatar.shootCooldown = 3;
			startingpos.x = avatar.getX();
			startingpos.y = avatar.getY();

		}
		else if (isJustShooting && rmStandingOn != null && !isShrinking()) {
			avatar.shootCooldown = 3;
			startingpos.x = rmStandingOn.getX();
			startingpos.y = rmStandingOn.getY();
		}



		if (avatar.isShooting() && can_grow && !isShrinking() && avatar.isGrounded()) {


			int y = (int) (startingpos.y);
			int x = (int) (startingpos.x);
			int key = 32*y + x;


			if (rmStandingOn==null&&roots.get(key) == null && can_grow && avatar.energy> 0) { //no root exists at pos or not standing on a root


				RootModel root = new RootModel(avatar.getPosition().add(2.0f,0).x, avatar.getPosition().add(0,-1.6f).y);
				if (!avatar.isFacingRight()) {
					root = new RootModel(avatar.getPosition().add(-2.0f,0).x, avatar.getPosition().add(0,-1.6f).y);
				}
				root.setDensity(HEAVY_DENSITY);
				root.setFriction(0.0f);
				root.setRestitution(0.0f);
				root.setSensor(true);
				root.setDrawScale(scale);
				root.setBodyType(BodyDef.BodyType.StaticBody);
				root.setName("Root part");

				roots.put(key, root);



				grow(root, InputController.getInstance().getDir(), INGAME_ROOT, 0, 0);

				root.setTexture(retrieveTexture("rootpiece_l"));


				RootObstacle jointPointer = root.getTail();
				while (jointPointer.previous != null) {
					if (jointPointer.type == RootObstacle.JOINT) {
						jointPointer.setTexture(retrieveTexture("joint"));
					}
					if (jointPointer.type == RootObstacle.ANCHOR) {
						jointPointer.setTexture(retrieveTexture("rootanchor"));
					}
					if (jointPointer.half == RootObstacle.LEFT_PIECE) {
						jointPointer.setTexture(retrieveTexture("rootpiece_l"));
					} else if (jointPointer.half == RootObstacle.RIGHT_PIECE){
						jointPointer.setTexture(retrieveTexture("rootpiece_r"));
					}
					jointPointer = jointPointer.previous;
				}

				root.getTail().setTexture(retrieveTexture("rootanchor"));
				root.getTail().setName("Root part");
			}
			else if(rmStandingOn!=null && can_grow){


				RootModel root = rmStandingOn;


				if (root.getTail() != null && InputController.getInstance().getDir() == (root.getTail().direction + 4) % 8) {
					//System.out.println("Entering here");
					shrink_cooldown = 0;
					shrink(root);
				}
				else {

					if (avatar.energy > 0) {
						root.setX(avatar.getPosition().add(6.0f,0).x);
						root.setY(avatar.getPosition().add(6.0f,0).y);


						grow(root, InputController.getInstance().getDir(), INGAME_ROOT, 0, 0);

						root.setTexture(retrieveTexture("rootpiece_l"));



						RootObstacle jointPointer = root.getTail();
						while (jointPointer.previous != null) {
							if (jointPointer.type == RootObstacle.JOINT) {
								jointPointer.setTexture(retrieveTexture("joint"));
							}
							if (jointPointer.half == RootObstacle.LEFT_PIECE) {
								jointPointer.setTexture(retrieveTexture("rootpiece_l"));
							} else if (jointPointer.half == RootObstacle.RIGHT_PIECE){
								jointPointer.setTexture(retrieveTexture("rootpiece_r"));
							}

							jointPointer = jointPointer.previous;
						}
						if (jointPointer.type == RootObstacle.ANCHOR) {
							jointPointer.setTexture(retrieveTexture("rootanchor"));
						}
						if (root.getTail().previous != null) {
							root.getTail().setTexture(retrieveTexture("endroot_r"));
							if (root.getTail().previous != null && root.tail.previous.type != RootObstacle.ANCHOR
									&& root.tail.previous.type != RootObstacle.JOINT) {
								root.getTail().previous.setTexture(retrieveTexture("endroot_l"));
							}
						}
					}


				}


			}
			else {
				if(avatar.energy>0) {
					RootModel root = roots.get(key);
					root.setX(avatar.getPosition().add(6.0f, 0).x);
					root.setY(avatar.getPosition().add(6.0f, 0).y);

					if (root.getTail() != null && InputController.getInstance().getDir() == (root.getTail().direction + 4) % 8) {
						//System.out.println("Entering here");
						shrink_cooldown = 0;
						shrink(root);
					} else {

						if (avatar.energy > 0) {
							grow(root, InputController.getInstance().getDir(), INGAME_ROOT, 0 ,0);

							root.setTexture(retrieveTexture("rootpiece_l"));


							RootObstacle jointPointer = root.getTail();
							while (jointPointer.previous != null) {
								if (jointPointer.type == RootObstacle.JOINT) {
									jointPointer.setTexture(retrieveTexture("joint"));
								}
								if (jointPointer.half == RootObstacle.LEFT_PIECE) {
									jointPointer.setTexture(retrieveTexture("rootpiece_l"));
								} else if (jointPointer.half == RootObstacle.RIGHT_PIECE) {
									jointPointer.setTexture(retrieveTexture("rootpiece_r"));
								}

								jointPointer = jointPointer.previous;
							}
							if (jointPointer.type == RootObstacle.ANCHOR) {
								jointPointer.setTexture(retrieveTexture("rootanchor"));
							}
							if (root.getTail().previous != null) {
								root.getTail().setTexture(retrieveTexture("endroot_r"));
								if (root.getTail().previous != null && root.tail.previous.type != RootObstacle.ANCHOR
										&& root.tail.previous.type != RootObstacle.JOINT) {
									root.getTail().previous.setTexture(retrieveTexture("endroot_l"));
								}
							}
						}


					}

				}

			}
		}



		if (pc == null) {
			pc = new PhysicsController(world, avatar);

		}


		if (isShrinking() && !avatar.isShooting()) {
			if (rmStandingOn!=null){
				shrink(rmStandingOn);

			}
		}

		if (avatar.getMovement() != 0) {
			SoundController.getInstance().play(MOVE_FILE,MOVE_FILE,true,EFFECT_VOLUME);
		}

		else if (SoundController.getInstance().isActive(MOVE_FILE)) {
			SoundController.getInstance().stop(MOVE_FILE);
		}

		//stop growing sound if growing has stopped
		if (SoundController.getInstance().isActive(GROW_FILE) &&
				((!Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.LEFT) &&
						!Gdx.input.isKeyPressed(Input.Keys.UP) && !Gdx.input.isKeyPressed(Input.Keys.DOWN)) || avatar.energy<=0)) {
			SoundController.getInstance().stop(GROW_FILE);
		}

		//stop shrinking sound if shrinking has stopped

		if (SoundController.getInstance().isActive(SHRINK_FILE) && (!Gdx.input.isKeyPressed(Input.Keys.ANY_KEY) ||
				avatar.energy == 7 || avatar.energy==0)) {

			SoundController.getInstance().stop(SHRINK_FILE);
		}

		// If we use sound, we must remember this.
		SoundController.getInstance().update();
		pc.updatePhysics();
		avatar.getBody().applyLinearImpulse(0.2f * MathUtils.cos(avatar.getAngle()-MathUtils.PI/2),
				0.2f*MathUtils.sin(avatar.getAngle()-MathUtils.PI/2), avatar.getX(), avatar.getY(), true);



	}

	public void isHazard(String objname) {
		if (!isComplete() && !isFailure()) {
			setFailure(true);
			avatar.setVX(0);
			avatar.setVY(0);
			SoundController.getInstance().play(FAIL_FILE,FAIL_FILE,false,EFFECT_VOLUME);
			rmStandingOn = null;
			avatar.fail = true;
			can_grow = false;
		}
	}

	Vector2 directionCache = new Vector2(0, 0);

	public void grow(RootModel root, int dir, int mode, int starting_x, int starting_y) {


		if (root.getTail() != null && !(Math.abs(root.getTail().direction - dir) >= 0 && Math.abs(root.getTail().direction - dir) <= 2 ||
				Math.abs(root.getTail().direction - dir) == 6 || Math.abs(root.getTail().direction - dir) == 7)) {
			return;
		}

		avatar.energy--;

		SoundController.getInstance().play(GROW_FILE,GROW_FILE,true,EFFECT_VOLUME);

		if (root.getTail() == null) {

//			float[] points = {0.0f, 1.25f, 0.75f, 1.65f, 1.1f, 1.65f, 2.0f, 1.3f, 3.0f, 1.2f, 3.0f, 0.0f, 2.0f, 0.2f, 1.25f, 0.0f, 0.0f, 0.0f};
			float[] points = {0.5f, 1.21875f, 3.125f, 1.21875f,
					3.125f, 0.53125f, 0.1875f, 0.5625f};
			//float[] points = {0.0f, 1.65f, 3.0f, 1.65f, 3.0f, 0.0f, 0.0f, 0.0f};
			root.setTail(new RootObstacle(points, root.getX(), root.getY(),  root.rootSize.x, root.rootSize.y, dir, null, RootObstacle.ANCHOR, RootObstacle.SP_PIECE));
			root.getTail().locs = points;
			Vector2 off = root.getTail().playerOffset(dir, avatar.isFacingRight(), avatar.getAngle());
			root.getTail().setBodyType(BodyDef.BodyType.StaticBody);
			root.getTail().setDensity(HEAVY_DENSITY);
			root.getTail().setFriction(BASIC_FRICTION);
			root.getTail().setRestitution(BASIC_RESTITUTION);
			root.getTail().setDrawScale(scale);
			root.getTail().setAngle(root.getTail().getAngle());

            if (mode == INGAME_ROOT) {
                root.getTail().setX(avatar.getX() + off.x);
                root.getTail().setY(avatar.getY() + off.y);
            }
            else if (mode == PREGROWN_ROOT){
                root.getTail().setX(starting_x);
                root.getTail().setY(starting_y);
            }


			addObject(root.getTail());
			root.add(root.getTail());
			root.setName("Root part");


		}

		else {

			RootObstacle tail = root.getTail();


			if (dir != tail.direction) {

				Vector2 rootoff = root.getTail().jointOffset(dir);

				if (tail.type == RootObstacle.ANCHOR) {
					if (tail.direction==GROW_TOP_LEFT || tail.direction==GROW_LEFT){
						directionCache.set(0, -0.40625f);
						rootoff.add(directionCache);

					}
					if (tail.direction==GROW_UP || tail.direction==GROW_TOP_RIGHT){
						directionCache.set(-0.40625f, 0);
						rootoff.add(directionCache);


					}
					if (tail.direction==GROW_RIGHT || tail.direction==GROW_BOTTOM_RIGHT){
						directionCache.set(0, 0.40625f);
						rootoff.add(directionCache);

					}
					if (tail.direction==GROW_DOWN || tail.direction==GROW_BOTTOM_LEFT){
						directionCache.set(0.40625f, 0);
						rootoff.add(directionCache);

					}
				}

				float[] jointpoints = {0.1125f, 0.8625f, 0.75f, 0.8625f, 0.8625f, 0.75f, 0.8625f, 0.1125f, 0.75f, 0.0f, 0.1125f, 0.0f, 0.0f, 0.1125f, 0.0f, 0.75f};
				RootObstacle joint = new RootObstacle(jointpoints, root.getTail().getX() + rootoff.x, root.getTail().getY() + rootoff.y,
						root.rootSize.x, root.rootSize.y, dir, root.getTail(), RootObstacle.JOINT, RootObstacle.SP_PIECE);


				Vector2 newoff = root.getTail().rootOffset(dir);

				if (tail.type == RootObstacle.ANCHOR) {

					if (tail.direction==GROW_TOP_LEFT || tail.direction==GROW_LEFT){
						directionCache.set(0, -0.40625f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_UP || tail.direction==GROW_TOP_RIGHT){
						directionCache.set(-0.40625f, 0);
						newoff.add(directionCache);


					}
					if (tail.direction==GROW_RIGHT || tail.direction==GROW_BOTTOM_RIGHT){
						directionCache.set(0, 0.40625f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_DOWN || tail.direction==GROW_BOTTOM_LEFT){
						directionCache.set(0.40625f, 0);
						newoff.add(directionCache);

					}

				}


				//float[] newpoints = {0.0f, 0.8f, 1.0f, 0.75f, 2.0f, 0.85f, 3.125f, 0.8f, 3.125f, 0.125f, 2.0f, 0.0f, 1.25f, 0.1f, 0.0f, 0.125f};
				float[] newpoints = {0.0f, 0.85f, 1.5625f, 0.85f, 1.5625f, 0.1f, 0.0f, 0.1f};
				RootObstacle newtail = new RootObstacle(newpoints, root.getTail().getX() + newoff.x, root.getTail().getY() + newoff.y,
						root.rootSize.x, root.rootSize.y, dir, joint, RootObstacle.NORMAL_PC, RootObstacle.LEFT_PIECE);
				newtail.setAngle(newtail.getAngle());


				tail = newtail;
				tail.setBodyType(BodyDef.BodyType.StaticBody);
				tail.setDensity(HEAVY_DENSITY);
				tail.setFriction(BASIC_FRICTION);
				tail.setRestitution(BASIC_RESTITUTION);
				//tail.setSensor(true);

				joint.setAngle(root.getTail().getAngle());


				joint.setBodyType(BodyDef.BodyType.StaticBody);
				joint.setDensity(HEAVY_DENSITY);
				joint.setFriction(BASIC_FRICTION);
				joint.setRestitution(BASIC_RESTITUTION);


				joint.setDrawScale(scale);
				root.setTail(joint);
				root.getTail().setName("Root part");
				addObject(joint);
				root.add(joint);
			}
			else {
				Vector2 newoff = root.getTail().rootOffset(dir);

				if (tail.type== RootObstacle.ANCHOR) {

					if (tail.direction==GROW_LEFT){
						directionCache.set(0.1875f, -0.40625f);
						newoff.add(directionCache);
					}
					if (tail.direction==GROW_UP){
						directionCache.set(-0.40625f, 0.1875f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_RIGHT){
						directionCache.set(0.1875f, 0.40625f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_DOWN){
						directionCache.set(0.40625f, -0.1875f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_TOP_LEFT){
						directionCache.set(-0.1875f - 0.1875f, -0.40625f + 0.1875f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_TOP_RIGHT){
						directionCache.set(-0.40625f + 0.1875f, 0.1875f + 0.1875f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_BOTTOM_RIGHT){
						directionCache.set(0.1875f + 0.1875f, 0.40625f - 0.1875f);
						newoff.add(directionCache);

					}
					if (tail.direction==GROW_BOTTOM_LEFT){
						directionCache.set(0.40625f - 0.1875f, -0.1875f - 0.1875f);
						newoff.add(directionCache);

					}

				}

				//float[] newpoints = {0.0f, 0.8f, 1.0f, 0.75f, 2.0f, 0.85f, 3.125f, 0.8f, 3.125f, 0.125f, 2.0f, 0.0f, 1.25f, 0.1f, 0.0f, 0.1538f};
				float[] newpoints = {0.0f, 0.85f, 1.5625f, 0.85f, 1.5625f, 0.1f, 0.0f, 0.1f};
				RootObstacle newtail = new RootObstacle(newpoints, root.getTail().getX() + newoff.x, root.getTail().getY() + newoff.y,
						root.rootSize.x, root.rootSize.y, dir, root.getTail(), RootObstacle.NORMAL_PC, (root.getTail().half + 1)%2);
				newtail.setAngle(newtail.getAngle());


				tail = newtail;
				tail.setBodyType(BodyDef.BodyType.StaticBody);
				tail.setDensity(HEAVY_DENSITY);
				tail.setFriction(BASIC_FRICTION);
				tail.setRestitution(BASIC_RESTITUTION);
				tail.setName("Root part");
				//tail.setSensor(true);
			}


			tail.setDrawScale(scale);
			root.setTail(tail);

			addObject(tail);
			root.add(tail);
		}

	}

	public void shrink(RootModel root) {

		SoundController.getInstance().play(SHRINK_FILE,SHRINK_FILE,true,EFFECT_VOLUME);

		int y = (int) avatar.getY();
		int x = (int) avatar.getX();
		int key = 32*y + x;
		if (roots.get(key) == null && rmStandingOn==null) {

			return;
		}


		if(avatar.fail || root.getTail() == null) {
			/*for (Obstacle o : objects) {
				if (o.getName().equals("Root part")) {
					o.markRemoved(true);
					avatar.energy++;
				}
			}
			roots.clear();*/
			return;
		}

		else {

			if (shrink_cooldown <= 0) {
				if (avatar.energy < 18) {
					avatar.energy++;
					//System.out.println("line");
				}

				if (avatar.energy >= level_max_energy) {
					if (avatar.isUpsideDown()) {
						world.setGravity(new Vector2(0,14.7f));
						avatar.rotate(avatar.getAngle(),(float)Math.PI);
					}
					else {
						world.setGravity(gravity);
						avatar.rotate(avatar.getAngle(),0);
					}
					roots.clear();
				}

				root.tail.markRemoved(true);
				root.tail = root.tail.previous;

				if (root.tail != null && root.tail.type == RootObstacle.JOINT) {
					root.tail.markRemoved(true);
					root.tail = root.tail.previous;
				}
				if (root.tail != null && root.tail.type != RootObstacle.ANCHOR) {
					root.tail.setTexture(retrieveTexture("endroot_r"));
					if (root.getTail().previous != null && root.tail.previous.type != RootObstacle.ANCHOR
							&& root.tail.previous.type != RootObstacle.JOINT) {
						root.getTail().previous.setTexture(retrieveTexture("endroot_l"));
					}
				}

				shrink_cooldown = 10;
			}
			shrink_cooldown--;
		}

	}

	public boolean isShrinking() {
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && !avatar.isShooting()) {

			return true;
		}
		return false;
	}

	private boolean compareByName(Obstacle a, Obstacle b, String firstName, String secondName) {
		return (a.getName().equals(firstName) && b.getName().equals(secondName)) ||
				((b.getName().equals(firstName) && a.getName().equals(secondName)));
	}

	private void removeBoardingBreakables(BreakableModel br) {
		for (BreakableModel br2 : breakable) {
			if(Math.abs(br2.getX()-br.getX()) < 1 && Math.abs(br2.getY()-br.getY()) < 3 ||
					Math.abs(br2.getX()-(br.getX()+br.getWidth())) < 1 && Math.abs(br2.getY()-br.getY()) < 3 ||
					Math.abs(br2.getX()+br2.getWidth()-br.getX()) < 1 && Math.abs(br2.getY()-br.getY()) < 3 ||
					Math.abs(br2.getX()+br2.getWidth()-br.getX()) < 1 && Math.abs(br2.getY()-br.getY()) < 3 ||
					Math.abs(br2.getX()+br2.getWidth()-(br.getX()+br.getWidth())) < 1 && Math.abs(br2.getY()-br.getY()) < 3 ) {
				br2.markRemoved(true);
				breakable.remove(br2);
				removeBoardingBreakables(br2);
			}
		}
	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */



	public void beginContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();


		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();

			//System.out.println(bd1.getName() + " " + bd2.getName());

			if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
					(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
				avatar.setGrounded(true);
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1);
			}
			if ((avatar.getSensor5Name().equals(fd2) && avatar != bd1) ||
					(avatar.getSensor5Name().equals(fd1) && avatar != bd2)) {
				avatar.setGrounded(true);
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1);
			}
			if ((avatar.getSensor7Name().equals(fd2) && avatar != bd1) ||
					(avatar.getSensor7Name().equals(fd1) && avatar != bd2)) {
				avatar.setGrounded(true);
				sensorFixtures.add(avatar == bd1 ? fix2 : fix1);
			}

			//System.out.println(bd2.getName() + " " + bd1.getName());

//			if ((avatar.getSensor4Name().equals(fd1) && !bd2.getName().equals("vine"))
//					|| (avatar.getSensor4Name().equals(fd2) && !bd1.getName().equals("vine"))) {
//				can_grow = false;
//				avatar.can_flip = false;
//				//rmStandingOn = null;
//			}

			//System.out.println(fd1 + " " + bd2.getName() + " " + fd2 + " " + bd1.getName() + " " + bd2.getPosition() + " " + bd1.getPosition());



			if ((avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("breakable"))
					|| (avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("breakable"))) {
				Obstacle target;
				if (bd2.getName().equals("breakable")) {
					target = bd2;
				}
				else target = bd1;
				//avatar.rotate(avatar.getAngle(),0);

				if (num_power > 0) {
					for (BreakableModel br : breakable) {
							if (br.getPosition().equals(target.getPosition())) {
								br.markRemoved(true);
								breakable.remove(br);
								usedPowerUp = true;
								removeBoardingBreakables(br);
							}
					}
					SoundController.getInstance().play(BREAK_FILE,BREAK_FILE,false,EFFECT_VOLUME);
				}
				//rmStandingOn = null;
			}



			if (avatar.getSensorName().equals(fd1) && bd2.getName().equals("powerup") ||
					avatar.getSensorName().equals(fd2) && bd1.getName().equals("powerup")) {
				if (bd1.getName().equals("powerup")) bd1.markRemoved(true);
				else bd2.markRemoved(true);
				gotPowerUp = true;
				SoundController.getInstance().play(POWERUP_FILE,POWERUP_FILE,false,EFFECT_VOLUME-0.4f);

			}

			if (avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("Spike") ||
					avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("Spike")) {
				isHazard("Spike");
			}

			if (avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("RightSpike") ||
					avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("RightSpike")) {
				isHazard("RightSpike");
			}

			if (avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("LeftSpike") ||
					avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("LeftSpike")) {
				isHazard("LeftSpike");
			}

			if (avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("enemy") ||
					avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("enemy")) {
				isHazard("enemy");
			}

			if ((avatar.getSensor4Name().equals(fd1) && bd2.getName().equals("breakable"))
					|| (avatar.getSensor4Name().equals(fd2) && bd1.getName().equals("breakable"))) {

                avatar.can_flip = false;
                can_grow = false;

				world.setGravity(gravity);
				avatar.setAngle(0);
			}

			if ((avatar.getSensor5Name().equals(fd1) && bd2.getName().equals("Root part"))
					|| (avatar.getSensor5Name().equals(fd2) && bd1.getName().equals("Root part"))) {
				//System.out.println("On root");
				RootObstacle rootStandingOn = null;
				if(bd1.getName().equals("Root part")){
					rootsInContact.add((RootObstacle)bd1);
					rootStandingOn = (RootObstacle) bd1;
					if (bd1.getAngle() == 0 && world.getGravity().y < 0) {
						avatar.rotate(avatar.getAngle(), 0);
						world.setGravity(gravity);
					}
				}
				else{
					rootsInContact.add((RootObstacle)bd2);
					rootStandingOn = (RootObstacle) bd2;
					if (bd2.getAngle() == 0 && world.getGravity().y < 0) {
						avatar.rotate(avatar.getAngle(), 0);
						world.setGravity(gravity);
					}
				}


				for(RootModel rm : roots.values()){ //root segment
					for(Obstacle ro : rm.getBodies()){ //root pieces
						if ((RootObstacle) ro == rootStandingOn){
							rmStandingOn = rm;
						}
					}
				}
				//can_grow = true;
			}

			if ((avatar.getSensor7Name().equals(fd1) && bd2.getName().equals("goal"))
					|| (avatar.getSensor7Name().equals(fd2) && bd1.getName().equals("goal"))) {
				setComplete(true);
				avatar.setVX(0);
				avatar.setVY(0);
				SoundController.getInstance().play(WIN_FILE,WIN_FILE,false,EFFECT_VOLUME);
				//	rmStandingOn = null;
			}
			if ((avatar.getSensor4Name().equals(fd1) && bd2.getName().equals("platform"))
					|| (avatar.getSensor4Name().equals(fd2) && bd1.getName().equals("platform"))) {
				avatar.can_flip = false;
				can_grow = false;
				//	rmStandingOn = null;
			}

			if (compareByName(bd1,bd2,"dude","platform") ||
					compareByName(bd2,bd1,"dude","platform")) {
				if ((avatar.getSensor4Name().equals(fd1) && bd2.getName().equals("platform"))
						|| (avatar.getSensor4Name().equals(fd2) && bd1.getName().equals("platform"))) {
					//avatar.rotate(avatar.getAngle(), 0);
					//world.setGravity(gravity);
				}
			}

			if ((avatar.getSensor4Name().equals(fd1) && bd2.getName().equals("vine"))
					|| (avatar.getSensor4Name().equals(fd2) && bd1.getName().equals("vine"))) {
				avatar.can_flip = true;
				can_grow = true;
				if (bd1.getName().equals("vine")) {
					if (bd1.getBody().getPosition().y > avatar.getPosition().y + avatar.getHeight()/2)
						avatar.rotate(avatar.getAngle(),(float)Math.PI);
				}
				else {
					if (bd2.getBody().getPosition().y > avatar.getPosition().y + avatar.getHeight()/2)
						avatar.rotate(avatar.getAngle(),(float)Math.PI);
				}
			}
			if ((avatar.getSensor4Name().equals(fd1) && bd2.getName().equals("Root part"))
					|| (avatar.getSensor4Name().equals(fd2) && bd1.getName().equals("Root part"))) {
				avatar.can_flip = true;
				can_grow = true;
			}


//			if ((bd1.getName().equals("dude"))){
//				RootObstacle rootStandingOn = null;
//				try{
//					rootStandingOn = (RootObstacle) bd1;
//					for(RootModel rm : roots.values()){ //root segment
//						for(Obstacle ro : rm.getBodies()){ //root pieces
//							if ((RootObstacle) ro == rootStandingOn){
//								rmStandingOn = rm;
//							}
//						}
//					}
//					can_grow = true;
//				}
//				catch(Exception e) {
//
//				}
//			}

			if (avatar.getSensor6Name().equals(fd1) && !bd2.getName().equals("powerup") ||
					avatar.getSensor6Name().equals(fd2) && !bd1.getName().equals("powerup")) {
				//System.out.println(bd2.getName() + " " + bd1.getName());
				//System.out.println(bd2.getBody().getPosition() + " " + bd1.getBody().getPosition());
				something_below = true;
				sensor6 = true;

			}



		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();

		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		Obstacle ad1 = (Obstacle)body1.getUserData();
		Obstacle ad2 = (Obstacle)body2.getUserData();

        something_below = false;


		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
				(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
		}
		if ((avatar.getSensor5Name().equals(fd2) && avatar != bd1) ||
				(avatar.getSensor5Name().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
		}
		if ((avatar.getSensor7Name().equals(fd2) && avatar != bd1) ||
				(avatar.getSensor7Name().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
		}


		if ((avatar.getSensor5Name().equals(fd1) && ad2.getName().equals("Root part"))
				|| (avatar.getSensor5Name().equals(fd2) && ad1.getName().equals("Root part"))) {
			// check if there are any sensor fixtures of roots that we are currently touching
			// if not, then set rmStandingOn to be null
			if (ad1.getName().equals(("Root part"))) {
				rootsInContact.remove((RootObstacle)ad1);
			} else {
				rootsInContact.remove((RootObstacle)ad2);
			}
			if (rootsInContact.size == 0) {
				rmStandingOn = null;
				
				InputController.leftInput = Input.Keys.A;
                InputController.rightInput = Input.Keys.D;

				//System.out.println("End contact with root");

			}

		}

		if (avatar.getSensor6Name().equals(fd1) && !ad2.getName().equals("powerup") ||
				avatar.getSensor6Name().equals(fd2) && !ad1.getName().equals("powerup")) {

			something_below = false;
			sensor6 = false;

		}


		if (sensorFixtures.size == 0) {
			avatar.setGrounded(false);
//			if (!avatar.getSensor6Name().equals(fd1) && !avatar.getSensor6Name().equals(fd2)) {
//                can_grow = false;
//                avatar.can_flip = false;
//            }
			something_below = false;
		}

		//System.out.println(ad1.getName() + " " + ad2.getPosition() + " " + fd2 + " " + fd1);

	}

	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}
}
