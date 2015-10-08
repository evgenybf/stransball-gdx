package org.stransball;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.degreesToRadians;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.sin;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.Constants.INTERNAL_SCREEN_WIDTH;
import static org.stransball.Constants.MAX_ATRACTOR_P;
import static org.stransball.GameKeysStatus.bLeft;
import static org.stransball.GameKeysStatus.bRight;
import static org.stransball.GameKeysStatus.bThrust;

import java.util.ArrayList;
import java.util.List;

import org.stransball.objects.ShipBullet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class WorldController {

    @SuppressWarnings("unused")
    private int fuel_used;
    private int fuel;
    @SuppressWarnings("unused")
    private int n_shots;
    @SuppressWarnings("unused")
    private int n_hits;
    @SuppressWarnings("unused")
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
    private boolean playThrustSound;
    private List<ShipBullet> bullets;
    @SuppressWarnings("unused")
    private int enemies_destroyed;
    private int ball_state;
    private int ball_x;
    private int ball_y;
    private int ship_atractor;
    private int atractor_particles;
    int[] atractor_p_x, atractor_p_y;
    float[] atractor_p_speed;
    long[] atractor_p_color;
    private int ball_speed_x;
    private int ball_speed_y;
    @SuppressWarnings("unused")
    private int fade_state;

    public WorldController(GameMap map) {
        this.map = map;

        bullets = new ArrayList<ShipBullet>();

        ship_x = (map.get_sx() * 8) * FACTOR;
        ship_y = (32) * FACTOR;

        ship_angle = 0;
        ship_speed_x = 0;
        ship_speed_y = 0;

        ship_state = 0;
        ship_anim = 0;

        fuel = /*fuelv*/50 * Constants.fuelfactor[0]; //TODO: have to depend on the map settings

        {
            shipRegion = assets.graphicAssets.shipRegion;

            sprite = new Sprite(shipRegion);
            sprite.setScale(0.5f, 0.5f);

            shipThrottleAnimation = assets.graphicAssets.shipThrustAnimation;

            assets.graphicAssets.shipPolygon.setScale(0.5f, 0.5f);
        }

        {
            int x = map.get_ball_position_x();
            int y = map.get_ball_position_y();

            ball_state = -32;
            ball_x = x * 16 * FACTOR;
            ball_y = (y * 16 - 6) * FACTOR;
            ball_speed_x = 0;
            ball_speed_y = 0;
        }

        atractor_p_x = new int[MAX_ATRACTOR_P];
        atractor_p_y = new int[MAX_ATRACTOR_P];
        atractor_p_speed = new float[MAX_ATRACTOR_P];
        atractor_p_color = new long[MAX_ATRACTOR_P];
    }

    public void update(ShapeRenderer renderer) {
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

            if (bThrust && fuel > 0) {
                float radian_angle = (ship_angle - 90.0f) * degreesToRadians;
                ship_speed_x += (int) (cos(radian_angle) * 18f);
                ship_speed_y += (int) (sin(radian_angle) * 18f);
                if (ship_speed_x > 4 * FACTOR)
                    ship_speed_x = 4 * FACTOR;
                if (ship_speed_x < -4 * FACTOR)
                    ship_speed_x = -4 * FACTOR;
                if (ship_speed_y > 4 * FACTOR)
                    ship_speed_y = 4 * FACTOR;
                if (ship_speed_y < -4 * FACTOR)
                    ship_speed_y = -4 * FACTOR;
                if (!Constants.DEBUG_GOD_MODE) {
                    fuel--;
                }
                fuel_used++;
                ship_anim++;
                if (ship_anim >= 6)
                    ship_anim = 1;

                if (!playThrustSound) {
                    assets.soundAssets.thrust.play();
                    assets.soundAssets.thrust.loop();
                    playThrustSound = true;
                }
            } else {
                ship_anim = 0;

                assets.soundAssets.thrust.stop();
                playThrustSound = false;
            }

            if (GameKeysStatus.bAtractor) {
                ship_atractor++;
                if (ship_atractor > 4)
                    ship_atractor = 1;

                if (atractor_particles < Constants.MAX_ATRACTOR_P) {
                    atractor_p_x[atractor_particles] = ship_x + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    atractor_p_y[atractor_particles] = ship_y + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    atractor_p_speed[atractor_particles] = (float) (5 + random(5 - 1)) / 10.0F;
                    atractor_p_color[atractor_particles] = 0;
                    atractor_particles++;
                }

            } else {
                ship_atractor = 0;
                if (atractor_particles > 0)
                    atractor_particles -= 8;
                if (atractor_particles < 0)
                    atractor_particles = 0;
            }

            for (int i = 0; i < atractor_particles; i++) {
                atractor_p_x[i] += (int) (ship_speed_x * 0.9f);
                atractor_p_y[i] += (int) (ship_speed_y * 0.9f);
                atractor_p_x[i] = (int) (ship_x * (1.0f - atractor_p_speed[i]) + atractor_p_x[i] * atractor_p_speed[i]);
                atractor_p_y[i] = (int) (ship_y * (1.0f - atractor_p_speed[i]) + atractor_p_y[i] * atractor_p_speed[i]);
                if ((Math.abs(ship_x - atractor_p_x[i]) < 2 * FACTOR)
                        && (Math.abs(ship_y - atractor_p_y[i]) < 2 * FACTOR)) {
                    atractor_p_x[i] = ship_x + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    atractor_p_y[i] = ship_y + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    atractor_p_speed[i] = (float) (5 + (random(5 - 1))) / 10.0F;
                    atractor_p_color[i] = 0;
                }
            }

            if (GameKeysStatus.isFire()) {
                float radian_angle = (ship_angle - 90) * MathUtils.degreesToRadians;

                n_shots++;
                if (!Constants.DEBUG_GOD_MODE) {
                    fuel -= Constants.shotfuel[0];
                }
                fuel_used += Constants.shotfuel[0];

                ShipBullet b = new ShipBullet();
                {
                    b.x = ship_x - 8 * FACTOR;
                    b.y = ship_y - 8 * FACTOR;
                    b.speed_x = (int) (MathUtils.cos(radian_angle) * 4 * FACTOR);
                    b.speed_y = (int) (MathUtils.sin(radian_angle) * 4 * FACTOR);
                    b.state = 0;
                }
                bullets.add(b);

                Assets.assets.soundAssets.shipshot.play();
            }

        } else if (ship_state == 1) {
            assets.soundAssets.thrust.stop();
            playThrustSound = false;

            ship_anim++;
            if (ship_anim >= 64)
                fade_state = 2;
            if (ship_anim >= 96) {
                throw new RuntimeException("You failed");
            }
        }

        // Ship cinematics:
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
        }
        if ((ship_y / FACTOR) < 0) {
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

        // Ball cinematics: 
        if (ball_speed_x > 0)
            ball_speed_x--;
        if (ball_speed_x < 0)
            ball_speed_x++;
        {
            int bx = (ball_x / FACTOR) + 8;
            int by = (ball_y / FACTOR) + 8;
            int sx = ship_x / FACTOR;
            int sy = (ship_y / FACTOR) + 8;

            if (ship_atractor != 0 && bx > sx - 8 && bx < sx + 8 && by > sy && by < sy + 32 && ball_state < 0) {
                ball_state++;
                if (ball_state == 0) {
                    Assets.assets.soundAssets.takeball.play();
                    map.ball_taken();
                }
            } else {
                if (ball_state < 0)
                    ball_state = -32;
            }

            if (ball_state == 0) {
                int xdif = (ball_x / FACTOR) - (ship_x / FACTOR);
                int ydif = (ball_y / FACTOR) - (ship_y / FACTOR);
                int totdif;
                xdif *= xdif;
                ydif *= ydif;
                totdif = xdif + ydif;
                if (totdif < 10000) {
                    if ((ship_x - 8 * FACTOR) < ball_x)
                        ball_speed_x -= 2;
                    if ((ship_x - 8 * FACTOR) > ball_x)
                        ball_speed_x += 2;
                    if ((ship_y - 8 * FACTOR) < ball_y)
                        ball_speed_y -= 2;
                    if ((ship_y - 8 * FACTOR) > ball_y)
                        ball_speed_y += 2;
                }
                if (totdif < 4000) {
                    if ((ship_x - 8 * FACTOR) < ball_x)
                        ball_speed_x -= 2;
                    if ((ship_x - 8 * FACTOR) > ball_x)
                        ball_speed_x += 2;
                    if ((ship_y - 8 * FACTOR) < ball_y)
                        ball_speed_y -= 2;
                    if ((ship_y - 8 * FACTOR) > ball_y)
                        ball_speed_y += 2;
                }
                if (totdif < 1000) {
                    if ((ship_x - 8 * FACTOR) < ball_x)
                        ball_speed_x -= 2;
                    if ((ship_x - 8 * FACTOR) > ball_x)
                        ball_speed_x += 2;
                    if ((ship_y - 8 * FACTOR) < ball_y)
                        ball_speed_y -= 2;
                    if ((ship_y - 8 * FACTOR) > ball_y)
                        ball_speed_y += 2;
                }
                if (totdif < 100) {
                    if ((ship_x - 8 * FACTOR) < ball_x)
                        ball_speed_x -= 2;
                    if ((ship_x - 8 * FACTOR) > ball_x)
                        ball_speed_x += 2;
                    if ((ship_y - 8 * FACTOR) < ball_y)
                        ball_speed_y -= 2;
                    if ((ship_y - 8 * FACTOR) > ball_y)
                        ball_speed_y += 2;
                }
            }

            bx = ball_x; // ??? (ball_x / FACTOR);
            by = ball_y; // ??? (ball_y / FACTOR);

            int bx_ = ball_x / FACTOR; // ??? (ball_x / FACTOR);
            int by_ = ball_y / FACTOR; // ??? (ball_y / FACTOR);

            Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;
            if (checkPolygonWithMapCollision(null, tilePolygons[360], bx, by)) {
                if (ball_speed_y > 0) {
                    ball_speed_y = (int) (-0.75 * ball_speed_y);
                    map.ball_collision(bx_ + 8, by_ + 12);
                } else {
                    if (checkPolygonWithMapCollision(null, tilePolygons[360], bx, by - 1))
                        ball_speed_y -= 2;
                }
            } else {
                ball_speed_y += 2;
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[340], bx, by)) {
                if (ball_speed_y < 0) {
                    ball_speed_y = (int) (-0.75 * ball_speed_y);
                    map.ball_collision(bx_ + 8, by_ + 4);
                } else {
                    ball_speed_y += 2;
                }
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[342], bx, by)) {
                if (ball_speed_x > 0) {
                    ball_speed_x = (int) (-0.75 * ball_speed_x);
                    map.ball_collision(bx_ + 12, by_ + 8);
                } else {
                    ball_speed_x -= 2;
                }
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[362], bx, by)) {
                if (ball_speed_x < 0) {
                    ball_speed_x = (int) (-0.75 * ball_speed_x);
                    map.ball_collision(bx_ + 4, by_ + 8);
                } else {
                    ball_speed_x += 2;
                }
            }
        }
        if (ball_speed_x > 4 * FACTOR)
            ball_speed_x = 4 * FACTOR;
        if (ball_speed_x < -4 * FACTOR)
            ball_speed_x = -4 * FACTOR;
        if (ball_speed_y > 4 * FACTOR)
            ball_speed_y = 4 * FACTOR;
        if (ball_speed_y < -4 * FACTOR)
            ball_speed_y = -4 * FACTOR;
        ball_x += ball_speed_x;
        ball_y += ball_speed_y;

        if ((ball_x / FACTOR) < 0) {
            ball_x = 0;
            ball_speed_x = 0;
        }
        if ((ball_y / FACTOR) < 0 && ball_state >= 0) {
            fade_state = 2;
            ball_speed_y = -FACTOR;
            ball_state++;
            if (ball_state >= 32) {
                throw new RuntimeException("You win!");
            }
        }
        if ((ball_x / FACTOR) > ((map.get_sx() - 1) * 16)) {
            ball_x = ((map.get_sx() - 1) * 16) * FACTOR;
            ball_speed_x = 0;
        }
        if ((ball_y / FACTOR) > ((map.get_sy() - 1) * 16)) {
            ball_y = ((map.get_sy() - 1) * 16) * FACTOR;
            ball_speed_y = 0;
        }

        // Bullets:
        {
            List<ShipBullet> deletelist = new ArrayList<ShipBullet>();

            for (ShipBullet b : bullets) {
                if (b.state == 0) {
                    b.x += b.speed_x;
                    b.y += b.speed_y;

                    if (checkPolygonWithMapCollision(null, Assets.assets.graphicAssets.tilePolygons[242], b.x, b.y)) {
                        // int ship_strength[]={1,2,4};
                        b.state++;
                        int retv = map.shipbullet_collision((b.x / FACTOR) + 8, (b.y / FACTOR) + 8, 1);
                        if (retv != 0)
                            n_hits++;
                        if (retv == 2)
                            enemies_destroyed++;
                    } else {
                        if (b.x < -8 * FACTOR || b.x > (map.get_sx() * 16 * FACTOR) + 8 * FACTOR || b.y < -8 * FACTOR
                                || b.y > (map.get_sy() * 16 * FACTOR) + 8 * FACTOR)
                            deletelist.add(b);
                    }
                } else {
                    b.state++;
                    if (b.state >= 40)
                        deletelist.add(b);
                }

            }

            bullets.removeAll(deletelist);
        }

        map.update(ship_x, ship_y, map_x, map_y, renderer);

        if (!Constants.DEBUG_GOD_MODE) {
            // Ship collision detection 
            if (ship_state == 0 && checkShipWithMapCollision(null)) {
                ship_speed_x /= 4;
                ship_speed_y /= 4;
                ship_state = 1;
                ship_anim = 0;

                Assets.assets.soundAssets.explosion.play();
            }
        }

        // Fuel recharge: 
        if (map.isShipInFuelRecharge(ship_x / FACTOR, ship_y / FACTOR)) {
            int old_fuel = fuel;
            fuel += 8;
            if (fuel > 50 * Constants.fuelfactor[0])
                fuel = 50 * Constants.fuelfactor[0];
            if ((old_fuel / Constants.fuelfactor[0]) < ((fuel - 1) / Constants.fuelfactor[0]))
                Assets.assets.soundAssets.fuel.play();
        }
    }

    private boolean checkPolygonWithMapCollision(final ShapeRenderer renderer, Polygon objectPolygon, int bx, int by) {
        int x = bx / FACTOR - 32;
        int y = by / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        int object_x = bx / FACTOR - map_x;
        int object_y = by / FACTOR - map_y;

        objectPolygon.setPosition(object_x, INTERNAL_SCREEN_HEIGHT - object_y);

        return map.checkCollision(object_x, object_y, x, y, sx, sy, objectPolygon, null, renderer);
    }

    private boolean checkShipWithMapCollision(ShapeRenderer renderer) {
        int x = ship_x / FACTOR - 32;
        int y = ship_y / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        Polygon objectPolygon = assets.graphicAssets.shipPolygon;

        int objectX = ship_x / FACTOR - map_x;
        int objectY = ship_y / FACTOR - map_y;

        objectPolygon.setRotation(360 - ship_angle);
        objectPolygon.setPosition(objectX, INTERNAL_SCREEN_HEIGHT - objectY);

        return map.checkCollision(objectX, objectY, x, y, sx, sy, objectPolygon, null, renderer);
    }

    public void render(SpriteBatch batch, ShapeRenderer renderer) {
        if (batch != null) {
            renderMap(batch, renderer);
            renderBall(batch);
            renderAttractor(batch);
            renderShip(batch, renderer);
            renderShipBullet(batch);

            drawFuelStatus(batch);
        }
    }

    private void drawFuelStatus(SpriteBatch batch) {
        {
            Sprite sprite = new Sprite(Assets.assets.graphicAssets.whiteSpot); //Assets.assets.graphicAssets.tiles.get(498));
            sprite.setPosition(2, INTERNAL_SCREEN_HEIGHT - 2);
            sprite.setSize(52, 1);
            sprite.setColor(Color.WHITE);
            sprite.draw(batch);

            sprite.setY(INTERNAL_SCREEN_HEIGHT - 9);
            sprite.draw(batch);

            sprite.setY(INTERNAL_SCREEN_HEIGHT - 9);
            sprite.setSize(1, 8);
            sprite.draw(batch);

            sprite.setX(53);
            sprite.draw(batch);

            sprite.setPosition(3, INTERNAL_SCREEN_HEIGHT - 8);
            sprite.setSize(fuel / 64.0f, 6);

            float f = (float) (fuel) / (64.0f * 30.0F);
            if (f >= 1.0F)
                f = 1.0F;

            sprite.setColor(new Color((int) (255 * (1 - f * f)), (int) (200 * Math.sqrt(f)), 0, 1));
            sprite.draw(batch);
        }
    }

    private void renderAttractor(SpriteBatch batch) {
        if (batch == null)
            return;

        AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(242);
        Sprite sprite = new Sprite(tile);

        for (int i = 0; i < atractor_particles; i++) {
            if (atractor_p_color[i] == 0) {
                int v = (random(192 - 1)) + 64;
                atractor_p_color[i] = v;
            }

            sprite.setScale(0.4f, 0.4f);
            sprite.setAlpha(255.0f / atractor_p_color[i]);

            int x = (atractor_p_x[i] / FACTOR) - map_x;
            int y = (atractor_p_y[i] / FACTOR) - map_y;
            sprite.setCenter(x, Constants.INTERNAL_SCREEN_HEIGHT - y);

            sprite.draw(batch);
        }
    }

    private void renderBall(SpriteBatch batch) {
        if (batch == null)
            return;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        AtlasRegion tile;

        if (ball_state < 0)
            tile = tiles.get(320);
        else
            tile = tiles.get(321);

        Sprite sprite = new Sprite(tile);

        int x = (ball_x / FACTOR) - map_x + 8; // FIXME: ball's coordinates returned by the Map are not correct!
        int y = (ball_y / FACTOR) - map_y + 8;

        sprite.setCenter(x, INTERNAL_SCREEN_HEIGHT - y);
        sprite.draw(batch);
    }

    private void renderShipBullet(SpriteBatch batch) {
        if (batch == null)
            return;

        for (ShipBullet b : bullets) {
            AtlasRegion tile;
            if (b.state < 8)
                tile = Assets.assets.graphicAssets.tiles.get(242);
            else
                tile = Assets.assets.graphicAssets.tiles.get(399 + (b.state / 8));

            int x = b.x / FACTOR - map_x + 8; // FIXME: bullet's coordinates are not correct!
            int y = b.y / FACTOR - map_y + 8;

            Sprite sprite = new Sprite(tile);
            sprite.setCenter(x, INTERNAL_SCREEN_HEIGHT - y);
            sprite.draw(batch);
        }
    }

    private void renderMap(SpriteBatch batch, ShapeRenderer renderer) {
        if (batch == null)
            return;

        int sx = INTERNAL_SCREEN_WIDTH;
        int sy = INTERNAL_SCREEN_HEIGHT;

        int dx = ((ship_x / FACTOR) - (INTERNAL_SCREEN_WIDTH / 2)) - map_x;
        int dy = ((ship_y / FACTOR) - (int) (INTERNAL_SCREEN_HEIGHT / 2.4)) - map_y;

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

        map.drawMap(batch, map_x, map_y, sx, sy, null);
        map.drawEnemies(batch, map_x, map_y, sx, sy, null, null);
    }

    private void renderShip(SpriteBatch batch, ShapeRenderer renderer) {
        if (ship_state == 0) {
            int ship_x_ = ship_x / FACTOR - map_x;
            int ship_y_ = ship_y / FACTOR - map_y;

            if (batch != null) {
                if (ship_anim == 0)
                    sprite.setRegion(shipRegion);
                else
                    sprite.setRegion(shipThrottleAnimation.getKeyFrames()[ship_anim - 1]);

                sprite.setRotation(360 - ship_angle);
                sprite.setCenter(ship_x_, INTERNAL_SCREEN_HEIGHT - ship_y_);
                sprite.draw(batch);
            }

            if (renderer != null) {
                Polygon shipPolygon = assets.graphicAssets.shipPolygon;
                shipPolygon.setRotation(360 - ship_angle);
                shipPolygon.setPosition(ship_x_, INTERNAL_SCREEN_HEIGHT - ship_y_);
                renderer.polygon(shipPolygon.getTransformedVertices());
            }
        } else if (ship_state == 1) {
            int frame = ship_anim / 8;

            if (frame < 6) {
                sprite.setRegion(Assets.assets.graphicAssets.shipExplosionAnimation.getKeyFrames()[frame]);

                int ship_x_ = ship_x / FACTOR - map_x;
                int ship_y_ = ship_y / FACTOR - map_y;

                sprite.setCenter(ship_x_, INTERNAL_SCREEN_HEIGHT - ship_y_);

                sprite.setRotation(0);

                if (batch != null) {
                    sprite.draw(batch);
                }
            }
        }
    }

}
