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
import java.util.Arrays;
import java.util.List;

import org.stransball.objects.ShipBullet;
import org.stransball.util.CollisionDetectorUtils;

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
    private boolean collision;
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
    private int nstars;
    private int[] star_x;
    private int[] star_y;
    private int[] star_color;

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

        fuel = 1000;

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

        {
            nstars = map.get_sx() * 8;
            star_x = new int[nstars];
            star_y = new int[nstars];
            star_color = new int[nstars];

            for (int i = 0; i < nstars; i++) {
                star_color[i] = random(255 - 1);
                star_x[i] = random(map.get_sx() * 16 - 1);
                star_y[i] = random(25600 - 1);
                star_y[i] = 160 - (int) (Math.sqrt(star_y[i]));
            }
        }
    }

    public void update(float delta, ShapeRenderer debugRenderer) {
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
                fuel--;
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

            if (GameKeysStatus.bFire && !GameKeysStatus.bFirePrev /* && fuel>=shotfuel[ship_type] */) {
                float radian_angle = (ship_angle - 90) * MathUtils.degreesToRadians;

                n_shots++;
                //                fuel-=shotfuel[ship_type];
                //                fuel_used+=shotfuel[ship_type];

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
            //if (ship_anim>=64) fade_state=2;
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

            Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;
            if (tile_map_collision(debugRenderer, tilePolygons[360], bx, by)) {
                if (ball_speed_y > 0) {
                    ball_speed_y = (int) (-0.75 * ball_speed_y);
                    map.ball_collision(bx + 8, by + 12);
                } else {
                    if (tile_map_collision(debugRenderer, tilePolygons[360], bx, by - 1))
                        ball_speed_y -= 2;
                }
            } else {
                ball_speed_y += 2;
            }

            if (tile_map_collision(debugRenderer, tilePolygons[340], bx, by)) {
                if (ball_speed_y < 0) {
                    ball_speed_y = (int) (-0.75 * ball_speed_y);
                    map.ball_collision(bx + 8, by + 4);
                } else {
                    ball_speed_y += 2;
                }
            }

            if (tile_map_collision(debugRenderer, tilePolygons[342], bx, by)) {
                if (ball_speed_x > 0) {
                    ball_speed_x = (int) (-0.75 * ball_speed_x);
                    map.ball_collision(bx + 12, by + 8);
                } else {
                    ball_speed_x -= 2;
                }
            }

            if (tile_map_collision(debugRenderer, tilePolygons[362], bx, by)) {
                if (ball_speed_x < 0) {
                    ball_speed_x = (int) (-0.75 * ball_speed_x);
                    map.ball_collision(bx + 4, by + 8);
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
            //            fade_state=2;
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

                    if (tile_map_collision(null, Assets.assets.graphicAssets.tilePolygons[242], b.x, b.y)) {
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
            for (ShipBullet b0 : deletelist) {
                bullets.remove(b0);
            }
        }

        map.update(delta);

        // Ship collision detection 
        if (ship_state == 0 && ship_map_collision(null)) {
            ship_speed_x /= 4;
            ship_speed_y /= 4;
            ship_state = 1;
            ship_anim = 0;

            Assets.assets.soundAssets.explosion.play();
        }
    }

    private boolean tile_map_collision(final ShapeRenderer renderer, Polygon polygon, int bx, int by) {
        collision = false;

        int x = bx / FACTOR - 32;
        int y = by / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        int object_x_ = bx / FACTOR - map_x;
        int object_y_ = by / FACTOR - map_y;

        polygon.setPosition(object_x_, INTERNAL_SCREEN_HEIGHT - object_y_);
        Polygon[] shipPolygons = CollisionDetectorUtils.tiangulate(polygon);

        if (renderer != null) {
            renderer.polygon(polygon.getTransformedVertices());
//            CollisionDetectorUtils.drawPolygons(renderer, shipPolygons);
        }

        map.drawWithoutEnemies(null, null, x, y, sx, sy,
                new CollisionDetector(renderer, object_x_, object_y_, shipPolygons));

        return collision;
    }

    private boolean ship_map_collision(final ShapeRenderer renderer) {
        collision = false;

        int x = ship_x / FACTOR - 32;
        int y = ship_y / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        Polygon shipPolygon = assets.graphicAssets.shipPolygon;

        int ship_x_ = ship_x / FACTOR - map_x;
        int ship_y_ = ship_y / FACTOR - map_y;
        shipPolygon.setRotation(360 - ship_angle);
        shipPolygon.setPosition(ship_x_, INTERNAL_SCREEN_HEIGHT - ship_y_);

        Polygon[] shipPolygons = CollisionDetectorUtils.tiangulate(shipPolygon);
        if (renderer != null) {
            renderer.polygon(shipPolygon.getTransformedVertices());
        }

        map.drawWithoutEnemies(null, null, x, y, sx, sy,
                new CollisionDetector(renderer, ship_x_, ship_y_, shipPolygons));

        return collision;
    }

    public void render(SpriteBatch batch, ShapeRenderer renderer) {
        if (batch != null) {
            renerStars(batch);
            renderMap(batch, renderer);
            renderBall(batch);
            renderAttractor(batch);
            renderShip(batch, renderer);
            renderShipBullet(batch);
        }

//        if (renderer != null) {
//            int bx = ball_x; // ??? (ball_x / FACTOR);
//            int by = ball_y; // ??? (ball_y / FACTOR);
//
//            Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;
//            
//            tile_map_collision(renderer, tilePolygons[360], bx, by);
////            tile_map_collision(renderer, tilePolygons[360], bx, by - 1);
//            tile_map_collision(renderer, tilePolygons[340], bx, by);
//            tile_map_collision(renderer, tilePolygons[342], bx, by);
//            tile_map_collision(renderer, tilePolygons[362], bx, by);
//        }

    }

    private void renerStars(SpriteBatch batch) {
        int sx = INTERNAL_SCREEN_WIDTH;
        int sy = INTERNAL_SCREEN_HEIGHT;
        int i;

        for (i = 0; i < nstars; i++) {
            int x, y;
            x = star_x[i] - map_x / 2;
            y = star_y[i] - map_y / 2;
            if (x >= 0 && x < sx && y >= 0 && y < sy) {
                Sprite sprite = new Sprite(Assets.assets.graphicAssets.tiles.get(243));
                sprite.setScale(0.2f);
                sprite.setPosition(x, INTERNAL_SCREEN_HEIGHT - y);
                sprite.draw(batch, star_color[i] + star_color[i] + star_color[i]);
            }
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

        map.drawWithoutEnemies(batch, renderer, map_x, map_y, sx, sy, null);
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

    private final class CollisionDetector implements IPolygonDetector {
        private final ShapeRenderer renderer;
        private final int ship_x_;
        private final int ship_y_;
        private final Polygon[] shipPolygons;

        private CollisionDetector(ShapeRenderer renderer, int ship_x_, int ship_y_, Polygon[] shipPolygons) {
            this.renderer = renderer;
            this.ship_x_ = ship_x_;
            this.ship_y_ = ship_y_;
            this.shipPolygons = shipPolygons;
        }

        @Override
        public void detect(int act_x, int act_y, int piece) {
            Polygon poly = Assets.assets.graphicAssets.tilePolygons[piece];
            if (poly != null) {
                poly.setPosition(ship_x_ + act_x - 32, INTERNAL_SCREEN_HEIGHT - (ship_y_ + act_y - 32));

                Polygon[] polygons = CollisionDetectorUtils.tiangulate(poly);

                if (CollisionDetectorUtils.overlapPolygons(shipPolygons, polygons)) {
                    if (renderer != null) {
                        renderer.setColor(Color.RED);
                    }
                    collision = true;
                } else {
                    if (renderer != null) {
                        renderer.setColor(Color.WHITE);
                    }
                }

                if (renderer != null) {
                    renderer.polygon(poly.getTransformedVertices());
//                    CollisionDetectorUtils.drawPolygons(renderer, polygons);
                }
            }
        }
    }

}
