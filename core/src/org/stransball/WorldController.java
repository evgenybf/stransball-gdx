package org.stransball;

import static com.badlogic.gdx.math.MathUtils.*;
import static org.stransball.Constants.FACTOR;
import static org.stransball.GameKeysStatus.*;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WorldController {

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

    private final GameMap map;
    private int map_x;
    private int map_y;

    public WorldController() {
        map = new GameMap();
        try {
            map.load(new FileReader("maps/map12.map"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ship_x = Constants.FACTOR * 124;
        ship_y = (124) * Constants.FACTOR;
        ship_angle = 0;
        ship_speed_x = 0;
        ship_speed_y = 0;
        fuel = 1000;
    }

    public void update(float delta) {
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

        if (bThrust /* && fuel > 0 */) {
            float radian_angle = (ship_angle - 90.0f) * degreesToRadians;
            ship_speed_x += (int) (cos(radian_angle) * 18f);
            ship_speed_y += (int) (sin(radian_angle) * 18f);
            if (ship_speed_x > 4 * Constants.FACTOR)
                ship_speed_x = 4 * Constants.FACTOR;
            if (ship_speed_x < -4 * Constants.FACTOR)
                ship_speed_x = -4 * Constants.FACTOR;
            if (ship_speed_y > 4 * Constants.FACTOR)
                ship_speed_y = 4 * Constants.FACTOR;
            if (ship_speed_y < -4 * Constants.FACTOR)
                ship_speed_y = -4 * Constants.FACTOR;
             fuel--;
             fuel_used++;
//             shipAnim++;
//             if (ship_anim>=6) ship_anim=1;
//             if (thrust_channel==-1 && S_thrust!=0)
//             thrust_channel=Mix_PlayChannel(-1,S_thrust,-1);
        }

        /* Ship cinematics: */
        if (ship_speed_x > 0)
            ship_speed_x--;
        if (ship_speed_x < 0)
            ship_speed_x++;
        ship_speed_y += 2;

        if (ship_speed_x > 4 * Constants.FACTOR)
            ship_speed_x = 4 * Constants.FACTOR;
        if (ship_speed_x < -4 * Constants.FACTOR)
            ship_speed_x = -4 * Constants.FACTOR;
        if (ship_speed_y > 4 * Constants.FACTOR)
            ship_speed_y = 4 * Constants.FACTOR;
        if (ship_speed_y < -4 * Constants.FACTOR)
            ship_speed_y = -4 * Constants.FACTOR;
        ship_x += ship_speed_x;
        ship_y += ship_speed_y;

        if ((ship_x / Constants.FACTOR) < 0) {
            ship_x = 0;
            ship_speed_x = 0;
        } /* if */
        if ((ship_y / Constants.FACTOR) < 0) {
            ship_y = 0;
            ship_speed_y = 0;
        } /* if */
        if ((ship_x / FACTOR) > (map.get_sx() * 16)) {
            ship_x = (map.get_sx() * 16) * FACTOR;
            ship_speed_x = 0;
        } /* if */
        if ((ship_y / FACTOR) > (map.get_sy() * 16)) {
            ship_y = (map.get_sy() * 16) * FACTOR;
            ship_speed_y = 0;
        } /* if */

        map.update(delta);
    }

    public void render(float delta, SpriteBatch batch) {
        int sx = Constants.INTERNAL_SCREEN_WIDTH;
        int sy = Constants.INTERNAL_SCREEN_HEIGHT;

        int dx = ((ship_x / FACTOR) - (Constants.INTERNAL_SCREEN_WIDTH / 2)) - map_x;
        int dy = ((ship_y / FACTOR) - (int) (Constants.INTERNAL_SCREEN_HEIGHT / 2.4)) - map_y;

        if (dx > 8)
            dx = 8;
        if (dx < -9)
            dx = -8;
        if (dy > 8)
            dy = 8;
        if (dy < -9)
            dy = -8;

        map_x += dx;
        map_y += dy;

        if (map_x > ((map.get_sx() * 16) - sx))
            map_x = ((map.get_sx() * 16) - sx);
        if (map_x < 0)
            map_x = 0;

        if (map_y > ((map.get_sy() * 16) - sy))
            map_y = ((map.get_sy() * 16) - sy);
        if (map_y < 0)
            map_y = 0;

        map.drawWithoutEnemies(batch, map_x, map_y, sx, sy);
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

    public int getShipXToDraw() {
        return ((ship_x / Constants.FACTOR) /*- 32*/) - map_x;
    }

    public int getShipYToDraw() {
        return (((ship_y / Constants.FACTOR) /*- 32*/)) - map_y;
    }

    public float getShipAngle() {
        return ship_angle;
    }

}
