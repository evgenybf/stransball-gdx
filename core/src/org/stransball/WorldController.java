package org.stransball;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.degreesToRadians;
import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.sin;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.DEBUG_SHOW_BALL_COLLISION;
import static org.stransball.Constants.DEBUG_SHOW_BULLET_COLLISION;
import static org.stransball.Constants.DEBUG_SHOW_ENEMY_COLLISION;
import static org.stransball.Constants.DEBUG_SHOW_SHIP_COLLISION;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.Constants.INTERNAL_SCREEN_WIDTH;
import static org.stransball.Constants.MAX_ATRACTOR_P;
import static org.stransball.GameKeysStatus.bLeft;
import static org.stransball.GameKeysStatus.bRight;
import static org.stransball.GameKeysStatus.bThrust;
import static org.stransball.util.DebugUtils.passDebugRenderer;

import java.util.ArrayList;
import java.util.List;

import org.stransball.objects.ShipBullet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public class WorldController {

    public enum ShipState {
        NORMAL, EXPLODED
    }

    private final GameMap map;

    private int mapXScreen;
    private int mapYScreen;

    private int shipXInternal; // xInternal = xScreenF * FACTOR
    private int shipYInternal; // yInternal = yScreenF * FACTOR
    private int shipSpeedX;
    private int shipSpeedY;
    private int shipAngle;
    private int shipFuel;
    private ShipState shipState;
    private int shipAnim;
    @SuppressWarnings("unused")
    private int shipFuelUsed;
    @SuppressWarnings("unused")
    private int shipShotsCount;
    @SuppressWarnings("unused")
    private int shipHitsCount;
    @SuppressWarnings("unused")
    private int shipEnemiesDestroyedCount;
    private AtlasRegion shipRegion;
    private Sprite shipSprite;
    private boolean shipPlayThrustSound;

    private int atractorParticlesCount;
    private int attractorParticles;
    private int[] attractorPX, attractorPY;
    private float[] attractorPSpeed;
    private long[] attractorPColor;

    private List<ShipBullet> bullets;

    private int ballXInternal; // xInternal = xScreenF * FACTOR
    private int ballYInternal; // yInternal = yScreenF * FACTOR
    private int ballSpeedX;
    private int ballSpeedY;
    private int ballMagnetisationLevel;

    @SuppressWarnings("unused")
    private int fadingState;

    public WorldController(GameMap map) {
        this.map = map;

        bullets = new ArrayList<ShipBullet>();

        shipXInternal = map.getCols() * 8 * FACTOR;
        shipYInternal = 32 * FACTOR;

        shipAngle = 0;
        shipSpeedX = 0;
        shipSpeedY = 0;

        shipState = ShipState.NORMAL;
        shipAnim = 0;

        shipFuel = /* fuelv */50 * Constants.fuelfactor[0]; // TODO: has to depend on the map settings

        {
            shipRegion = assets.graphicAssets.shipRegion;

            shipSprite = new Sprite(shipRegion);
            shipSprite.setScale(0.5f, 0.5f);

            Polygon shipPolygon = assets.graphicAssets.shipPolygon;
            shipPolygon.setOrigin(shipSprite.getWidth() / 2.0f, shipSprite.getHeight() / 2.0f);
            shipPolygon.setScale(0.5f, 0.5f);
        }

        {
            int ballCol = map.getBallPositionX();
            int ballRow = map.getBallPositionY();

            ballMagnetisationLevel = -32;
            ballXInternal = ballCol * 16 * FACTOR;
            ballYInternal = (ballRow * 16 - 6) * FACTOR;
            ballSpeedX = 0;
            ballSpeedY = 0;
        }

        attractorPX = new int[MAX_ATRACTOR_P];
        attractorPY = new int[MAX_ATRACTOR_P];
        attractorPSpeed = new float[MAX_ATRACTOR_P];
        attractorPColor = new long[MAX_ATRACTOR_P];
    }

    public void update(ShapeRenderer renderer) {
        updateShip();
        updateShipPositionByGravity();

        updateBall(passDebugRenderer(renderer, DEBUG_SHOW_BALL_COLLISION));
        updateBallPositionByGravity();

        updateBullets(passDebugRenderer(renderer, DEBUG_SHOW_BULLET_COLLISION));

        updateMapCameraPosition();
        map.update(shipXInternal, shipYInternal, shipSpeedX, shipSpeedY, mapXScreen, mapYScreen,
                passDebugRenderer(renderer, DEBUG_SHOW_ENEMY_COLLISION));

        // Ship collision detection
        if (shipState == ShipState.NORMAL
                && checkCollisionOfShipAndMap(passDebugRenderer(renderer, DEBUG_SHOW_SHIP_COLLISION))) {
            if (!Constants.DEBUG_GOD_MODE) {
                shipSpeedX /= 4;
                shipSpeedY /= 4;
                shipState = ShipState.EXPLODED;
                shipAnim = 0;

                Assets.assets.soundAssets.explosion.play();
            }
        }

        int shipXScreenF = shipXInternal / FACTOR;
        int shipYScreenF = shipYInternal / FACTOR;
        if (map.isShipInFuelRecharge(shipXScreenF, shipYScreenF)) {
            rechargeShipFuel();
        }
    }

    private void rechargeShipFuel() {
        int prevShipFuel = shipFuel;

        shipFuel += 8;
        if (shipFuel > 50 * Constants.fuelfactor[0]) {
            shipFuel = 50 * Constants.fuelfactor[0];
        }

        if (prevShipFuel / Constants.fuelfactor[0] < (shipFuel - 1) / Constants.fuelfactor[0]) {
            Assets.assets.soundAssets.fuel.play();
        }
    }

    private void updateBall(ShapeRenderer renderer) {
        if (ballSpeedX > 0)
            ballSpeedX--;
        if (ballSpeedX < 0)
            ballSpeedX++;

        int ballXScreenF = ballXInternal / FACTOR + 8;
        int ballYScreenF = ballYInternal / FACTOR + 8;
        int shipXScreenF = shipXInternal / FACTOR;
        int shipYScreenF = shipYInternal / FACTOR + 8;

        if (atractorParticlesCount > 0 && ballMagnetisationLevel < 0 && abs(ballXScreenF - shipXScreenF) < 8
                && ballYScreenF > shipYScreenF && ballYScreenF < shipYScreenF + 32) {
            ballMagnetisationLevel++;
            if (ballMagnetisationLevel == 0) {
                // Ball has been magnetized
                Assets.assets.soundAssets.takeball.play();
                map.takeBall();
            }
        } else {
            if (ballMagnetisationLevel < 0) {
                ballMagnetisationLevel = -32;
            }
        }

        if (ballMagnetisationLevel == 0) {
            attractBallToShip();
        }

        handleBallBouncing(renderer);
    }

    private void attractBallToShip() {
        int xdif = ballXInternal / FACTOR - shipXInternal / FACTOR;
        xdif *= xdif;

        int ydif = ballYInternal / FACTOR - shipYInternal / FACTOR;
        ydif *= ydif;

        int totdif = xdif + ydif;
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

    private void handleBallBouncing(ShapeRenderer renderer) {
        int ballXScreenF = ballXInternal / FACTOR;
        int ballYScreenF = ballYInternal / FACTOR;

        Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;
        if (checkCollisionOfPolygonAndMap(tilePolygons[360], ballXInternal, ballYInternal, renderer)) {
            if (ballSpeedY > 0) {
                ballSpeedY = (int) (-0.75 * ballSpeedY);
                map.collideBall(ballXScreenF + 8, ballYScreenF + 12);
            } else {
                if (checkCollisionOfPolygonAndMap(tilePolygons[360], ballXInternal, ballYInternal - 1, renderer))
                    ballSpeedY -= 2;
            }
        } else {
            ballSpeedY += 2;
        }

        if (checkCollisionOfPolygonAndMap(tilePolygons[340], ballXInternal, ballYInternal, renderer)) {
            if (ballSpeedY < 0) {
                ballSpeedY = (int) (-0.75 * ballSpeedY);
                map.collideBall(ballXScreenF + 8, ballYScreenF + 4);
            } else {
                ballSpeedY += 2;
            }
        }

        if (checkCollisionOfPolygonAndMap(tilePolygons[342], ballXInternal, ballYInternal, renderer)) {
            if (ballSpeedX > 0) {
                ballSpeedX = (int) (-0.75 * ballSpeedX);
                map.collideBall(ballXScreenF + 12, ballYScreenF + 8);
            } else {
                ballSpeedX -= 2;
            }
        }

        if (checkCollisionOfPolygonAndMap(tilePolygons[362], ballXInternal, ballYInternal, renderer)) {
            if (ballSpeedX < 0) {
                ballSpeedX = (int) (-0.75 * ballSpeedX);
                map.collideBall(ballXScreenF + 4, ballYScreenF + 8);
            } else {
                ballSpeedX += 2;
            }
        }
    }

    private void updateBallPositionByGravity() {
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

        if (ballYInternal / FACTOR < 0 && ballMagnetisationLevel >= 0) {
            fadingState = 2;
            ballSpeedY = -FACTOR;
            ballMagnetisationLevel++;
            if (ballMagnetisationLevel >= 32) {
                throw new RuntimeException("You win!");
            }
        }

        if (ballXInternal / FACTOR > (map.getCols() - 1) * 16) {
            ballXInternal = (map.getCols() - 1) * 16 * FACTOR;
            ballSpeedX = 0;
        }

        if (ballYInternal / FACTOR > (map.getRows() - 1) * 16) {
            ballYInternal = ((map.getRows() - 1) * 16) * FACTOR;
            ballSpeedY = 0;
        }
    }

    private void updateShip() {
        if (shipState == ShipState.NORMAL) {
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

            if (bThrust && shipFuel > 0) {
                float radianAngle = (shipAngle - 90.0f) * degreesToRadians;
                shipSpeedX += (int) (cos(radianAngle) * 18f);
                shipSpeedY += (int) (sin(radianAngle) * 18f);
                if (shipSpeedX > 4 * FACTOR)
                    shipSpeedX = 4 * FACTOR;
                if (shipSpeedX < -4 * FACTOR)
                    shipSpeedX = -4 * FACTOR;
                if (shipSpeedY > 4 * FACTOR)
                    shipSpeedY = 4 * FACTOR;
                if (shipSpeedY < -4 * FACTOR)
                    shipSpeedY = -4 * FACTOR;
                if (!Constants.DEBUG_GOD_MODE) {
                    shipFuel--;
                }
                shipFuelUsed++;
                shipAnim++;
                if (shipAnim >= 6)
                    shipAnim = 1;

                if (!shipPlayThrustSound) {
                    assets.soundAssets.thrust.play();
                    assets.soundAssets.thrust.loop();
                    shipPlayThrustSound = true;
                }
            } else {
                shipAnim = 0;

                assets.soundAssets.thrust.stop();
                shipPlayThrustSound = false;
            }

            if (GameKeysStatus.bAtractor) {
                atractorParticlesCount++;
                if (atractorParticlesCount > 4)
                    atractorParticlesCount = 1;

                if (attractorParticles < Constants.MAX_ATRACTOR_P) {
                    attractorPX[attractorParticles] = shipXInternal + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    attractorPY[attractorParticles] = shipYInternal + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    attractorPSpeed[attractorParticles] = (float) (5 + random(5 - 1)) / 10.0F;
                    attractorPColor[attractorParticles] = 0;
                    attractorParticles++;
                }

            } else {
                atractorParticlesCount = 0;
                if (attractorParticles > 0)
                    attractorParticles -= 8;
                if (attractorParticles < 0)
                    attractorParticles = 0;
            }

            for (int i = 0; i < attractorParticles; i++) {
                attractorPX[i] += (int) (shipSpeedX * 0.9f);
                attractorPY[i] += (int) (shipSpeedY * 0.9f);
                attractorPX[i] = (int) (shipXInternal * (1.0f - attractorPSpeed[i])
                        + attractorPX[i] * attractorPSpeed[i]);
                attractorPY[i] = (int) (shipYInternal * (1.0f - attractorPSpeed[i])
                        + attractorPY[i] * attractorPSpeed[i]);
                if ((Math.abs(shipXInternal - attractorPX[i]) < 2 * FACTOR)
                        && (Math.abs(shipYInternal - attractorPY[i]) < 2 * FACTOR)) {
                    attractorPX[i] = shipXInternal + (random(16 * FACTOR - 1)) - 8 * FACTOR;
                    attractorPY[i] = shipYInternal + (random(16 * FACTOR - 1)) + 16 * FACTOR;
                    attractorPSpeed[i] = (float) (5 + (random(5 - 1))) / 10.0F;
                    attractorPColor[i] = 0;
                }
            }

            if (GameKeysStatus.isFire()) {
                float radianAngle = (shipAngle - 90) * degreesToRadians;

                shipShotsCount++;
                if (!Constants.DEBUG_GOD_MODE) {
                    shipFuel -= Constants.shotfuel[0];
                }
                shipFuelUsed += Constants.shotfuel[0];

                ShipBullet b = new ShipBullet();
                {
                    b.x = shipXInternal - 8 * FACTOR;
                    b.y = shipYInternal - 8 * FACTOR;
                    b.speedX = (int) (cos(radianAngle) * 4 * FACTOR);
                    b.speedY = (int) (sin(radianAngle) * 4 * FACTOR);
                    b.state = 0;
                }
                bullets.add(b);

                Assets.assets.soundAssets.shipshot.play();
            }

        } else if (shipState == ShipState.EXPLODED) {
            assets.soundAssets.thrust.stop();
            shipPlayThrustSound = false;

            shipAnim++;

            if (shipAnim >= 64)
                fadingState = 2;

            if (shipAnim >= 96) {
                throw new RuntimeException("You failed");
            }
        }
    }

    private void updateShipPositionByGravity() {
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

        if (shipXInternal / FACTOR < 0) {
            shipXInternal = 0;
            shipSpeedX = 0;
        }
        if (shipYInternal / FACTOR < 0) {
            shipYInternal = 0;
            shipSpeedY = 0;
        }
        if (shipXInternal / FACTOR > map.getCols() * 16) {
            shipXInternal = map.getCols() * 16 * FACTOR;
            shipSpeedX = 0;
        }
        if (shipYInternal / FACTOR > map.getRows() * 16) {
            shipYInternal = map.getRows() * 16 * FACTOR;
            shipSpeedY = 0;
        }
    }

    private void updateBullets(ShapeRenderer renderer) {
        List<ShipBullet> toDeleteList = new ArrayList<ShipBullet>();

        for (ShipBullet b : bullets) {
            if (b.state == 0) {
                b.x += b.speedX;
                b.y += b.speedY;

                if (checkCollisionOfPolygonAndMap(Assets.assets.graphicAssets.tilePolygons[242], b.x, b.y, renderer)) {
                    b.state++;

                    int bulletXScreenF = (b.x / FACTOR) + 8;
                    int bulletYScreenF = (b.y / FACTOR) + 8;
                    int buleltStrength = 1; // int ship_strength[]={1,2,4};

                    int retv = map.collideShipBullet(bulletXScreenF, bulletYScreenF, buleltStrength);

                    if (retv != 0)
                        shipHitsCount++;

                    if (retv == 2)
                        shipEnemiesDestroyedCount++;
                } else {
                    if (b.x < -8 * FACTOR || b.x > (map.getCols() * 16 * FACTOR) + 8 * FACTOR || b.y < -8 * FACTOR
                            || b.y > (map.getRows() * 16 * FACTOR) + 8 * FACTOR) {
                        toDeleteList.add(b);
                    }
                }
            } else {
                b.state++;
                if (b.state >= 40)
                    toDeleteList.add(b);
            }

        }

        bullets.removeAll(toDeleteList);
    }

    private boolean checkCollisionOfPolygonAndMap(Polygon objectPolygon, int xInternal, int yInternal,
            ShapeRenderer renderer) {
        int objectXScreenF = xInternal / FACTOR;
        int objectYScreenF = yInternal / FACTOR;

        int objectXScreen = objectXScreenF - mapXScreen;
        int objectYScreen = objectYScreenF - mapYScreen;

        objectPolygon.setPosition(objectXScreen, INTERNAL_SCREEN_HEIGHT - (objectYScreen + map.stepY));

        // (+8, +8) here moves the point closer to bullet's or ball's center
        return map.checkCollision(objectXScreenF + 8, objectYScreenF + 8, mapXScreen, mapYScreen, objectPolygon, null,
                renderer);
    }

    private boolean checkCollisionOfShipAndMap(ShapeRenderer renderer) {
        Polygon objectPolygon = assets.graphicAssets.shipPolygon;

        objectPolygon.setRotation(360 - shipAngle);

        int objectXScreenF = shipXInternal / FACTOR;
        int objectYScreenF = shipYInternal / FACTOR;

        int objectXScreen = objectXScreenF - mapXScreen;
        int objectYScreen = objectYScreenF - mapYScreen;

        // ship's polygon is centralized
        objectPolygon.setPosition(objectXScreen - map.stepY, INTERNAL_SCREEN_HEIGHT - (objectYScreen + map.stepY));

        return map.checkCollision(objectXScreenF, objectYScreenF, mapXScreen, mapYScreen, objectPolygon, null,
                renderer);
    }

    public void render(SpriteBatch batch, ShapeRenderer renderer) {
        if (batch != null) {
            int screenWidth = INTERNAL_SCREEN_WIDTH;
            int screenHeight = INTERNAL_SCREEN_HEIGHT;

            map.render(batch, mapXScreen, mapYScreen, screenWidth, screenHeight);

            renderBall(batch);
            renderAttractor(batch);
            renderShip(batch, renderer);
            renderShipBullet(batch);

            drawFuelStatus(batch);
        }
    }

    private void drawFuelStatus(SpriteBatch batch) {
        Sprite sprite = new Sprite(Assets.assets.graphicAssets.whiteSpot); // Assets.assets.graphicAssets.tiles.get(498));

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
        sprite.setSize(shipFuel / 64.0f, 6);

        float f = (float) (shipFuel) / (64.0f * 30.0F);
        if (f >= 1.0f) {
            f = 1.0f;
        }

        sprite.setColor(new Color((int) (255 * (1 - f * f)), (int) (200 * sqrt(f)), 0, 1));
        sprite.draw(batch);
    }

    private void renderAttractor(SpriteBatch batch) {
        if (batch == null)
            return;

        AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(242);
        Sprite sprite = new Sprite(tile);

        for (int i = 0; i < attractorParticles; i++) {
            if (attractorPColor[i] == 0) {
                int v = (random(192 - 1)) + 64;
                attractorPColor[i] = v;
            }

            sprite.setScale(0.4f, 0.4f);
            sprite.setAlpha(255.0f / attractorPColor[i]);

            int xScreen = attractorPX[i] / FACTOR - mapXScreen;
            int yScreen = attractorPY[i] / FACTOR - mapYScreen;
            sprite.setCenter(xScreen, Constants.INTERNAL_SCREEN_HEIGHT - yScreen);

            sprite.draw(batch);
        }
    }

    private void renderBall(SpriteBatch batch) {
        if (batch == null)
            return;

        int tileIndex = (ballMagnetisationLevel < 0) ? 320 : 321;
        AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(tileIndex);

        Sprite sprite = new Sprite(tile);

        // int ballXScreen = ballXInternal / FACTOR - mapXScreen + 8;
        // int ballYScreen = ballYInternal / FACTOR - mapYScreen + 8;
        // sprite.setCenter(ballXScreen, INTERNAL_SCREEN_HEIGHT - ballYScreen);

        int ballXScreen = ballXInternal / FACTOR - mapXScreen;
        int ballYScreen = ballYInternal / FACTOR - mapYScreen;

        sprite.setPosition(ballXScreen, INTERNAL_SCREEN_HEIGHT - (ballYScreen + map.stepY));

        sprite.draw(batch);
    }

    private void renderShipBullet(SpriteBatch batch) {
        if (batch == null)
            return;

        for (ShipBullet b : bullets) {
            int tileIndex = (b.state < 8) ? 242 : 399 + (b.state / 8);
            AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(tileIndex);

            Sprite sprite = new Sprite(tile);

            // int bulletXScreen = b.x / FACTOR - mapXScreen + 8;
            // int bulletYScreen = b.y / FACTOR - mapYScreen + 8;
            // sprite.setCenter(bulletXScreen, INTERNAL_SCREEN_HEIGHT - bulletYScreen);

            int bulletXScreen = b.x / FACTOR - mapXScreen;
            int bulletYScreen = b.y / FACTOR - mapYScreen;

            sprite.setPosition(bulletXScreen, INTERNAL_SCREEN_HEIGHT - (bulletYScreen + map.stepY));

            sprite.draw(batch);
        }
    }

    private void updateMapCameraPosition() {
        int screenWidth = INTERNAL_SCREEN_WIDTH;
        int screenHeight = INTERNAL_SCREEN_HEIGHT;

        int deltaX = shipXInternal / FACTOR - INTERNAL_SCREEN_WIDTH / 2 - mapXScreen;
        int deltaY = shipYInternal / FACTOR - (int) (INTERNAL_SCREEN_HEIGHT / 2.4) - mapYScreen;

        if (deltaX > 8)
            deltaX = 8;
        else if (deltaX < -9)
            deltaX = -8;

        if (deltaY > 8)
            deltaY = 8;
        else if (deltaY < -9)
            deltaY = -8;

        mapXScreen += deltaX;
        mapYScreen += deltaY;

        if (mapXScreen > map.getCols() * 16 - screenWidth)
            mapXScreen = map.getCols() * 16 - screenWidth;
        else if (mapXScreen < 0)
            mapXScreen = 0;

        if (mapYScreen > map.getRows() * 16 - screenHeight)
            mapYScreen = map.getRows() * 16 - screenHeight;
        else if (mapYScreen < 0)
            mapYScreen = 0;
    }

    private void renderShip(SpriteBatch batch, ShapeRenderer renderer) {
        if (shipState == ShipState.NORMAL) {
            int shipXScreen = shipXInternal / FACTOR - mapXScreen;
            int shipYScreen = shipYInternal / FACTOR - mapYScreen;

            if (batch != null) {
                if (shipAnim == 0)
                    shipSprite.setRegion(shipRegion);
                else
                    shipSprite.setRegion(Assets.assets.graphicAssets.shipThrustTiles.get(shipAnim - 1));

                shipSprite.setRotation(360 - shipAngle);

                // I have something to do with it in the future but for now it's ok as the
                // polygon's coordinates are correct
                // shipSprite.setPosition(shipXScreen - map.stepY, INTERNAL_SCREEN_HEIGHT -
                // (shipYScreen + map.stepY));

                // shipSprite.setPosition(shipXScreen - map.stepY, INTERNAL_SCREEN_HEIGHT -
                // (shipYScreen + map.stepY));
                shipSprite.setCenter(shipXScreen, INTERNAL_SCREEN_HEIGHT - shipYScreen);

                shipSprite.draw(batch);
            }

            if (renderer != null) {
                Polygon shipPolygon = assets.graphicAssets.shipPolygon;
                shipPolygon.setRotation(360 - shipAngle);
                shipPolygon.setPosition(shipXScreen, INTERNAL_SCREEN_HEIGHT - (shipYScreen + map.stepY));
                renderer.polygon(shipPolygon.getTransformedVertices());
            }
        } else if (shipState == ShipState.EXPLODED) {
            int frame = shipAnim / 8;

            if (frame < 6) {
                shipSprite.setRegion(Assets.assets.graphicAssets.shipExplosionTiles.get(frame));

                int shipXScreen = shipXInternal / FACTOR - mapXScreen;
                int shipYScreen = shipYInternal / FACTOR - mapYScreen;

                shipSprite.setCenter(shipXScreen, INTERNAL_SCREEN_HEIGHT - shipYScreen);
                shipSprite.setRotation(0);

                if (batch != null) {
                    shipSprite.draw(batch);
                }
            }
        }
    }

}
