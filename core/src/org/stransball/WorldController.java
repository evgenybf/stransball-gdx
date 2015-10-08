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

    private final GameMap map;

    private int shipXInternal; // x * FACTOR
    private int shipYInternal; // y * FACTOR
    private int shipSpeedX;
    private int shipSpeedY;
    private int shipAngle;
    private int shipState;
    private int mapXScreen;
    private int mapYScreen;
    private int shipAnim;
    private AtlasRegion shipRegion;
    private Sprite sprite;
    private Animation shipThrottleAnimation;
    private boolean playThrustSound;
    private List<ShipBullet> bullets;
    @SuppressWarnings("unused")
    private int enemiesDestroyedCount;
    private int ballXInternal; // x * FACTOR
    private int ballYInternal; // x * FACTOR
    private int ballSpeedX;
    private int ballSpeedY;
    private int ballState;
    private int shipAtractor;
    private int atractor_particles;
    int[] atractor_p_x, atractor_p_y;
    float[] atractor_p_speed;
    long[] atractor_p_color;
    @SuppressWarnings("unused")
    private int fade_state;

    public WorldController(GameMap map) {
        this.map = map;

        bullets = new ArrayList<ShipBullet>();

        shipXInternal = map.getCols() * 8 * FACTOR;
        shipYInternal = 32 * FACTOR;

        shipAngle = 0;
        shipSpeedX = 0;
        shipSpeedY = 0;

        shipState = 0;
        shipAnim = 0;

        fuel = /*fuelv*/50 * Constants.fuelfactor[0]; //TODO: have to depend on the map settings

        {
            shipRegion = assets.graphicAssets.shipRegion;

            sprite = new Sprite(shipRegion);
            sprite.setScale(0.5f, 0.5f);

            shipThrottleAnimation = assets.graphicAssets.shipThrustAnimation;

            assets.graphicAssets.shipPolygon.setScale(0.5f, 0.5f);
        }

        {
            int x = map.getBallPositionX();
            int y = map.getBallPositionY();

            ballState = -32;
            ballXInternal = x * 16 * FACTOR;
            ballYInternal = (y * 16 - 6) * FACTOR;
            ballSpeedX = 0;
            ballSpeedY = 0;
        }

        atractor_p_x = new int[MAX_ATRACTOR_P];
        atractor_p_y = new int[MAX_ATRACTOR_P];
        atractor_p_speed = new float[MAX_ATRACTOR_P];
        atractor_p_color = new long[MAX_ATRACTOR_P];
    }

    public void update(ShapeRenderer renderer) {
        if (shipState == 0) {
            if (bLeft) {
                shipAngle -= 4;
                if (shipAngle < 0)
                    shipAngle += 360;
            }

            if (bRight) {
                shipAngle += 4;
                if (shipAngle > 360)
                    shipAngle -= 360;
            }

            if (bThrust && fuel > 0) {
                float radian_angle = (shipAngle - 90.0f) * degreesToRadians;
                shipSpeedX += (int) (cos(radian_angle) * 18f);
                shipSpeedY += (int) (sin(radian_angle) * 18f);
                if (shipSpeedX > 4 * FACTOR)
                    shipSpeedX = 4 * FACTOR;
                if (shipSpeedX < -4 * FACTOR)
                    shipSpeedX = -4 * FACTOR;
                if (shipSpeedY > 4 * FACTOR)
                    shipSpeedY = 4 * FACTOR;
                if (shipSpeedY < -4 * FACTOR)
                    shipSpeedY = -4 * FACTOR;
                if (!Constants.DEBUG_GOD_MODE) {
                    fuel--;
                }
                fuel_used++;
                shipAnim++;
                if (shipAnim >= 6)
                    shipAnim = 1;

                if (!playThrustSound) {
                    assets.soundAssets.thrust.play();
                    assets.soundAssets.thrust.loop();
                    playThrustSound = true;
                }
            } else {
                shipAnim = 0;

                assets.soundAssets.thrust.stop();
                playThrustSound = false;
            }

            if (GameKeysStatus.bAtractor) {
                shipAtractor++;
                if (shipAtractor > 4)
                    shipAtractor = 1;

                if (atractor_particles < Constants.MAX_ATRACTOR_P) {
                    atractor_p_x[atractor_particles] = shipXInternal + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    atractor_p_y[atractor_particles] = shipYInternal + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    atractor_p_speed[atractor_particles] = (float) (5 + random(5 - 1)) / 10.0F;
                    atractor_p_color[atractor_particles] = 0;
                    atractor_particles++;
                }

            } else {
                shipAtractor = 0;
                if (atractor_particles > 0)
                    atractor_particles -= 8;
                if (atractor_particles < 0)
                    atractor_particles = 0;
            }

            for (int i = 0; i < atractor_particles; i++) {
                atractor_p_x[i] += (int) (shipSpeedX * 0.9f);
                atractor_p_y[i] += (int) (shipSpeedY * 0.9f);
                atractor_p_x[i] = (int) (shipXInternal * (1.0f - atractor_p_speed[i])
                        + atractor_p_x[i] * atractor_p_speed[i]);
                atractor_p_y[i] = (int) (shipYInternal * (1.0f - atractor_p_speed[i])
                        + atractor_p_y[i] * atractor_p_speed[i]);
                if ((Math.abs(shipXInternal - atractor_p_x[i]) < 2 * FACTOR)
                        && (Math.abs(shipYInternal - atractor_p_y[i]) < 2 * FACTOR)) {
                    atractor_p_x[i] = shipXInternal + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    atractor_p_y[i] = shipYInternal + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    atractor_p_speed[i] = (float) (5 + (random(5 - 1))) / 10.0F;
                    atractor_p_color[i] = 0;
                }
            }

            if (GameKeysStatus.isFire()) {
                float radian_angle = (shipAngle - 90) * MathUtils.degreesToRadians;

                n_shots++;
                if (!Constants.DEBUG_GOD_MODE) {
                    fuel -= Constants.shotfuel[0];
                }
                fuel_used += Constants.shotfuel[0];

                ShipBullet b = new ShipBullet();
                {
                    b.x = shipXInternal - 8 * FACTOR;
                    b.y = shipYInternal - 8 * FACTOR;
                    b.speed_x = (int) (MathUtils.cos(radian_angle) * 4 * FACTOR);
                    b.speed_y = (int) (MathUtils.sin(radian_angle) * 4 * FACTOR);
                    b.state = 0;
                }
                bullets.add(b);

                Assets.assets.soundAssets.shipshot.play();
            }

        } else if (shipState == 1) {
            assets.soundAssets.thrust.stop();
            playThrustSound = false;

            shipAnim++;
            if (shipAnim >= 64)
                fade_state = 2;
            if (shipAnim >= 96) {
                throw new RuntimeException("You failed");
            }
        }

        // Ship cinematics:
        if (shipSpeedX > 0)
            shipSpeedX--;
        if (shipSpeedX < 0)
            shipSpeedX++;
        shipSpeedY += 2;

        if (shipSpeedX > 4 * FACTOR)
            shipSpeedX = 4 * FACTOR;
        if (shipSpeedX < -4 * FACTOR)
            shipSpeedX = -4 * FACTOR;
        if (shipSpeedY > 4 * FACTOR)
            shipSpeedY = 4 * FACTOR;
        if (shipSpeedY < -4 * FACTOR)
            shipSpeedY = -4 * FACTOR;
        shipXInternal += shipSpeedX;
        shipYInternal += shipSpeedY;

        if ((shipXInternal / FACTOR) < 0) {
            shipXInternal = 0;
            shipSpeedX = 0;
        }
        if ((shipYInternal / FACTOR) < 0) {
            shipYInternal = 0;
            shipSpeedY = 0;
        }
        if ((shipXInternal / FACTOR) > (map.getCols() * 16)) {
            shipXInternal = (map.getCols() * 16) * FACTOR;
            shipSpeedX = 0;
        }
        if ((shipYInternal / FACTOR) > (map.getRows() * 16)) {
            shipYInternal = (map.getRows() * 16) * FACTOR;
            shipSpeedY = 0;
        }

        // Ball cinematics: 
        if (ballSpeedX > 0)
            ballSpeedX--;
        if (ballSpeedX < 0)
            ballSpeedX++;
        {
            int bx = (ballXInternal / FACTOR) + 8;
            int by = (ballYInternal / FACTOR) + 8;
            int sx = shipXInternal / FACTOR;
            int sy = (shipYInternal / FACTOR) + 8;

            if (shipAtractor != 0 && bx > sx - 8 && bx < sx + 8 && by > sy && by < sy + 32 && ballState < 0) {
                ballState++;
                if (ballState == 0) {
                    Assets.assets.soundAssets.takeball.play();
                    map.takeBall();
                }
            } else {
                if (ballState < 0)
                    ballState = -32;
            }

            if (ballState == 0) {
                int xdif = (ballXInternal / FACTOR) - (shipXInternal / FACTOR);
                int ydif = (ballYInternal / FACTOR) - (shipYInternal / FACTOR);
                int totdif;
                xdif *= xdif;
                ydif *= ydif;
                totdif = xdif + ydif;
                if (totdif < 10000) {
                    if ((shipXInternal - 8 * FACTOR) < ballXInternal)
                        ballSpeedX -= 2;
                    if ((shipXInternal - 8 * FACTOR) > ballXInternal)
                        ballSpeedX += 2;
                    if ((shipYInternal - 8 * FACTOR) < ballYInternal)
                        ballSpeedY -= 2;
                    if ((shipYInternal - 8 * FACTOR) > ballYInternal)
                        ballSpeedY += 2;
                }
                if (totdif < 4000) {
                    if ((shipXInternal - 8 * FACTOR) < ballXInternal)
                        ballSpeedX -= 2;
                    if ((shipXInternal - 8 * FACTOR) > ballXInternal)
                        ballSpeedX += 2;
                    if ((shipYInternal - 8 * FACTOR) < ballYInternal)
                        ballSpeedY -= 2;
                    if ((shipYInternal - 8 * FACTOR) > ballYInternal)
                        ballSpeedY += 2;
                }
                if (totdif < 1000) {
                    if ((shipXInternal - 8 * FACTOR) < ballXInternal)
                        ballSpeedX -= 2;
                    if ((shipXInternal - 8 * FACTOR) > ballXInternal)
                        ballSpeedX += 2;
                    if ((shipYInternal - 8 * FACTOR) < ballYInternal)
                        ballSpeedY -= 2;
                    if ((shipYInternal - 8 * FACTOR) > ballYInternal)
                        ballSpeedY += 2;
                }
                if (totdif < 100) {
                    if ((shipXInternal - 8 * FACTOR) < ballXInternal)
                        ballSpeedX -= 2;
                    if ((shipXInternal - 8 * FACTOR) > ballXInternal)
                        ballSpeedX += 2;
                    if ((shipYInternal - 8 * FACTOR) < ballYInternal)
                        ballSpeedY -= 2;
                    if ((shipYInternal - 8 * FACTOR) > ballYInternal)
                        ballSpeedY += 2;
                }
            }

            bx = ballXInternal; // ??? (ball_x / FACTOR);
            by = ballYInternal; // ??? (ball_y / FACTOR);

            int bx_ = ballXInternal / FACTOR; // ??? (ball_x / FACTOR);
            int by_ = ballYInternal / FACTOR; // ??? (ball_y / FACTOR);

            Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;
            if (checkPolygonWithMapCollision(null, tilePolygons[360], bx, by)) {
                if (ballSpeedY > 0) {
                    ballSpeedY = (int) (-0.75 * ballSpeedY);
                    map.collideBall(bx_ + 8, by_ + 12);
                } else {
                    if (checkPolygonWithMapCollision(null, tilePolygons[360], bx, by - 1))
                        ballSpeedY -= 2;
                }
            } else {
                ballSpeedY += 2;
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[340], bx, by)) {
                if (ballSpeedY < 0) {
                    ballSpeedY = (int) (-0.75 * ballSpeedY);
                    map.collideBall(bx_ + 8, by_ + 4);
                } else {
                    ballSpeedY += 2;
                }
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[342], bx, by)) {
                if (ballSpeedX > 0) {
                    ballSpeedX = (int) (-0.75 * ballSpeedX);
                    map.collideBall(bx_ + 12, by_ + 8);
                } else {
                    ballSpeedX -= 2;
                }
            }

            if (checkPolygonWithMapCollision(null, tilePolygons[362], bx, by)) {
                if (ballSpeedX < 0) {
                    ballSpeedX = (int) (-0.75 * ballSpeedX);
                    map.collideBall(bx_ + 4, by_ + 8);
                } else {
                    ballSpeedX += 2;
                }
            }
        }
        if (ballSpeedX > 4 * FACTOR)
            ballSpeedX = 4 * FACTOR;
        if (ballSpeedX < -4 * FACTOR)
            ballSpeedX = -4 * FACTOR;
        if (ballSpeedY > 4 * FACTOR)
            ballSpeedY = 4 * FACTOR;
        if (ballSpeedY < -4 * FACTOR)
            ballSpeedY = -4 * FACTOR;
        ballXInternal += ballSpeedX;
        ballYInternal += ballSpeedY;

        if ((ballXInternal / FACTOR) < 0) {
            ballXInternal = 0;
            ballSpeedX = 0;
        }
        if ((ballYInternal / FACTOR) < 0 && ballState >= 0) {
            fade_state = 2;
            ballSpeedY = -FACTOR;
            ballState++;
            if (ballState >= 32) {
                throw new RuntimeException("You win!");
            }
        }
        if ((ballXInternal / FACTOR) > ((map.getCols() - 1) * 16)) {
            ballXInternal = ((map.getCols() - 1) * 16) * FACTOR;
            ballSpeedX = 0;
        }
        if ((ballYInternal / FACTOR) > ((map.getRows() - 1) * 16)) {
            ballYInternal = ((map.getRows() - 1) * 16) * FACTOR;
            ballSpeedY = 0;
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
                        int retv = map.collideShipBullet((b.x / FACTOR) + 8, (b.y / FACTOR) + 8, 1);
                        if (retv != 0)
                            n_hits++;
                        if (retv == 2)
                            enemiesDestroyedCount++;
                    } else {
                        if (b.x < -8 * FACTOR || b.x > (map.getCols() * 16 * FACTOR) + 8 * FACTOR || b.y < -8 * FACTOR
                                || b.y > (map.getRows() * 16 * FACTOR) + 8 * FACTOR)
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

        map.update(shipXInternal, shipYInternal, mapXScreen, mapYScreen, renderer);

        if (!Constants.DEBUG_GOD_MODE) {
            // Ship collision detection 
            if (shipState == 0 && checkShipWithMapCollision(null)) {
                shipSpeedX /= 4;
                shipSpeedY /= 4;
                shipState = 1;
                shipAnim = 0;

                Assets.assets.soundAssets.explosion.play();
            }
        }

        // Fuel recharge: 
        if (map.isShipInFuelRecharge(shipXInternal / FACTOR, shipYInternal / FACTOR)) {
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

        int object_x = bx / FACTOR - mapXScreen;
        int object_y = by / FACTOR - mapYScreen;

        objectPolygon.setPosition(object_x, INTERNAL_SCREEN_HEIGHT - object_y);

        return map.checkCollision(object_x, object_y, x, y, sx, sy, objectPolygon, null, renderer);
    }

    private boolean checkShipWithMapCollision(ShapeRenderer renderer) {
        int x = shipXInternal / FACTOR - 32;
        int y = shipYInternal / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        Polygon objectPolygon = assets.graphicAssets.shipPolygon;

        int objectX = shipXInternal / FACTOR - mapXScreen;
        int objectY = shipYInternal / FACTOR - mapYScreen;

        objectPolygon.setRotation(360 - shipAngle);
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

            int x = atractor_p_x[i] / FACTOR - mapXScreen;
            int y = atractor_p_y[i] / FACTOR - mapYScreen;
            sprite.setCenter(x, Constants.INTERNAL_SCREEN_HEIGHT - y);

            sprite.draw(batch);
        }
    }

    private void renderBall(SpriteBatch batch) {
        if (batch == null)
            return;

        int tileIndex = (ballState < 0) ? 320 : 321;
        AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(tileIndex);

        Sprite sprite = new Sprite(tile);

        int x = ballXInternal / FACTOR - mapXScreen + 8; // FIXME: ball's coordinates returned by the Map are not correct!
        int y = ballYInternal / FACTOR - mapYScreen + 8;

        sprite.setCenter(x, INTERNAL_SCREEN_HEIGHT - y);
        sprite.draw(batch);
    }

    private void renderShipBullet(SpriteBatch batch) {
        if (batch == null)
            return;

        for (ShipBullet b : bullets) {
            int tileIndex = (b.state < 8) ? 242 : 399 + (b.state / 8);
            AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(tileIndex);

            int x = b.x / FACTOR - mapXScreen + 8; // FIXME: bullet's coordinates are not correct!
            int y = b.y / FACTOR - mapYScreen + 8;

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

        updateMapCameraPosition();

        map.render(batch, mapXScreen, mapYScreen, sx, sy);
    }

    private void updateMapCameraPosition() {
        int sx = INTERNAL_SCREEN_WIDTH;
        int sy = INTERNAL_SCREEN_HEIGHT;

        int dx = shipXInternal / FACTOR - INTERNAL_SCREEN_WIDTH / 2 - mapXScreen;
        int dy = shipYInternal / FACTOR - (int) (INTERNAL_SCREEN_HEIGHT / 2.4) - mapYScreen;

        if (dx > 8)
            dx = 8;
        else if (dx < -9)
            dx = -8;

        if (dy > 8)
            dy = 8;
        else if (dy < -9)
            dy = -8;

        mapXScreen += dx;
        mapYScreen += dy;

        if (mapXScreen > map.getCols() * 16 - sx)
            mapXScreen = map.getCols() * 16 - sx;
        else if (mapXScreen < 0)
            mapXScreen = 0;

        if (mapYScreen > map.getRows() * 16 - sy)
            mapYScreen = map.getRows() * 16 - sy;
        else if (mapYScreen < 0)
            mapYScreen = 0;
    }

    private void renderShip(SpriteBatch batch, ShapeRenderer renderer) {
        if (shipState == 0) {
            int shipXScreen = shipXInternal / FACTOR - mapXScreen;
            int shipYScreen = shipYInternal / FACTOR - mapYScreen;

            if (batch != null) {
                if (shipAnim == 0)
                    sprite.setRegion(shipRegion);
                else
                    sprite.setRegion(shipThrottleAnimation.getKeyFrames()[shipAnim - 1]);

                sprite.setRotation(360 - shipAngle);
                sprite.setCenter(shipXScreen, INTERNAL_SCREEN_HEIGHT - shipYScreen);
                sprite.draw(batch);
            }

            if (renderer != null) {
                Polygon shipPolygon = assets.graphicAssets.shipPolygon;
                shipPolygon.setRotation(360 - shipAngle);
                shipPolygon.setPosition(shipXScreen, INTERNAL_SCREEN_HEIGHT - shipYScreen);
                renderer.polygon(shipPolygon.getTransformedVertices());
            }
        } else if (shipState == 1) {
            int frame = shipAnim / 8;

            if (frame < 6) {
                sprite.setRegion(Assets.assets.graphicAssets.shipExplosionAnimation.getKeyFrames()[frame]);

                int shipXScreen = shipXInternal / FACTOR - mapXScreen;
                int shipYScreen = shipYInternal / FACTOR - mapYScreen;

                sprite.setCenter(shipXScreen, INTERNAL_SCREEN_HEIGHT - shipYScreen);
                sprite.setRotation(0);

                if (batch != null) {
                    sprite.draw(batch);
                }
            }
        }
    }

}
