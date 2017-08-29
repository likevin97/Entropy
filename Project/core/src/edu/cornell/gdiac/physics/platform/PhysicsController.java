package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.physics.model.*;

/**
 * Created by jjermanok on 3/29/17.
 */
public class PhysicsController {

    World world;
    PlayerModel avatar;

    boolean hitGround;
    Vector2 o = new Vector2(0.0f, 0.0f);

    public PhysicsController(World w, PlayerModel a) {
        world = w;
        avatar = a;
    }
    private Fixture closestFixture;
    private Vector2 closestPoint;

    RayCastCallback r = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            //System.out.println(normal.angleRad()- (float)Math.PI/2);
            Obstacle hit = (Obstacle)fixture.getBody().getUserData();
            if (fixture.isSensor()) return -1;
            if (hit == null) return 0.0f;
            float angle = normal.angleRad() - (float)Math.PI/2;

            closestFixture = fixture;
            closestPoint = point;
			/*if (fraction < smallestFrac) {
				smallestFrac = fraction;
				bestAngle = angle;
			}*/
            //System.out.println(hit.getName());
            if(hit.getName().equals("Root part") || hit.getName().equals("vine")) {
                //avatar.setAngle(angle);
                world.setGravity(new Vector2((float) Math.cos(normal.angleRad()) * -14.7f * 0.1f, (float) Math.sin(normal.angleRad()) * -14.7f * 0.3f));
                //System.out.println("1 new gravity set");
                avatar.rotate(avatar.getAngle(),angle);

            }
            return fraction;
        }
    };

    RayCastCallback j = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            //System.out.println(normal.angleRad()- (float)Math.PI/2);
            //float angle = normal.angleRad() - (float)Math.PI/2;
            hitGround = true;
            if (fixture.isSensor()) return -1;

            closestFixture = fixture;
            closestPoint = point;
			/*if (fraction < smallestFrac) {
				smallestFrac = fraction;
				bestAngle = angle;
			}*/
            //avatar.setAngle(angle);
            //world.setGravity(new Vector2((float) Math.cos(normal.angleRad()) * -14.7f, (float) Math.sin(normal.angleRad()) * -14.7f));

            return fraction;
        }
    };


    private Vector2 gravity = new Vector2(0.0f, -14.7f);

    public void updatePhysics() {

        hitGround = false;

        //o = new Vector2(avatar.getX() + avatar.getWidth() / 2, avatar.getY() + avatar.getHeight() / 2);
        if (avatar.isFacingRight()) {
//            o = new Vector2(avatar.getX() + (float)Math.cos(avatar.getAngle())*avatar.getWidth() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getHeight() / 2, avatar.getY() + (float)Math.cos(avatar.getAngle())*avatar.getHeight() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getWidth() / 2);

            o.set(avatar.getX() + (float)Math.cos(avatar.getAngle())*avatar.getWidth() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getHeight() / 2,
                    avatar.getY() + (float)Math.cos(avatar.getAngle())*avatar.getHeight() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getWidth() / 2);
            world.rayCast(r,o.x,o.y,o.x+(float)Math.cos(avatar.getAngle())*avatar.getWidth()/2,o.y+(float)Math.sin(avatar.getAngle())*avatar.getWidth()/2); //right

        }
        else {
//            o = new Vector2(avatar.getX() - (float)Math.cos(avatar.getAngle())*avatar.getWidth() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getHeight() / 2, avatar.getY() + (float)Math.cos(avatar.getAngle())*avatar.getHeight() / 2 - (float)Math.sin(avatar.getAngle())*avatar.getWidth() / 2);
            o.set(avatar.getX() - (float)Math.cos(avatar.getAngle())*avatar.getWidth() / 2 + (float)Math.sin(avatar.getAngle())*avatar.getHeight() / 2,
                    avatar.getY() + (float)Math.cos(avatar.getAngle())*avatar.getHeight() / 2 - (float)Math.sin(avatar.getAngle())*avatar.getWidth() / 2);
            world.rayCast(r,o.x,o.y,o.x-(float)Math.cos(avatar.getAngle())*avatar.getWidth()/2,o.y-(float)Math.sin(avatar.getAngle())*avatar.getWidth()/2); //left

        }

        //world.rayCast(r,o.x,o.y,o.x+(float)Math.sin(avatar.getAngle())*avatar.getWidth()/2,o.y+(float)Math.cos(avatar.getAngle())*avatar.getWidth()/2); //up
        //world.rayCast(r,o.x,o.y,o.x-(float)Math.sin(avatar.getAngle())*avatar.getWidth()/2,o.y-(float)Math.cos(avatar.getAngle())*avatar.getWidth()/2); //down
        //world.rayCast(r,o.x,o.y,o.x+avatar.getWidth()/2,o.y+avatar.getHeight()/2); //NE
        //world.rayCast(r,o.x,o.y,o.x-avatar.getWidth()/2,o.y+avatar.getHeight()/2); //NW
        //world.rayCast(r,o.x,o.y,o.x+avatar.getWidth()/2,o.y-avatar.getHeight()/2); //SE
        //world.rayCast(r,o.x,o.y,o.x-avatar.getWidth()/2,o.y-avatar.getHeight()/2); //SW
        world.rayCast(j,o.x,o.y,o.x+(float)Math.sin(avatar.getAngle())*avatar.getWidth()/2,o.y-(float)Math.cos(avatar.getAngle())*avatar.getWidth()/2); //down

        //avatar.setAngle(bestAngle);
        //System.out.println(bestAngle);
        //world.setGravity(new Vector2((float) Math.cos(bestAngle + (float)Math.PI/2) * -14.7f, (float) Math.sin(bestAngle + (float)Math.PI/2) * -14.7f));
        //world.setGravity(gravity);


        if (!hitGround && !(avatar.getAngle() > Math.PI/4-0.1 && avatar.getAngle() < Math.PI/4+0.1 ) &&
            !(avatar.getAngle() > 3*Math.PI/4-0.1 && avatar.getAngle() < 3*Math.PI/4+0.1 ) &&
            !(avatar.getAngle() > 5*Math.PI/4-0.1 && avatar.getAngle() < 5*Math.PI/4+0.1 ) &&
            !(avatar.getAngle() > 7*Math.PI/4-0.1 && avatar.getAngle() < 7*Math.PI/4+0.1 )) {
                if (avatar.isFacingRight()) {
                    avatar.rotate(avatar.getAngle(), avatar.getAngle() - (float) Math.PI / 2);
                }
                else {
                    avatar.rotate(avatar.getAngle(), avatar.getAngle() + (float) Math.PI / 2);
                }
        }

        if(!avatar.isGrounded() && !world.getGravity().equals(gravity)) {
            world.setGravity(gravity);
            //System.out.println("2 New gravity set");
        }
        avatar.applyForce();

    }

}
