package org.stransball;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.degreesToRadians;
import static com.badlogic.gdx.math.MathUtils.sin;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.FACTOR;
import static org.stransball.GameKeysStatus.bLeft;
import static org.stransball.GameKeysStatus.bRight;
import static org.stransball.GameKeysStatus.bThrust;

import java.io.FileNotFoundException;
import java.io.FileReader;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

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
    private int ship_state;
    private int ship_anim;
    private AtlasRegion shipRegion;
    private Sprite sprite;
    private Animation shipThrottleAnimation;
    private Sound shipSound;
    private float shipStateTime;
    private boolean playThrustSound;

    public WorldController() {
        map = new GameMap();
        try {
            map.load(new FileReader("maps/map10.map"));
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

        ship_state = 0;
        ship_anim = 0;

        {
            shipRegion = assets.shipAssets.shipRegion;

            sprite = new Sprite(shipRegion);
            sprite.setScale(0.5f, 0.5f);

            shipThrottleAnimation = assets.shipAssets.shipThrustAnimation;

            shipSound = assets.soundAssets.thrust;

            shipStateTime = 0.0f;

            assets.shipAssets.shipPolygon.setScale(0.5f, 0.5f);
        }
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
            ship_anim++;
            if (ship_anim >= 6)
                ship_anim = 1;
            //                         if (thrust_channel==-1 && S_thrust!=0)
            //thrust_channel=Mix_PlayChannel(-1,S_thrust,-1);
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

        if (ship_state == 0 && ship_map_collision()) {
            ship_speed_x /= 4;
            ship_speed_y /= 4;
            ship_state = 1;
            ship_anim = 0;
        } /* if */

    }

    private boolean ship_map_collision() {
        // TODO Auto-generated method stub

        if (true)
            return false;

        int x = ((ship_x / FACTOR) - 32);
        int y = ((ship_y / FACTOR) - 32);
        int sx = 64;
        int sy = 64;

        //map->draw_map(map_sfc,tiles_mask,d.x,d.y,64,64);
        map.drawWithoutEnemies(null, null, x, y, sx, sy, true);

        return false;
    }

    public void render(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer, boolean drawPoly) {
        renderMap(delta, batch, shapeRenderer, drawPoly);

        renderShip(delta, batch, shapeRenderer, drawPoly);
    }

    private void renderMap(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer, boolean drawPoly) {
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

        map.drawWithoutEnemies(batch, shapeRenderer, map_x, map_y, sx, sy, drawPoly);
    }

    private void renderShip(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer, boolean drawPoly) {
        int x = ((ship_x / Constants.FACTOR) /*- 32*/) - map_x;
        int y = (((ship_y / Constants.FACTOR) /*- 32*/)) - map_y;

        boolean bThrust = GameKeysStatus.bThrust;

        shipStateTime += delta;
        if (!bThrust) {
            sprite.setRegion(shipRegion);
            shipSound.stop();
            playThrustSound = false;
        } else {
            //TODO: my new code: sprite.setRegion(shipThrottleAnimation.getKeyFrame(shipStateTime));
            sprite.setRegion(shipThrottleAnimation.getKeyFrames()[ship_anim-1]);
            if (!playThrustSound) {
                shipSound.play();
                shipSound.loop();
                playThrustSound = true;
            }
        }

        sprite.setRotation(360 - ship_angle);
        sprite.setCenterX(x);
        sprite.setCenterY(Constants.INTERNAL_SCREEN_HEIGHT - y);

        if (!drawPoly) {
            sprite.draw(batch);

        } else {
            assets.shipAssets.shipPolygon.setRotation(360 - ship_angle);
            assets.shipAssets.shipPolygon.setPosition(x, Constants.INTERNAL_SCREEN_HEIGHT - y);

            shapeRenderer.polygon(assets.shipAssets.shipPolygon.getTransformedVertices());
        }
    }

}
