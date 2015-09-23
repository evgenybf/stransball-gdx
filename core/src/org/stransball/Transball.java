package org.stransball;

import static java.lang.String.format;

import com.badlogic.gdx.Gdx;

public class Transball {

    private static final int FACTOR = 512;

    private int fuel_used;
    private int fuel;
    private int n_shots;
    private int n_hits;
    private int enemiesDestroyed;

    private int ship_angle;
    private int ship_speed_x;
    private int ship_speed_y;

    private int ship_x;

    private int ship_y;

    public Transball() {
        ship_x=FACTOR*124;
        ship_y=(124)*FACTOR;
        ship_angle=0;
        ship_speed_x=0;
        ship_speed_y=0;
        fuel = 1000;
    }

    public void cycle() {
        boolean bLeft = Gdx.input.isKeyPressed(Constants.LEFT_KEY);
        boolean bRight = Gdx.input.isKeyPressed(Constants.RIGHT_KEY);
        boolean bThrust = Gdx.input.isKeyPressed(Constants.THRUST_KEY);
//        boolean bAntiThrust = Gdx.input.isKeyPressed(GameConstants.ANTITHRUST_KEY);
//        boolean bAtractor = Gdx.input.isKeyPressed(GameConstants.ATRACTOR_KEY);

        if (bLeft) {
            ship_angle -= 4;
            if (ship_angle < 0)
                ship_angle += 360;
        }

        if (bRight) {
            ship_angle += 4;
            if (ship_angle > 360)
                ship_angle -= 360;
        }

        if (bThrust /*&& fuel > 0*/) {
            double radian_angle = Math.toRadians(ship_angle - 90.0f);
            ship_speed_x += (int) (Math.cos(radian_angle) * 18f);
            ship_speed_y += (int) (Math.sin(radian_angle) * 18f);
            if (ship_speed_x > 4 * FACTOR)
                ship_speed_x = 4 * FACTOR;
            if (ship_speed_x < -4 * FACTOR)
                ship_speed_x = -4 * FACTOR;
            if (ship_speed_y > 4 * FACTOR)
                ship_speed_y = 4 * FACTOR;
            if (ship_speed_y < -4 * FACTOR)
                ship_speed_y = -4 * FACTOR;
//            fuel--;
//            fuel_used++;
            //            shipAnim++;
            //            if (ship_anim>=6) ship_anim=1;
            //            if (thrust_channel==-1 && S_thrust!=0) thrust_channel=Mix_PlayChannel(-1,S_thrust,-1);
        }

        /* Ship cinematics: */
        if (ship_speed_x > 0)
            ship_speed_x--;
        if (ship_speed_x < 0)
            ship_speed_x++;
        ship_speed_y += 2;

        if (ship_speed_x > 4 * FACTOR)
            ship_speed_x = 4 * FACTOR;
        if (ship_speed_x < -4 * FACTOR)
            ship_speed_x = -4 * FACTOR;
        if (ship_speed_y > 4 * FACTOR)
            ship_speed_y = 4 * FACTOR;
        if (ship_speed_y < -4 * FACTOR)
            ship_speed_y = -4 * FACTOR;
        ship_x += ship_speed_x;
        ship_y += ship_speed_y;

        if ((ship_x / FACTOR) < 0) {
            ship_x = 0;
            ship_speed_x = 0;
        } /* if */
        if ((ship_y / FACTOR) < 0) {
            ship_y = 0;
            ship_speed_y = 0;
        } /* if */
        //        if ((ship_x/FACTOR)>(map->get_sx()*16)) {
        //            ship_x=(map->get_sx()*16)*FACTOR;
        //            ship_speed_x=0;
        //        } /* if */ 
        //        if ((ship_y/FACTOR)>(map->get_sy()*16)) {
        //            ship_y=(map->get_sy()*16)*FACTOR;
        //            ship_speed_y=0;
        //        } /* if */ 
    }

    public void render() {

    }

    public int getFuelUsed() {
        return fuel_used;
    }

    public int getFuel() {
        return fuel;
    }

    public int getnShots() {
        return n_shots;
    }

    public int getnHits() {
        return n_hits;
    }

    public int getEnemiesDestroyed() {
        return enemiesDestroyed;
    }

    public int getShipX() {
        return ((ship_x/FACTOR)-32);
    }

    public int getShipY() {
        return 320 - (((ship_y/FACTOR)-32));
    }

    public float getShipAngle() {
        return 360 - ship_angle;
    }

}
