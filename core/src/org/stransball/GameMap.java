package org.stransball;

import static com.badlogic.gdx.math.MathUtils.random;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.stransball.objects.BackgroundLayer;
import org.stransball.objects.Door;
import org.stransball.objects.Enemy;
import org.stransball.objects.Enemy.EnemyType;
import org.stransball.objects.EnemyBullet;
import org.stransball.objects.EnemyTank;
import org.stransball.objects.FuelRecharge;
import org.stransball.objects.Smoke;
import org.stransball.objects.SmokeSource;
import org.stransball.objects.StarsLayer;
import org.stransball.objects.Switch;
import org.stransball.util.CollisionDetectionUtils;
import org.stransball.util.Triangulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class GameMap {

    public int[] map;
    private int cols;
    private int rows;
    private int animTimer;
    private int animFlag;

    private BackgroundLayer background;
    private StarsLayer stars;

    private List<Enemy> enemies;
    private List<Door> doors;
    private List<Switch> switches;
    private List<FuelRecharge> fuelRecharges;

    private List<SmokeSource> smokeSources;
    private List<Smoke> smokes;

    @Deprecated
    // It has to be tile.height instead. Complete formula: screen.height -
    // tile.height - y
    public int stepY;

    private final Triangulator triangulator;;
    private final MapLoader mapLoader;

    public GameMap() {
        triangulator = new Triangulator();
        mapLoader = new MapLoader(this);
    }

    public void load(Reader input) {
        MapData mapData = mapLoader.load(input);

        map = mapData.map;
        cols = mapData.cols;
        rows = mapData.rows;
        animTimer = mapData.animTimer;
        animFlag = mapData.animFlag;

        background = mapData.background;
        stars = mapData.stars;

        enemies = mapData.enemies;
        doors = mapData.doors;
        switches = mapData.switches;
        fuelRecharges = mapData.fuelRecharges;

        smokeSources = mapData.smokeSources;
        smokes = mapData.smokes;

        stepY = Assets.assets.graphicAssets.tiles.get(0).originalHeight;
    }

    public void update(int shipXInternal, int shipYInternal, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, ShapeRenderer renderer) {
        { // Tile animation
            animTimer++;

            if (animTimer > 24) {
                animFlag++;

                if (animFlag < 0 || animFlag > 7) {
                    animFlag = 0;
                }

                animTimer = 0;
            }
        }

        background.update();

        int shipXScreenF = shipXInternal / FACTOR;
        int shipYScreenF = shipYInternal / FACTOR;

        updateEnemies(shipXScreenF, shipYScreenF, shipSpeedX, shipSpeedY, mapXScreen, mapYScreen, renderer);

        updateDoors();
        updateSwitches();

        updateSmoke();
    }

    private void updateSmoke() {
        {
            List<SmokeSource> toDelete = new ArrayList<SmokeSource>();

            for (SmokeSource ss : smokeSources) {
                ss.timer++;

                if (ss.timer > 256) {
                    toDelete.add(ss);
                } else {
                    int chance = ss.timer;
                    chance = (chance * chance) / 256;
                    chance /= 16;

                    if (MathUtils.random(chance + 2 - 1) == 0) {
                        Smoke s = new Smoke();

                        s.x = ss.x * FACTOR;
                        s.y = ss.y * FACTOR;

                        s.speedX = ((random(1 + FACTOR / 16 - 1) - (FACTOR / 32))) + ss.speedX;
                        s.speedY = ((random(1 + FACTOR / 16 - 1) - (FACTOR / 32))) + ss.speedY;
                        s.desiredX = (random(FACTOR / 4 - 1)) - FACTOR / 8;
                        s.desiredY = ((random(1 + FACTOR / 4 - 1) - (FACTOR / 8))) - FACTOR / 4;
                        s.timer = 0;

                        smokes.add(s);
                    }
                }
            }

            smokeSources.removeAll(toDelete);
        }

        {
            List<Smoke> toDelete = new ArrayList<Smoke>();

            for (Smoke s : smokes) {
                s.timer++;

                s.x += s.speedX;
                s.y += s.speedY;

                if (s.speedX > s.desiredX)
                    s.speedX -= 2;
                else if (s.speedX < s.desiredX)
                    s.speedX += 2;

                if (s.speedY > s.desiredY)
                    s.speedY -= 1;
                else if (s.speedY < s.desiredY)
                    s.speedY += 1;

                if (s.timer > 255 || s.y < -8 * FACTOR) {
                    toDelete.add(s);
                }
            }

            smokes.removeAll(toDelete);
        }
    }

    private void updateSwitches() {
        for (Switch s : switches) {
            if (s.state > 0) {
                s.state--;
            }
        }
    }

    private void updateDoors() {
        for (Door d : doors) {
            if (d.action == -1) {
                if (d.state > 0)
                    d.state--;
                else
                    d.action = 0;
            }
            if (d.action == 1) {
                if (d.state < 14)
                    d.state++;
                else
                    d.action = 0;
            }
        }
    }

    private void updateEnemies(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, ShapeRenderer renderer) {
        List<Enemy> enemiesToDelete = new ArrayList<Enemy>();
        List<Enemy> newEnemies = new ArrayList<Enemy>();

        for (Enemy e : enemies) {
            e.update(shipXScreenF, shipYScreenF, shipSpeedX, shipSpeedY, mapXScreen, mapYScreen, enemiesToDelete,
                    newEnemies, renderer);
        }

        enemies.removeAll(enemiesToDelete);
        enemies.addAll(newEnemies);
    }

    public void render(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight) {
        stars.render(batch, mapXScreen, mapYScreen);
        background.render(batch, mapXScreen, mapYScreen, screenWidth, screenHeight);

        drawWalls(batch, mapXScreen, mapYScreen, screenWidth, screenHeight, null);

        renderSmoke(batch, mapXScreen, mapYScreen, screenWidth, screenHeight);

        drawEnemies(batch, mapXScreen, mapYScreen, screenWidth, screenHeight, null, null);
    }

    public void drawWalls(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        int stepX = tiles.get(0).originalWidth;
        int stepY = tiles.get(0).originalHeight;

        for (int j = 0, actYScreen = -mapYScreen; j < rows; j++, actYScreen += stepY) {
            if (actYScreen < -stepY || actYScreen >= screenHeight)
                continue;

            for (int i = 0, actXScreen = -mapXScreen; i < cols; i++, actXScreen += stepX) {
                if (actXScreen < -stepX || actXScreen >= screenWidth)
                    continue;

                int tileIndex = -1;

                if (j >= 0) {
                    tileIndex = map[i + j * cols];
                }

                tileIndex = animateTile(tileIndex);

                if (tileIndex < 0)
                    continue;

                if (tileIndex == 113 || tileIndex == 114) {
                    // DOOR
                    int state = 0;

                    for (Door d : doors) {
                        if ((d.col == i || d.col == i - 1) && d.row == j) {
                            state = d.state;
                        }
                    }

                    int offset = (tileIndex == 113) ? -state : state;

                    drawWithOffset(actXScreen, actYScreen, offset, batch, tileIndex, detector, stepY);
                } else {
                    if ((tileIndex >= 116 && tileIndex < 120) || (tileIndex >= 136 && tileIndex < 140)
                            || (tileIndex >= 156 && tileIndex < 160)) {
                        // SWITCH
                        for (Switch s : switches) {
                            if (s.col == i && s.row == j) {
                                int tileIndex_ = (s.state != 0) ? tileIndex + 140 : tileIndex;

                                if (batch != null) {
                                    batch.draw(tiles.get(tileIndex_), actXScreen,
                                            INTERNAL_SCREEN_HEIGHT - (actYScreen + stepY));
                                }

                                if (detector != null) {
                                    detector.handlePolygon(actXScreen, actYScreen + stepY, tileIndex_);
                                }
                            }
                        }
                    } else {
                        if (batch != null) {
                            batch.draw(tiles.get(tileIndex), actXScreen, INTERNAL_SCREEN_HEIGHT - (actYScreen + stepY));
                        }

                        if (detector != null) {
                            detector.handlePolygon(actXScreen, actYScreen + stepY, tileIndex);
                        }
                    }
                }
            }
        }
    }

    private void drawWithOffset(int actXScreen, int actYScreen, int offset, SpriteBatch batch, int tileIndex,
            ICollisionDetector detector, int stepY) {
        if (batch != null) {
            AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(tileIndex);

            Sprite sprite = new Sprite(tile);

            sprite.setPosition(actXScreen + offset, INTERNAL_SCREEN_HEIGHT - (actYScreen + stepY));

            if (offset > 0 && offset < tile.getRegionWidth()) {
                sprite.setRegionWidth(sprite.getRegionWidth() - offset);
            } else if (offset < 0) {
                sprite.setRegionWidth(sprite.getRegionWidth() + offset);
            }

            sprite.draw(batch);
        }

        if (detector != null) {
            detector.handlePolygon(actXScreen + offset, actYScreen + stepY, tileIndex);
        }
    }

    private void drawEnemies(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            Enemy enemy, ICollisionDetector detector) {
        for (Enemy e : enemies) {
            if (e != enemy) {
                e.draw(batch, mapXScreen, mapYScreen, screenWidth, screenHeight, detector);
            }
        }
    }

    private void renderSmoke(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight) {
        if (batch == null)
            return;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        for (Smoke s : smokes) {
            int frame = ((s.timer) >> 3) % 3;

            int rx = s.x / FACTOR - mapXScreen;
            int ry = s.y / FACTOR - mapYScreen;

            if (rx > -16 && rx < screenWidth && ry > -16 && ry < screenHeight) {
                int alpha = 255 - s.timer;

                alpha = (alpha * alpha) / (255);

                if (alpha < 0)
                    alpha = 0;
                if (alpha > 255)
                    alpha = 255;

                AtlasRegion tile = tiles.get(272 + frame);

                Sprite sprite = new Sprite(tile);

                int x_ = rx + 8;
                int y_ = ry + 8;

                sprite.setCenter(x_, INTERNAL_SCREEN_HEIGHT - y_);

                sprite.draw(batch, alpha / 255.0f);
            }
        }
    }

    private int animateTile(int tileIndex) {
        if (tileIndex < 0)
            return tileIndex;

        if (tileIndex == 110 && animTimer > 16)
            return 111;
        if (tileIndex == 110 && animTimer > 8)
            return 112;

        if (tileIndex == 64 && animTimer > 16)
            return 66;
        if (tileIndex == 64 && animTimer > 8)
            return 65;

        if (tileIndex == 67 && (animFlag & 0x01) == 1)
            return 69;
        if (tileIndex == 68 && (animFlag & 0x01) == 1)
            return 70;

        if (tileIndex == 26 && animTimer > 12 && (animFlag & 0x01) == 0)
            return 24;
        if (tileIndex == 26 && animTimer > 12 && (animFlag & 0x01) == 1)
            return 25;

        if (tileIndex == 146 && animTimer > 12 && (animFlag & 0x01) == 0)
            return 144;
        if (tileIndex == 146 && animTimer > 12 && (animFlag & 0x01) == 1)
            return 145;

        if (tileIndex == 27 && (animFlag & 0x01) == 1)
            return 28;
        if (tileIndex == 147 && (animFlag & 0x01) == 1)
            return 148;

        if (tileIndex == 115 && animFlag > 3)
            return -1;
        if (tileIndex == 130 && animFlag > 3)
            return -1;

        if (tileIndex == 32 && animFlag > 3)
            return 30;
        if (tileIndex == 33 && animFlag > 3)
            return 31;
        if (tileIndex == 36 && animFlag > 3)
            return 34;
        if (tileIndex == 37 && animFlag > 3)
            return 35;

        if (tileIndex == 422 && animFlag > 3)
            return 420;
        if (tileIndex == 423 && animFlag > 3)
            return 421;

        if (tileIndex == 162 && animFlag > 3)
            return 160;
        if (tileIndex == 163 && animFlag > 3)
            return 161;
        if (tileIndex == 166 && animFlag > 3)
            return 164;
        if (tileIndex == 167 && animFlag > 3)
            return 165;

        if (tileIndex == 76 && animTimer <= 12 && (animFlag & 0x03) == 0)
            return -1;
        if (tileIndex == 76 && animTimer > 12 && (animFlag & 0x03) == 0)
            return 79;
        if (tileIndex == 76 && animTimer <= 12 && (animFlag & 0x03) == 1)
            return 78;
        if (tileIndex == 76 && animTimer > 12 && (animFlag & 0x03) == 1)
            return 77;
        if (tileIndex == 76 && animTimer > 12 && (animFlag & 0x03) == 2)
            return 77;
        if (tileIndex == 76 && animTimer <= 12 && (animFlag & 0x03) == 3)
            return 78;
        if (tileIndex == 76 && animTimer > 12 && (animFlag & 0x03) == 3)
            return 79;

        if (tileIndex == 150 && animTimer <= 12 && (animFlag & 0x03) == 0)
            return -1;
        if (tileIndex == 150 && animTimer > 12 && (animFlag & 0x03) == 0)
            return 153;
        if (tileIndex == 150 && animTimer <= 12 && (animFlag & 0x03) == 1)
            return 152;
        if (tileIndex == 150 && animTimer > 12 && (animFlag & 0x03) == 1)
            return 151;
        if (tileIndex == 150 && animTimer > 12 && (animFlag & 0x03) == 2)
            return 151;
        if (tileIndex == 150 && animTimer <= 12 && (animFlag & 0x03) == 3)
            return 152;
        if (tileIndex == 150 && animTimer > 12 && (animFlag & 0x03) == 3)
            return 153;

        return tileIndex;
    }

    public int collideShipBullet(int bulletXScreenF, int bulletYScreenF, int bulletStrength) {
        int retval = 0;

        List<Enemy> enemiesToDelete = new ArrayList<Enemy>();
        List<Enemy> newEnemies = new ArrayList<Enemy>();

        Enemy selected = null;
        int mindistance = -1;

        for (Enemy e : enemies) {
            int ex = e.x;
            int ey = e.y;

            if (e.type == EnemyType.BULLET) {
                ex /= FACTOR;
                ey /= FACTOR;
            }

            if (e.type == EnemyType.BULLET || e.type == EnemyType.CANON || e.type == EnemyType.FAST_CANON
                    || e.type == EnemyType.DIRECTIONAL_CANON || e.type == EnemyType.DIRECTIONAL_CANON_2) {
                ex += 8;
                ey += 8;
            }

            int distance = (bulletXScreenF - ex) * (bulletXScreenF - ex)
                    + (bulletYScreenF - ey) * (bulletYScreenF - ey);

            int tolerance = 100;

            if (e.type == EnemyType.DIRECTIONAL_CANON || e.type == EnemyType.DIRECTIONAL_CANON_2
                    || (e.type == EnemyType.TANK && e.tankType == 3))
                tolerance = 200;

            if (((mindistance == -1 && distance < tolerance) || distance < mindistance)
                    && (e.type == EnemyType.BULLET || e.type == EnemyType.CANON || e.type == EnemyType.FAST_CANON
                            || e.type == EnemyType.DIRECTIONAL_CANON || e.type == EnemyType.TANK
                            || e.type == EnemyType.DIRECTIONAL_CANON_2)) {
                selected = e;
                mindistance = distance;
            }
        }

        if (selected != null) {
            Assets.assets.soundAssets.enemyHit.play();
            retval = 1;
        }

        if (selected != null && selected.takeShot(bulletStrength)) {
            int generate_smoke = -1;

            if (selected.type != EnemyType.BULLET)
                retval = 2; // If it's not a bullet, then you have destroyed an enemy

            if (selected.type == EnemyType.CANON || selected.type == EnemyType.FAST_CANON
                    || selected.type == EnemyType.DIRECTIONAL_CANON || selected.type == EnemyType.DIRECTIONAL_CANON_2) {
                int x_ = selected.x / 16;
                int y_ = selected.y / 16;
                int i = x_ + y_ * cols;

                if (map[i] == 154 || map[i] == 386) {
                    map[i] = 180;
                    generate_smoke = 0;
                }
                if (map[i] == 155 || map[i] == 387) {
                    map[i] = 181;
                    generate_smoke = 1;
                }
                if (map[i] == 174 || map[i] == 406) {
                    map[i] = 182;
                    generate_smoke = 2;
                }
                if (map[i] == 175 || map[i] == 407) {
                    map[i] = 183;
                    generate_smoke = 3;
                }
                if (map[i] >= 176 && map[i] < 180) {
                    generate_smoke = map[i] - 176;
                    map[i] -= 6;
                }
                if (map[i] >= 196 && map[i] < 200) {
                    generate_smoke = map[i] - 196;
                    map[i] -= 16;
                }
                if (map[i] >= 216 && map[i] < 220) {
                    generate_smoke = map[i] - 216;
                    map[i] -= 36;
                }
                if (map[i] >= 236 && map[i] < 240) {
                    generate_smoke = map[i] - 236;
                    map[i] -= 36;
                }
                if (generate_smoke == 0) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speedX = 0;
                    ss.speedY = -FACTOR / 4;
                    ss.timer = 0;
                    smokeSources.add(ss);
                }
                if (generate_smoke == 1) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speedX = 0;
                    ss.speedY = FACTOR / 4;
                    ss.timer = 0;
                    smokeSources.add(ss);
                }
                if (generate_smoke == 2) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speedX = FACTOR / 4;
                    ss.speedY = 0;
                    ss.timer = 0;
                    smokeSources.add(ss);
                }
                if (generate_smoke == 3) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speedX = -FACTOR / 4;
                    ss.speedY = 0;
                    ss.timer = 0;
                    smokeSources.add(ss);
                }
                Assets.assets.soundAssets.explosion.play();
            }

            if (selected.type != EnemyType.TANK && selected.type != EnemyType.DESTROYED_TANK
                    && selected.type != EnemyType.EXPLOSION) {
                if (selected.type == EnemyType.BULLET) {
                    selected.state = 0;

                    enemiesToDelete.add(selected);
                    selected = ((EnemyBullet) selected).toExplosion();
                    newEnemies.add(selected);
                } else {
                    selected.state = -1;
                }
            } else {
                if (selected.type == EnemyType.TANK) {
                    Assets.assets.soundAssets.explosion.play();
                    selected.state = 0;

                    enemiesToDelete.add(selected);
                    selected = ((EnemyTank) selected).toDestroyedTank();
                    newEnemies.add(selected);

                    generate_smoke = 0;

                    if (generate_smoke == 0) {
                        SmokeSource ss = new SmokeSource();
                        ss.x = selected.x;
                        ss.y = selected.y;
                        ss.speedX = 0;
                        ss.speedY = -FACTOR / 4;
                        ss.timer = 0;
                        smokeSources.add(ss);
                    }
                }
            }
        }

        enemies.removeAll(enemiesToDelete);
        enemies.addAll(newEnemies);

        return retval;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getBallPositionX() {
        for (int i = 0; i < cols * rows; i++) {
            if (map[i] == 110)
                return i % cols;
        }
        return 0;
    }

    public int getBallPositionY() {
        for (int i = 0; i < cols * rows; i++) {
            if (map[i] == 110)
                return i / cols;
        }
        return 0;
    }

    public void takeBall() {
        for (Door d : doors) {
            // The doors with event==0 are activated when the ball is taken
            if (d.event == 0) {
                d.activate();
            }
        }
    }

    public void collideBall(int ballXScreenF, int ballYScreenF) {
        Switch selected = null;
        int minDistance = -1;

        for (Switch s : switches) {
            int switchXScreenF = s.col * 16 + 8;
            int switchYScreenF = s.row * 16 + 8;

            int distance = (ballXScreenF - switchXScreenF) * (ballXScreenF - switchXScreenF)
                    + (ballYScreenF - switchYScreenF) * (ballYScreenF - switchYScreenF);

            if ((minDistance == -1 && distance < 64) || distance < minDistance) {
                selected = s;
                minDistance = distance;
            }
        }

        if (selected != null) {
            selected.state = 16;

            Assets.assets.soundAssets.switchship.play();

            for (Door d : doors) {
                if (d.event == selected.number) {
                    d.activate();
                }
            }
        }
    }

    public boolean isShipInFuelRecharge(int shipXScreenF, int shipYScreenF) {
        for (FuelRecharge f : fuelRecharges) {
            if (f.isInside(shipXScreenF, shipYScreenF)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkCollision(int objectXScreenF, int objectYScreenF, int mapXScreen, int mapYScreen,
            Polygon objectPolygon, Enemy enemy, ShapeRenderer renderer) {
        int regionWidth = 32;
        int regionHeight = 32;
        int regionXScreenF = objectXScreenF - regionWidth / 2;
        int regionYScreenF = objectYScreenF - regionHeight / 2;

        return checkCollisionInRegion(regionXScreenF, regionYScreenF, regionWidth, regionHeight, mapXScreen, mapYScreen,
                objectPolygon, enemy, renderer);
    }

    private boolean checkCollisionInRegion(int regionXScreenF, int regionYScreenF, int regionWidth, int regionHeight,
            int mapXScreen, int mapYScreen, Polygon objectPolygon, Enemy enemy, ShapeRenderer renderer) {
        if (objectPolygon == null)
            return false;

        Iterable<Polygon> objectPolygons = triangulator.tiangulate(objectPolygon);

        CollisionDetector detector = new CollisionDetector(renderer, objectPolygons, regionXScreenF, regionYScreenF,
                mapXScreen, mapYScreen);

        if (renderer != null) {
            renderer.setColor(Color.WHITE);
            CollisionDetectionUtils.drawPolygons(renderer, objectPolygons);
        }

        drawWalls(null, regionXScreenF, regionYScreenF, regionWidth, regionHeight, detector);

        if (detector.wasCollision())
            return true;

        drawEnemies(null, regionXScreenF, regionYScreenF, regionWidth, regionHeight, enemy, detector);

        return detector.wasCollision();
    }

}
