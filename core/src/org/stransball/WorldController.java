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

import org.stransball.util.CollisionDetectorUtils;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

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
    private boolean playThrustSound;
    private boolean collision;

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

            assets.shipAssets.shipPolygon.setScale(0.5f, 0.5f);
        }
    }

    public void update(float delta) {
        if (ship_state == 0) {
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
        } else if (ship_state == 1) {
            ship_anim++;
            //if (ship_anim>=64) fade_state=2;
            if (ship_anim >= 96)
                return;
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
        }
        if ((ship_y / Constants.FACTOR) < 0) {
            ship_y = 0;
            ship_speed_y = 0;
        }
        if ((ship_x / FACTOR) > (map.get_sx() * 16)) {
            ship_x = (map.get_sx() * 16) * FACTOR;
            ship_speed_x = 0;
        }
        if ((ship_y / FACTOR) > (map.get_sy() * 16)) {
            ship_y = (map.get_sy() * 16) * FACTOR;
            ship_speed_y = 0;
        }

        map.update(delta);

        if (ship_state == 0 && ship_map_collision(null)) {
            ship_speed_x /= 4;
            ship_speed_y /= 4;
            ship_state = 1;
            ship_anim = 0;
        }

    }

    private boolean ship_map_collision(final ShapeRenderer shapeRenderer) {
        final int x = ((ship_x / FACTOR) - 32);
        final int y = ((ship_y / FACTOR) - 32);
        int sx = 64;
        int sy = 64;

        final Polygon shipPolygon = assets.shipAssets.shipPolygon;
        final int ship_x_ = ((ship_x / Constants.FACTOR) /*- 32*/) - map_x;
        final int ship_y_ = (((ship_y / Constants.FACTOR) /*- 32*/)) - map_y;
        {
            shipPolygon.setRotation(360 - ship_angle);
            shipPolygon.setPosition(ship_x_, Constants.INTERNAL_SCREEN_HEIGHT - ship_y_);
        }

        final Polygon[] shipPolygons = CollisionDetectorUtils.tiangulate(shipPolygon);
        if (shapeRenderer != null) {
            //shapeRenderer.polygon(shipPolygon.getTransformedVertices());

            CollisionDetectorUtils.renderPolygons(shapeRenderer, shipPolygons);
        }

        collision = false;
        map.drawWithoutEnemies(null, null, x, y, sx, sy,
                new CollisionDetector(shapeRenderer, ship_x_, ship_y_, shipPolygons));

        return collision;
    }

    public void render(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer) {
        renderMap(delta, batch, shapeRenderer);

        renderShip(delta, batch, shapeRenderer);
    }

    private void renderMap(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer) {
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

        map.drawWithoutEnemies(batch, shapeRenderer, map_x, map_y, sx, sy, null);
    }

    private void renderShip(float delta, SpriteBatch batch, ShapeRenderer shapeRenderer) {

        if (ship_state == 0) {
            boolean bThrust = GameKeysStatus.bThrust;

            if (!bThrust) {
                sprite.setRegion(shipRegion);
                shipSound.stop();
                playThrustSound = false;
            } else {
                //TODO: my new code: sprite.setRegion(shipThrottleAnimation.getKeyFrame(shipStateTime));
                sprite.setRegion(shipThrottleAnimation.getKeyFrames()[ship_anim - 1]);
                if (!playThrustSound) {
                    shipSound.play();
                    shipSound.loop();
                    playThrustSound = true;
                }
            }

            int ship_x_ = ((ship_x / Constants.FACTOR)) - map_x;
            int ship_y_ = (((ship_y / Constants.FACTOR))) - map_y;

            if (batch != null) {
                sprite.setRotation(360 - ship_angle);
                sprite.setCenterX(ship_x_);
                sprite.setCenterY(Constants.INTERNAL_SCREEN_HEIGHT - ship_y_);
                sprite.draw(batch);
            }

            if (shapeRenderer != null) {
                Polygon shipPolygon = assets.shipAssets.shipPolygon;
                shipPolygon.setRotation(360 - ship_angle);
                shipPolygon.setPosition(ship_x_, Constants.INTERNAL_SCREEN_HEIGHT - ship_y_);
                shapeRenderer.polygon(shipPolygon.getTransformedVertices());
            }
        } else if (ship_state == 1) {
            int frame = ship_anim / 8;

            if (frame < 6) {
                sprite.setRegion(Assets.assets.shipAssets.shipExplosionAnimation.getKeyFrames()[frame]);

                int ship_x_ = ((ship_x / FACTOR)) - map_x;
                int ship_y_ = ((ship_y / FACTOR)) - map_y;

                sprite.setCenterX(ship_x_);
                sprite.setCenterY(Constants.INTERNAL_SCREEN_HEIGHT - ship_y_);

                sprite.setRotation(0);

                if (batch != null) {
                    sprite.draw(batch);
                }
            }
        }
    }

    private final class CollisionDetector implements IPolygonDetector {
        private final ShapeRenderer shapeRenderer;
        private final int ship_x_;
        private final int ship_y_;
        private final Polygon[] shipPolygons;

        private CollisionDetector(ShapeRenderer shapeRenderer, int ship_x_, int ship_y_, Polygon[] shipPolygons) {
            this.shapeRenderer = shapeRenderer;
            this.ship_x_ = ship_x_;
            this.ship_y_ = ship_y_;
            this.shipPolygons = shipPolygons;
        }

        @Override
        public void detect(int act_x, int act_y, int piece) {
            Polygon poly = Assets.assets.shipAssets.tilePolygons[piece];
            if (poly != null) {
                poly.setPosition(ship_x_ + act_x - 32, Constants.INTERNAL_SCREEN_HEIGHT - (ship_y_ + act_y - 32));

                Polygon[] poligons = CollisionDetectorUtils.tiangulate(poly);

                if (CollisionDetectorUtils.collide(shipPolygons, poligons)) {
                    if (shapeRenderer != null) {
                        shapeRenderer.setColor(Color.RED);
                    }
                    collision = true;
                } else {
                    if (shapeRenderer != null) {
                        shapeRenderer.setColor(Color.WHITE);
                    }
                }

                if (shapeRenderer != null) {
                    //shapeRenderer.polygon(poly.getTransformedVertices());
                    CollisionDetectorUtils.renderPolygons(shapeRenderer, poligons);
                }
            }
        }
    }

}
