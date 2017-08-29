package edu.cornell.gdiac.physics.model;

/**
 * Created by Geehyun on 2017. 3. 4..
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;

import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.physics.platform.PlatformController;
import edu.cornell.gdiac.util.*;
import java.lang.reflect.Field;

public class RootObstacle extends PolygonObstacle {


    public static final int GROW_TOP_LEFT = 0;
    public static final int GROW_UP = 1;
    public static final int GROW_TOP_RIGHT = 2;
    public static final int GROW_LEFT = 7;
    public static final int GROW_RIGHT = 3;
    public static final int GROW_BOTTOM_LEFT = 6;
    public static final int GROW_DOWN = 5;
    public static final int GROW_BOTTOM_RIGHT = 4;

    public int direction;
    private Vector2 rootSize;
    float offset;

    public float[] locs;

    public RootObstacle previous;

    public int type;

    public int half;

    //Type
    public static final int NORMAL_PC = 0;
    public static final int JOINT = 1;
    public static final int ANCHOR = 2;

    //Half
    public static final int LEFT_PIECE = 0;
    public static final int RIGHT_PIECE = 1;

    public static final int SP_PIECE = 99;

    public RootObstacle (float[] points, float x, float y, float width, float height, int dir, RootObstacle prev, int type, int half) {
        super(points, x,y);
        this.locs = points;
        this.direction = dir;
        this.rootSize = new Vector2(width, height);
        this.offset = (width/4.0f) / (float) Math.sqrt(4.0f);
        this.previous = prev;
        this.type = type;
        this.half = half;
        setBodyType(BodyDef.BodyType.StaticBody);
        setName("Root part");
    }

    public float getAngle() {

        if (this.type == 1) {
            return super.getAngle();
        }
        else {
            if (direction == GROW_UP) {
                return MathUtils.PI/2.0f;
            }

            else if (direction == GROW_DOWN) {
                return -1 * MathUtils.PI/2.0f;
            }

            else if (direction == GROW_TOP_LEFT) {
                return 3 * MathUtils.PI/4.0f;
            }

            else if (direction == GROW_BOTTOM_RIGHT) {
                return -1*MathUtils.PI/4.0f;
            }

            else if (direction == GROW_RIGHT) {
                return 0.0f;
            }

            else if (direction == GROW_LEFT) {
                return MathUtils.PI;
            }

            else if (direction == GROW_TOP_RIGHT){
                return MathUtils.PI/4.0f;
            }
            else {
                return -3*MathUtils.PI/4.0f;
            }
        }
    }



    public Vector2 playerOffset(int dir, boolean right, float angle) {


        Vector2 v = new Vector2();
        float angleOff = getHeight()*1.6f - (float)Math.sin(angle+Math.PI/2)*getHeight()*1.6f;

        switch (dir) {
            case GROW_TOP_LEFT:
                v.set(-getWidth()/4, -getWidth()/3);
                break;
            case GROW_UP:
                if (right) v.set(getWidth() + getWidth()/8, angleOff -getWidth()/2);
                else v.set(-getWidth()/1.8f, angleOff -getWidth()/2);
                break;
            case GROW_TOP_RIGHT:
                v.set(getWidth()/2, angleOff -2*getWidth()/3);
                break;
            case GROW_LEFT:
                v.set(0, angleOff -getWidth()/9);
                break;
            case GROW_RIGHT:
                v.set(0, angleOff -2*getWidth()/3);
                break;
            case GROW_BOTTOM_LEFT:
                v.set(0, angleOff -getWidth()/4);
                break;
            case GROW_DOWN:
                v.set(-getWidth()/4, angleOff -getWidth()/4);
                break;
            case GROW_BOTTOM_RIGHT:
                v.set(-getWidth()/4, angleOff -getWidth()/2);
                break;
            default:
                break;
        }
        return v;
    }

    public Vector2 rootOffset(int dir) {
        Vector2 rootoff = new Vector2();

        if (this.direction == dir) {
            if (this.direction == GROW_RIGHT) {
                rootoff.set(getWidth(), 0);
            }
            else if (this.direction == GROW_TOP_RIGHT) {
                rootoff.set(getWidth()*0.7071f, getWidth()*0.7071f);
            }
            else if (this.direction == GROW_UP) {
                rootoff.set(0, getWidth());
            }
            else if (this.direction == GROW_TOP_LEFT) {
                rootoff.set(-getWidth()*0.7071f, getWidth()*0.7071f);
            }
            else if (this.direction == GROW_LEFT) {
                rootoff.set(-getWidth(), 0);
            }
            else if (this.direction == GROW_BOTTOM_LEFT) {
                rootoff.set(-getWidth()*0.7071f, -getWidth()*0.7071f);
            }
            else if (this.direction == GROW_DOWN) {
                rootoff.set(0, -getWidth());
            }
            else {
                rootoff.set(getWidth()*0.7071f, -getWidth()*0.7071f);
            }
        }
        else if (this.direction != dir) {
            if (this.direction == GROW_RIGHT) {
                rootoff.set(getWidth() + getHeight()/2, 0);
            }
            else if (this.direction == GROW_TOP_RIGHT) {
                rootoff.set(getWidth()*0.7071f + getHeight()/2*0.7071f, getWidth()*0.7071f + getHeight()/2*0.7071f);
            }
            else if (this.direction == GROW_UP) {
                rootoff.set(0, getWidth()+ getHeight()/2);
            }
            else if (this.direction == GROW_TOP_LEFT) {
                rootoff.set(-getWidth()*0.7071f - getHeight()/2*0.7071f, getWidth()*0.7071f + getHeight()/2*0.7071f);
            }
            else if (this.direction == GROW_LEFT) {
                rootoff.set(-getWidth() - getHeight()/2, 0);
            }
            else if (this.direction == GROW_BOTTOM_LEFT) {
                rootoff.set(-getWidth()*0.7071f- getHeight()/2*0.7071f, -getWidth()*0.7071f- getHeight()/2*0.7071f);
            }
            else if (this.direction == GROW_DOWN) {
                rootoff.set(0, -getWidth()- getHeight()/2);
            }
            else {
                rootoff.set(getWidth()*0.7071f + getHeight()/2*0.7071f, -getWidth()*0.7071f - getHeight()/2*0.7071f);
            }

        }

        if (dir - this.direction == 2 || dir - this.direction == 1 || dir - this.direction == -6 || dir - this.direction == -7) { // turning right
            if (this.type == ANCHOR) {
                rootoff.set(rootoff.x * 0.85f, rootoff.y * 0.85f);
            }
            else if (this.type != ANCHOR) { //Turning right
                rootoff.set(rootoff.x * 0.87f, rootoff.y * 0.87f);
            }

            if (dir - this.direction == 2 || dir - this.direction == -6) {
                if (dir == GROW_RIGHT) {
                    rootoff.set(rootoff.x - 0.5f, rootoff.y);
                }
                else if (dir == GROW_TOP_RIGHT) {
                    rootoff.set(rootoff.x - 0.5f*0.7071f, rootoff.y - 0.5f*0.7071f);
                }
                else if (dir == GROW_UP) {
                    rootoff.set(rootoff.x, rootoff.y - 0.5f);
                }
                else if (dir == GROW_TOP_LEFT) {
                    rootoff.set(rootoff.x + 0.5f*0.7071f, rootoff.y - 0.5f*0.7071f);
                }
                else if (dir == GROW_LEFT) {
                    rootoff.set(rootoff.x + 0.5f, rootoff.y);
                }
                else if (dir == GROW_BOTTOM_LEFT) {
                    rootoff.set(rootoff.x + 0.5f*0.7071f, rootoff.y + 0.5f*0.7071f);
                }
                else if (dir == GROW_DOWN) {
                    rootoff.set(rootoff.x, rootoff.y + 0.5f);
                }
                else if (dir == GROW_BOTTOM_RIGHT) {
                    rootoff.set(rootoff.x - 0.5f*0.7071f, rootoff.y + 0.5f*0.7071f);
                }
            }

        }
        else if (dir - this.direction == -2 || dir - this.direction == -1 || dir - this.direction == 6 || dir - this.direction == 7) { //turning left
            if (this.type == ANCHOR) { //Turning left
                rootoff.set(rootoff.x * 1.1f, rootoff.y * 1.1f);
            }
            else if (this.type != ANCHOR) { //Turning left
                rootoff.set(rootoff.x * 1.4f, rootoff.y * 1.4f);
            }

            if (dir - this.direction == -2 || dir - this.direction == 6) {
                if (dir == GROW_RIGHT) {
                    rootoff.set(rootoff.x + 0.5f, rootoff.y);
                }
                else if (dir == GROW_TOP_RIGHT) {
                    rootoff.set(rootoff.x + 0.5f*0.7071f, rootoff.y + 0.5f*0.7071f);
                }
                else if (dir == GROW_UP) {
                    rootoff.set(rootoff.x, rootoff.y + 0.5f);
                }
                else if (dir == GROW_TOP_LEFT) {
                    rootoff.set(rootoff.x - 0.5f*0.7071f, rootoff.y + 0.5f*0.7071f);
                }
                else if (dir == GROW_LEFT) {
                    rootoff.set(rootoff.x - 0.5f, rootoff.y);
                }
                else if (dir == GROW_BOTTOM_LEFT) {
                    rootoff.set(rootoff.x - 0.5f*0.7071f, rootoff.y - 0.5f*0.7071f);
                }
                else if (dir == GROW_DOWN) {
                    rootoff.set(rootoff.x, rootoff.y - 0.5f);
                }
                else if (dir == GROW_BOTTOM_RIGHT) {
                    rootoff.set(rootoff.x + 0.5f*0.7071f, rootoff.y - 0.5f*0.7071f);
                }
            }

        }


        return new Vector2(rootoff.x, rootoff.y);
    }


    public Vector2 jointOffset(int dir) {


        Vector2 jointoff = new Vector2();


//        if (this.direction == GROW_RIGHT) {
//            jointoff.set(getWidth() - rootSize.y/4, 0);
//        }
//        else if (this.direction == GROW_TOP_RIGHT) {
//            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/4);
//        }
//        else if (this.direction == GROW_UP) {
//            jointoff.set(0, getWidth() - rootSize.y/4);
//        }
//        else if (this.direction == GROW_TOP_LEFT) {
//            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/4);
//        }
//        else if (this.direction == GROW_LEFT) {
//            jointoff.set(-getWidth() + rootSize.y/4, 0);
//        }
//        else if (this.direction == GROW_BOTTOM_LEFT) {
//            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, -getWidth()*0.7071f + 0.7071f*rootSize.y/4);
//        }
//        else if (this.direction == GROW_DOWN) {
//            jointoff.set(0, -getWidth() + rootSize.y/4);
//        }
//        else {
//            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/4, -getWidth()*0.7071f + 0.7071f*rootSize.y/4);
//        }

        if (this.direction == GROW_RIGHT) {
            jointoff.set(getWidth() - rootSize.y/4, 0);
        }
        else if (this.direction == GROW_TOP_RIGHT) {
            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/4);
        }
        else if (this.direction == GROW_UP) {
            jointoff.set(0, getWidth() - rootSize.y/4);
        }
        else if (this.direction == GROW_TOP_LEFT) {
            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, getWidth()*0.7071f - 0.7071f*rootSize.y/4);
        }
        else if (this.direction == GROW_LEFT) {
            jointoff.set(-getWidth() + rootSize.y/4, 0);
        }
        else if (this.direction == GROW_BOTTOM_LEFT) {
            jointoff.set(-getWidth()*0.7071f + 0.7071f*rootSize.y/4, -getWidth()*0.7071f + 0.7071f*rootSize.y/4);
        }
        else if (this.direction == GROW_DOWN) {
            jointoff.set(0, -getWidth() + rootSize.y/4);
        }
        else {
            jointoff.set(getWidth()*0.7071f - 0.7071f*rootSize.y/4, -getWidth()*0.7071f + 0.7071f*rootSize.y/4);
        }


        return jointoff;


    }

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


        //adjust Coda's position based on angle to center it on the body, SUPER COMPLICATED
        canvas.draw(getTexture(), Color.WHITE, 0, 0, getX()*drawScale.x, getY()*drawScale.y, getAngle(), 1* PlatformController.worldX, 1 * PlatformController.worldY);

    }

}
