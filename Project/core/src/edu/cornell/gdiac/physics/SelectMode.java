package edu.cornell.gdiac.physics;



import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.utils.Json;
import edu.cornell.gdiac.util.*;

/**
 * Created by justintran on 5/17/17.
 */
public class SelectMode implements Screen, InputProcessor, ControllerListener {
    private static final String LEVEL1 = "menu/select1.png";
    private static final String LEVEL2 = "menu/select2.png";
    private static final String LEVEL3 = "menu/select3.png";
    private static final String LEVEL4 = "menu/select4.png";

    private static final String LEVEL5 = "menu/select5.png";
    private static final String LEVEL6 = "menu/select6.png";
    private static final String LEVEL7 = "menu/select7.png";
    private static final String LEVEL8 = "menu/select8.png";

    private static final String LEVEL9 = "menu/select9.png";
    private static final String LEVEL10 = "menu/select10.png";
    private static final String LEVEL11 = "menu/select11.png";
    private static final String LEVEL12 = "menu/select12.png";

    private static final String LEVEL13 = "menu/select13.png";
    private static final String LEVEL14 = "menu/select14.png";
    private static final String LEVEL15 = "menu/select15.png";

    private Texture level1;
    private Texture level2;
    private Texture level3;
    private Texture level4;

    Texture level5;

    private Texture level6;
    private Texture level7;
    private Texture level8;
    private Texture level9;
    private Texture level10;
    private Texture level11;
    private Texture level12;
    private Texture level13;
    private Texture level14;
    private Texture level15;

    private JsonAssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    private boolean active;



    public SelectMode(GameCanvas canvas, int millis) {
        this.manager = JsonAssetManager.getInstance();
        this.canvas  = canvas;


        // Compute the dimensions from the canvas


        level1 = new Texture(LEVEL1);
        level2 = new Texture(LEVEL2);
        level3 = new Texture(LEVEL3);
        level4 = new Texture(LEVEL4);
        level5 = new Texture(LEVEL5);
        level6 = new Texture(LEVEL6);
        level7 = new Texture(LEVEL7);
        level8 = new Texture(LEVEL8);
        level9 = new Texture(LEVEL9);
        level10 = new Texture(LEVEL10);
        level11 = new Texture(LEVEL11);
        level12 = new Texture(LEVEL12);
        level13 = new Texture(LEVEL13);
        level14 = new Texture(LEVEL14);
        level15 = new Texture(LEVEL15);




        //Gdx.input.setInputProcessor(this);


        // Let ANY connected controller start the game.
//        for(Controller controller : Controllers.getControllers()) {
//            controller.addListener(this);
//        }

        active = true;

    }
    public void dispose() {
        level1 = null;
        level2 = null;
        level3 = null;
        level4 = null;
        level5 = null;
        level6 = null;
        level7 = null;
        level8 = null;
        level9 = null;
        level10 = null;
        level11 = null;
        level12 = null;
        level13 = null;
        level14 = null;
        level15 = null;

    }

    public int levelNum = 0;
    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)){
            levelNum = (levelNum + 1) % 15;

        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)){
            levelNum = (levelNum + 14) % 15;

        }
    }
    private void draw() {

        canvas.begin();

        if (levelNum == 0){
            canvas.draw(level1, 0, 0);
            //System.out.println("drawing 1");
        }
        else if (levelNum == 1){
            canvas.draw(level2, 0, 0);
            //System.out.println("drawing 2");
        }
        else if (levelNum == 2){
            canvas.draw(level3, 0, 0);
            //System.out.println("drawing 3");
        }
        else if (levelNum == 3){
            canvas.draw(level4, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 4){
            canvas.draw(level5, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 5){
            canvas.draw(level6, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 6){
            canvas.draw(level7, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 7){
            canvas.draw(level8, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 8){
            canvas.draw(level9, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 9){
            canvas.draw(level10, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 10){
            canvas.draw(level11, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 11){
            canvas.draw(level12, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 12){
            canvas.draw(level13, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 13){
            canvas.draw(level14, 0, 0);
            //System.out.println("drawing 4");
        }
        else if (levelNum == 14){
            canvas.draw(level15, 0, 0);
            //System.out.println("drawing 4");
        }

        canvas.end();

    }

    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)|| Gdx.input.isKeyJustPressed(Input.Keys.P)){

                listener.exitScreen(this, 7);
            }
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

    private float scale;

    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/800;
        float sy = ((float)height)/700;
        scale = (sx < sy ? sx : sy);


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

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {

        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {

        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @param keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {

        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}
