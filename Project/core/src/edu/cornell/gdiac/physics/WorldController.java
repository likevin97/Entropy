/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics;

import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.physics.platform.*;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.model.*;

import javax.sound.sampled.Line;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public abstract class WorldController implements Screen {

	/**
	 * Tracks the asset state.  Otherwise subclasses will try to load assets
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

	/** Track asset loading from all instances and subclasses */
	protected AssetState worldAssetState = AssetState.EMPTY;

	protected BitmapFont displayFont;
	protected BitmapFont tutorialFont;

	// Pathnames to shared assets
	/** File to texture for walls and platforms */
	/** File to texture for the win door */

	private JsonReader jsonReader;
	private JsonValue assetDirectory;
	private JsonValue levelFormat;

	/** The level represented by this world **/
	protected LevelModel level;

	protected SpriteBatch batch;



	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent() {
		if (worldAssetState != AssetState.EMPTY) {
			return;
		}
		worldAssetState = AssetState.LOADING;

		jsonReader = new JsonReader();
		assetDirectory = jsonReader.parse(Gdx.files.internal("json/assets.json"));

		JsonAssetManager.getInstance().loadDirectory(assetDirectory);


	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */

	protected TextureRegion exit;
	public void loadContent() {
		if (worldAssetState != AssetState.LOADING) {
			return;
		}

		JsonAssetManager.getInstance().allocateDirectory();
		displayFont = JsonAssetManager.getInstance().getEntry("display", BitmapFont.class);
		tutorialFont = JsonAssetManager.getInstance().getEntry("tutorial", BitmapFont.class);
		tutorialFont.setColor(Color.WHITE);
		exit = JsonAssetManager.getInstance().getEntry("goal",TextureRegion.class);
		worldAssetState = AssetState.COMPLETE;
	}
	/**
	 * Helper to initialize a texture after loading.
	 *
	 * @param manager Reference to global asset manager
	 * @param key The key identifying the texture in the loader
	 *
	 * @return the texture newly initialized
	 */
	public static Texture loadTexture(AssetManager manager, String key) {
		Texture result = null;
		if (manager.isLoaded(key)) {
			result = manager.get(key, Texture.class);
			result.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		return result;
	}


	/**
	 * Returns a newly loaded texture region for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * whether or not the texture should repeat) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param repeat	Whether the texture should be repeated
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
		if (manager.isLoaded(file)) {
			TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			if (repeat) {
				region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			}
			return region;
		}
		return null;
	}

	/**
	 * Returns a newly loaded filmstrip for the given file.
	 *
	 * This helper methods is used to set texture settings (such as scaling, and
	 * the number of animation frames) after loading.
	 *
	 * @param manager 	Reference to global asset manager.
	 * @param file		The texture (region) file
	 * @param rows 		The number of rows in the filmstrip
	 * @param cols 		The number of columns in the filmstrip
	 * @param size 		The number of frames in the filmstrip
	 *
	 * @return a newly loaded texture region for the given file.
	 */
	protected FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
		if (manager.isLoaded(file)) {
			FilmStrip strip = new FilmStrip(manager.get(file, Texture.class),rows,cols,size);
			strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			return strip;
		}
		return null;
	}

	/**
	 * Unloads the assets for this game.
	 *
	 * This method erases the static variables.  It also deletes the associated textures
	 * from the asset manager. If no assets are loaded, this method does nothing.
	 *
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent() {
		JsonAssetManager.getInstance().unloadDirectory();
		JsonAssetManager.clearInstance();
	}

	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
	/** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 75;

	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;

	/** Width of the game world in Box2d units */
	public static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	public static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -4.9f;

	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	public static int levelNum;


	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Whether or not debug mode is active */
	private boolean debug;
	/** Countdown active for winning or losing */
	private int countdown;

	private boolean goBack = false;



	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}

	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @param the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param value the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();


	}

	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected WorldController() {
		world = new World(new Vector2(0,DEFAULT_GRAVITY),false);
		jsonReader = new JsonReader();
		level = new LevelModel();
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;

	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected WorldController(float width, float height, float gravity) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */

	Texture goodluck;
	protected WorldController(Rectangle bounds, Vector2 gravity) {

		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;
		levelNum = 0;



	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
		//level.dispose();
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public abstract void reset(int num);

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param delta Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */

	public boolean pauseOn = false;

	public boolean preUpdate(float dt) {
		InputController input = InputController.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}

		// Handle resets
		if (input.didReset()) {
			reset(levelNum);
		}

		// Now it is time to maybe switch screens.
		if (goBack){
			goBack = false;
			pauseOn = false;
			listener.exitScreen(this, 10000);
		}
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (input.didAdvance()) {
			if (levelNum == 14){
				goBack = true;

			}
			else{
				listener.exitScreen(this, EXIT_NEXT);
			}
			return false;
		} else if (input.didRetreat()) {
			listener.exitScreen(this, EXIT_PREV);
			return false;
		}
		if ((complete && !failed) || failed){
			if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)){
				endpointerLevel = (endpointerLevel + 1) % 2;

			}
			else if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)){
				endpointerLevel = (endpointerLevel + 1) % 2;
			}

			if (failed){
				if (endpointerLevel == 0 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
					reset(levelNum);
				}
				else if (endpointerLevel == 1 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
					goBack = true;
				}
			}
			else if (!failed){
				if (endpointerLevel == 0 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
					if (levelNum == 14){
						goBack = true;
					}
					else{
						listener.exitScreen(this, EXIT_NEXT);
					}

					return false;
				}
				else if (endpointerLevel == 1 && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
					goBack = true;
				}
			}
		}

//		else if (countdown > 0) {
//			countdown--;
//		} else if (countdown == 0) {
//			if (failed) {
//				reset(levelNum);
//			} else if (complete) {
//				listener.exitScreen(this, EXIT_NEXT);
//				return false;
//			}
//		}



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
	 * @param delta Number of seconds since last animation frame
	 */
	public abstract void update(float dt);

	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	int i = 0;
	public static boolean levelSelect = false;
	boolean mouseOn;

	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}

		// Turn the physics engine crank.
		if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
			if (pauseOn == false){
				pauseOn = true;
			}
			else{
				pauseOn = false;
			}

		}
		if (pauseOn){

			if (Gdx.input.getDeltaX() > 0 || Gdx.input.getDeltaY() > 0){
				mouseOn = true;
			}
			if (mouseOn){
				if (Gdx.input.getY() >= 220 && Gdx.input.getY() <= 260){
					pointerLevel = 0;
					//System.out.println("lower half");
				}
				if (Gdx.input.getY() >= 280 && Gdx.input.getY() <= 320){
					pointerLevel = 1;
					//System.out.println("lower half");
				}
				if (Gdx.input.getY() >= 340 && Gdx.input.getY() <= 380){
					pointerLevel = 2;
					//System.out.println("lower half");
				}
				if (Gdx.input.getY() >= 400 && Gdx.input.getY() <= 440){
					pointerLevel = 3;
					//System.out.println("lower half");
				}
			}

			if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)){
				mouseOn = false;
				if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)){
					pointerLevel = (pointerLevel + 1) % 4;

				}
				else if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)){
					pointerLevel = (pointerLevel + 3) % 4;

				}

			}


			if (pointerLevel == 0 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isButtonPressed(Input.Buttons.LEFT))){
				pauseOn = false;
				pointerLevel = 0;
			}
			if (pointerLevel == 1 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isButtonPressed(Input.Buttons.LEFT))){
				pauseOn = false;
				reset(levelNum);
			}
			if (pointerLevel == 2 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isButtonPressed(Input.Buttons.LEFT))){
				goBack = true;
				levelSelect = false;
			}
			if (pointerLevel == 3 && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isButtonPressed(Input.Buttons.LEFT))){
				goBack = true;
				levelSelect = true;
			}
		}

		if (!pauseOn){
			world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);
		}


		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
		}
	}

	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param canvas The drawing context
	 */

	TextureRegion pausescreen;
	TextureRegion darkOverlay;

	float x;
	float y;

	private static int pointerX;

	private static int resumePointerY;
	private static int restartPointerY;
	private static int menuPointerY;
	private static int levelSelectPointerY;

	private int pointerLevel = 0;
	private float deltaCombo = 0;

	private static int[][] pointers = new int[4][2];
	TextureRegion pointer;

	private static int[][] endpointers = new int[2][2];
	private int endpointerLevel = 0;




	//drop tutorial animation
	private Texture dropSheet = new Texture(Gdx.files.internal("tutorial/droptutorial.png"));

	private TextureRegion[][] droptmp = TextureRegion.split(dropSheet, dropSheet.getWidth()/5,
			dropSheet.getHeight() / 1);

	private TextureRegion[] dropFrames = new TextureRegion[5];
	private Animation dropAnimation = new Animation<TextureRegion>(0.75f, dropFrames);


	//flip tutorial animation
	private Texture flipSheet = new Texture(Gdx.files.internal("tutorial/fliptutorial.png"));

	private TextureRegion[][] fliptmp = TextureRegion.split(flipSheet, flipSheet.getWidth()/6,
			flipSheet.getHeight());

	private TextureRegion[] flipFrames = new TextureRegion[6];
	private Animation flipAnimation = new Animation<TextureRegion>(0.75f, flipFrames);

	//grow tutorial animation
	private Texture growSheet = new Texture(Gdx.files.internal("tutorial/growtutorial.png"));

	private TextureRegion[][] growtmp = TextureRegion.split(growSheet, growSheet.getWidth()/8,
			growSheet.getHeight() / 1);

	private TextureRegion[] growFrames = new TextureRegion[8];
	private Animation growAnimation = new Animation<TextureRegion>(1.5f, growFrames);



	//walk tutorial animation
	private Texture walkSheet = new Texture(Gdx.files.internal("tutorial/walktutorial.png"));

	private TextureRegion[][] walktmp = TextureRegion.split(walkSheet, walkSheet.getWidth()/2,
			walkSheet.getHeight() / 1);

	private TextureRegion[] walkFrames = new TextureRegion[2];
	private Animation walkAnimation = new Animation<TextureRegion>(1.0f, walkFrames);





	//breakable tutorial animation
	private Texture breakableSheet = new Texture(Gdx.files.internal("tutorial/breakabletutorial.png"));

	private TextureRegion[][] breakabletmp = TextureRegion.split(breakableSheet, breakableSheet.getWidth()/6,
			breakableSheet.getHeight() / 1);

	private TextureRegion[] breakableFrames = new TextureRegion[6];
	private Animation breakableAnimation = new Animation<TextureRegion>(1.0f, breakableFrames);

	//enemy animation
	private Texture enemySheet = new Texture(Gdx.files.internal("tutorial/enemytutorial.png"));

	private TextureRegion[][] enemytmp = TextureRegion.split(enemySheet, enemySheet.getWidth()/6,
			enemySheet.getHeight() / 1);

	private TextureRegion[] enemyFrames = new TextureRegion[6];
	private Animation enemyAnimation = new Animation<TextureRegion>(1.0f, enemyFrames);

	//space animation
	private Texture spaceSheet = new Texture(Gdx.files.internal("tutorial/ungrowtutorial.png"));

	private TextureRegion[][] spacetmp = TextureRegion.split(spaceSheet, spaceSheet.getWidth()/5,
			spaceSheet.getHeight() / 1);

	private TextureRegion[] spaceFrames = new TextureRegion[5];
	private Animation spaceAnimation = new Animation<TextureRegion>(1.0f, spaceFrames);



	private Texture speechbubble = new Texture(Gdx.files.internal("tutorial/speechbubble.png"));

	float stateTime = 0;
	TextureRegion currentFrame;
	TextureRegion crystalTexture;
	TextureRegion endOverlay;


	public void draw(float delta) {
		crystalTexture = JsonAssetManager.getInstance().getEntry("crystal", TextureRegion.class);
		endOverlay = JsonAssetManager.getInstance().getEntry("endOverlay", TextureRegion.class);

		goodluck = new Texture(Gdx.files.internal("tutorial/goodluck.png"));
		stateTime += Gdx.graphics.getDeltaTime();
		tutorialFont.setColor(Color.DARK_GRAY);


		canvas.clear();

		batch = new SpriteBatch();


		canvas.begin();

		//openingBG.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		pointer = JsonAssetManager.getInstance().getEntry("pointer", TextureRegion.class);

		pointerX = canvas.getWidth()/2 - 150;

		resumePointerY = canvas.getHeight()/2 + 40;
		restartPointerY = resumePointerY - 60;
		menuPointerY = resumePointerY - 120;
		levelSelectPointerY = resumePointerY - 180;

		pointers[0][0] = pointerX;
		pointers[0][1] = resumePointerY;

		pointers[1][0] = pointerX;
		pointers[1][1] = restartPointerY;

		pointers[2][0] = pointerX;
		pointers[2][1] = menuPointerY;

		pointers[3][0] = pointerX;
		pointers[3][1] = levelSelectPointerY;

		endpointers[0][0] = pointerX;
		endpointers[0][1] = resumePointerY - 75;

		endpointers[1][0] = pointerX;
		endpointers[1][1] = resumePointerY - 175;

		canvas.draw(JsonAssetManager.getInstance().getEntry("background", TextureRegion.class),
				Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());


		canvas.end();

		canvas.begin();

		if (pauseOn){
			pausescreen = JsonAssetManager.getInstance().getEntry("pausescreen", TextureRegion.class);
			darkOverlay = JsonAssetManager.getInstance().getEntry("darkOverlay", TextureRegion.class);


			canvas.draw(darkOverlay, Color.WHITE,
					canvas.camera.position.x - darkOverlay.getRegionWidth()/2, canvas.camera.position.y - darkOverlay.getRegionHeight()/2,
					1024, 576);
			canvas.draw(pausescreen, Color.WHITE,
					canvas.camera.position.x - pausescreen.getTexture().getWidth()/2, canvas.camera.position.y - pausescreen.getTexture().getHeight()/2,
					609, 427);

			canvas.draw(pointer, Color.WHITE,
					pointers[pointerLevel][0], pointers[pointerLevel][1],
					10, 15);

			canvas.end();
			return;

		}

		for(Obstacle obj : objects) {
			if (levelNum == 0){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 450, canvas.getCamHeight()/2 - 240);
			}
			if (levelNum == 1){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 400, canvas.getCamHeight()/2 - 175);
			}
			if (levelNum == 2){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 250, canvas.getCamHeight()/2 + 115);

				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 + 250, canvas.getCamHeight()/2 - 85);
			}
			if (levelNum == 3){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 450, canvas.getCamHeight()/2 - 240);


			}

			if (levelNum == 4){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 450, canvas.getCamHeight()/2 - 240);

				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 + 400, canvas.getCamHeight()/2 + 150);

			}

			if (levelNum == 15){
				canvas.draw(crystalTexture.getTexture(), canvas.getCamWidth()/2 - 450, canvas.getCamHeight()/2 - 240);
			}

			if (!(obj instanceof RootObstacle && ((RootObstacle) obj).type == 1 || obj.getName().equals("dude"))) {
				obj.draw(canvas);
			}
		}

		for(Obstacle obj : objects) {

			if ((obj instanceof RootObstacle && ((RootObstacle) obj).type == 1) || obj.getName().equals("dude")) {
				obj.draw(canvas);
			}
		}

//		canvas.draw(exit, Color.WHITE, 0, 0, 0, 40, 0, 1, 1);


		int index = 0;
		if (dropFrames[0] == null){
			for (int i = 0; i < 5; i++) {
				dropFrames[index++] = droptmp[0][i];

			}
		}
		index = 0;
		if (flipFrames[0] == null){
			for (int i = 0; i < 6; i++) {
				System.out.println(i);
				flipFrames[index++] = fliptmp[0][i];

			}
		}

		index = 0;
		if (growFrames[0] == null){
			for (int i = 0; i < 8; i++) {
				growFrames[index++] = growtmp[0][i];

			}
		}

		index = 0;
		if (walkFrames[0] == null){
			for (int i = 0; i < 2; i++) {
				walkFrames[index++] = walktmp[0][i];

			}
		}

		index = 0;
		if (breakableFrames[0] == null){
			for (int i = 0; i < 6; i++) {
				breakableFrames[index++] = breakabletmp[0][i];

			}
		}

		index = 0;
		if (enemyFrames[0] == null){
			for (int i = 0; i < 6; i++) {
				enemyFrames[index++] = enemytmp[0][i];

			}
		}

		index = 0;
		if (spaceFrames[0] == null){
			for (int i = 0; i < 5; i++) {
				spaceFrames[index++] = spacetmp[0][i];

			}
		}




		if (levelNum == 0) {
			//walk animation

			currentFrame = (TextureRegion)walkAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 375, canvas.getCamHeight()/2 - 90);
			canvas.draw(currentFrame, canvas.getCamWidth()/2 - 375, canvas.getCamHeight()/2 - 90);



		}
		if (levelNum == 1) {

			//grow animation

			currentFrame = (TextureRegion)growAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 325, canvas.getCamHeight()/2 - 50);
			canvas.draw(currentFrame, canvas.getCamWidth()/2 - 325, canvas.getCamHeight()/2 - 50);


		}
		if (levelNum == 2) {


			currentFrame = (TextureRegion) flipAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 170, canvas.getCamHeight()/2 + 150);
			canvas.draw(currentFrame, canvas.getCamWidth()/2 - 170, canvas.getCamHeight()/2 + 150);


			currentFrame = (TextureRegion) dropAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

			canvas.draw(speechbubble, (float)canvas.getCamWidth()/2 + 60, (float)canvas.getCamHeight()/2 - 50,
					speechbubble.getWidth(), speechbubble.getHeight(), 0, 0, speechbubble.getWidth(), speechbubble.getHeight(),
					true, false);

			canvas.draw(currentFrame, canvas.getCamWidth()/2 + 60, canvas.getCamHeight()/2 - 40);



		}

		if (levelNum == 3) {
			//walk animation

			currentFrame = (TextureRegion)breakableAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 375, canvas.getCamHeight()/2 - 90);
			canvas.draw(currentFrame, canvas.getCamWidth()/2 - 360, canvas.getCamHeight()/2 - 78);



		}
		if (levelNum == 4) {

			currentFrame = (TextureRegion)spaceAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 375, canvas.getCamHeight()/2 - 180);
			canvas.draw(currentFrame, canvas.getCamWidth()/2 - 375, canvas.getCamHeight()/2 - 180);


			currentFrame = (TextureRegion) enemyAnimation.getKeyFrame(stateTime, true);
			currentFrame.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);




			canvas.draw(speechbubble, (float)canvas.getCamWidth()/2 + 215, (float)canvas.getCamHeight()/2 + 160,
					speechbubble.getWidth(), speechbubble.getHeight(), 0, 0, speechbubble.getWidth(), speechbubble.getHeight(),
					true, false);

			canvas.draw(currentFrame, canvas.getCamWidth()/2 + 215, canvas.getCamHeight()/2 + 160);

		}

		if (levelNum == 14){
			canvas.draw(speechbubble, canvas.getCamWidth()/2 - 370, canvas.getCamHeight()/2 - 120);
			canvas.draw(goodluck, canvas.getCamWidth()/2 - 330, canvas.getCamHeight()/2 - 90);

		}



		canvas.end();


		if (debug) {
			canvas.beginDebug();
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}


		// Final message
		if (complete && !failed) {
//			displayFont.setColor(new Color(0x30dd7e));
			canvas.begin(); // DO NOT SCALE
			canvas.draw(endOverlay, canvas.getCamWidth()/2 - endOverlay.getRegionWidth()/2, canvas.getCamHeight()/2 - 200);
			canvas.drawTextCentered("VICTORY!", displayFont, 70f);

			canvas.drawTextCentered("Next level", displayFont, -30.0f);

			canvas.drawTextCentered("Main menu", displayFont, -130.0f);

			canvas.draw(pointer, endpointers[endpointerLevel][0], endpointers[endpointerLevel][1]);
			canvas.end();
		} else if (failed) {
//			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.draw(endOverlay, canvas.getCamWidth()/2 - endOverlay.getRegionWidth()/2, canvas.getCamHeight()/2 - 200);
			canvas.drawTextCentered("FAILURE!", displayFont, 70f);
			canvas.drawTextCentered("Restart level", displayFont, -30.0f);

			canvas.drawTextCentered("Main menu", displayFont, -130.0f);
			canvas.draw(pointer, endpointers[endpointerLevel][0], endpointers[endpointerLevel][1]);
			canvas.end();
		}
	}

	/**
	 * Called when the Screen is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);




		}
	}

	/**
	 * Called when the Screen is paused.
	 *
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	public void setNum(int i) {
		levelNum = i;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

}