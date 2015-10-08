package org.stransball;

import static com.badlogic.gdx.math.MathUtils.random;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.stransball.objects.BackgroundLayer;
import org.stransball.objects.Door;
import org.stransball.objects.Enemy;
import org.stransball.objects.Enemy.EnemyType;
import org.stransball.objects.FuelRecharge;
import org.stransball.objects.Smoke;
import org.stransball.objects.SmokeSource;
import org.stransball.objects.StarsLayer;
import org.stransball.objects.Switch;
import org.stransball.util.CollisionDetectionUtils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class GameMap {

    private static final int EMPTY_ROWS = 8;

    private int[] map;
    private int sx;
    private int sy;
    private int animtimer;
    private int animflag;

    private BackgroundLayer background;
    private StarsLayer stars;

    private int switchnumber;
    private List<Enemy> enemies;
    private List<Door> doors;
    private List<Switch> switches;
    private List<FuelRecharge> fuel_recharges;

    private List<SmokeSource> smokesources;
    private List<Smoke> smokes;

    public void load(Reader input) {
        Scanner scanner = new Scanner(input);
        try {
            load(scanner);
        } finally {
            scanner.close();
        }
    }

    private void load(Scanner scanner) {
        switchnumber = 1;

        enemies = new ArrayList<Enemy>();
        doors = new ArrayList<Door>();
        switches = new ArrayList<Switch>();
        fuel_recharges = new ArrayList<FuelRecharge>();
        smokesources = new ArrayList<SmokeSource>();
        smokes = new ArrayList<Smoke>();

        sx = scanner.nextInt();
        sy = scanner.nextInt();
        sy += EMPTY_ROWS;

        map = new int[sx * sy];

        Arrays.fill(map, -1);

        for (int i = sx * EMPTY_ROWS; i < sx * sy; i++) {
            map[i] = scanner.nextInt();
            map[i]--;
        }

        // Look for enemies, doors, etc.:
        for (int i = 0; i < sx * sy; i++) {

            // ENEMIES:
            if ((map[i] >= 176 && map[i] < 180) || (map[i] >= 196 && map[i] < 200) || (map[i] >= 216 && map[i] < 220)
                    || (map[i] >= 236 && map[i] < 240)
                    || (map[i] == 154 || map[i] == 155 || map[i] == 174 || map[i] == 175)
                    || (map[i] == 386 || map[i] == 387 || map[i] == 406 || map[i] == 407)) {

                int direction = 0;
                int x = (i % sx) * 16;
                int y = (i / sx) * 16;

                if (map[i] == 176 || map[i] == 216 || map[i] == 236 || map[i] == 196 || map[i] == 154 || map[i] == 386)
                    direction = 0;
                if (map[i] == 177 || map[i] == 217 || map[i] == 237 || map[i] == 197 || map[i] == 155 || map[i] == 387)
                    direction = 1;
                if (map[i] == 178 || map[i] == 218 || map[i] == 238 || map[i] == 198 || map[i] == 174 || map[i] == 406)
                    direction = 2;
                if (map[i] == 179 || map[i] == 219 || map[i] == 239 || map[i] == 199 || map[i] == 175 || map[i] == 407)
                    direction = 3;

                if ((map[i] >= 176 && map[i] < 180) || (map[i] >= 216 && map[i] < 220)
                        || (map[i] >= 236 && map[i] < 240)) {
                    // CANON:
                    Enemy e = new Enemy(EnemyType.CANON);
                    e.state = 0;
                    e.life = 4;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    enemies.add(e);
                }

                if (map[i] >= 196 && map[i] < 200) {
                    // FAST CANON:
                    Enemy e = new Enemy(EnemyType.FAST_CANON);
                    e.state = 0;
                    e.life = 8;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    enemies.add(e);
                }

                if (map[i] == 154 || map[i] == 155 || map[i] == 174 || map[i] == 175) {
                    // DIRECTIONAL CANON:
                    Enemy e = new Enemy(EnemyType.DIRECTIONAL_CANON);
                    e.state = i % 128;
                    e.life = 12;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    e.turret_angle = 0;
                    enemies.add(e);
                }
                if (map[i] == 386 || map[i] == 387 || map[i] == 406 || map[i] == 407) {
                    // DIRECTIONAL CANON 2:
                    Enemy e = new Enemy(EnemyType.DIRECTIONAL_CANON_2);
                    e.state = i % 128;
                    e.life = 12;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    e.tank_angle = 0;
                    e.turret_angle = 0;
                    enemies.add(e);
                }
            }

            // DOORS:
            if (map[i] == 113) {
                Door d = new Door();

                d.x = i % sx;
                d.y = i / sx;
                d.action = 0;

                d.state = scanner.nextInt();
                d.event = scanner.nextInt();
                doors.add(d);
            }

            // SWITCHES:
            if ((map[i] >= 116 && map[i] < 120) || (map[i] >= 136 && map[i] < 140) || (map[i] >= 156 && map[i] < 160)) {
                Switch s = new Switch();
                s.x = i % sx;
                s.y = i / sx;
                s.number = switchnumber++;
                s.state = 0;
                switches.add(s);
            }

            // FUEL RECHARGES:
            if (map[i] == 132) {
                FuelRecharge f = new FuelRecharge();
                f.x = i % sx;
                f.y = i / sx;
                fuel_recharges.add(f);
            }
        }

        // Tanks:
        {
            int ntanks;

            ntanks = scanner.nextInt();
            for (int i = 0; i < ntanks; i++) {
                Enemy e = new Enemy(EnemyType.TANK);

                int x = scanner.nextInt();
                int y = scanner.nextInt();
                int type = scanner.nextInt();

                x *= 16;
                y *= 16;

                e.state = 1;
                e.state2 = 0;
                e.x = x;
                e.y = y;
                e.life = 10;
                e.tank_type = type;
                e.tank_angle = 0;
                e.turret_angle = 90;
                enemies.add(e);
            }
        }

        int background_type = scanner.nextInt();
        background = new BackgroundLayer(background_type, sx, sy);

        animtimer = 0;
        animflag = 0;

        stars = new StarsLayer(sx);
    }

    public void update(int ship_x, int ship_y, int map_x, int map_y, ShapeRenderer renderer) {
        // Tile animation
        {
            animtimer++;
            if (animtimer > 24) {
                animflag++;
                if (animflag < 0 || animflag > 7)
                    animflag = 0;
                animtimer = 0;
            }
        }

        background.update();

        updateEnemies(ship_x / FACTOR, ship_y / FACTOR, map_x, map_y, renderer);

        updateDoors();
        updateSwitches();

        updateSmoke();
    }

    private void updateSmoke() {
        List<Smoke> todelete = new ArrayList<Smoke>();
        List<SmokeSource> todelete2 = new ArrayList<SmokeSource>();

        for (SmokeSource ss : smokesources) {
            ss.timer++;
            if (ss.timer > 256) {
                todelete2.add(ss);
            } else {
                int chance;

                chance = ss.timer;
                chance = (chance * chance) / 256;
                chance /= 16;

                if (MathUtils.random(chance + 2 - 1) == 0) {
                    Smoke s = new Smoke();

                    s.x = ss.x * FACTOR;
                    s.y = ss.y * FACTOR;

                    s.speed_x = ((random(1 + FACTOR / 16 - 1) - (FACTOR / 32))) + ss.speed_x;
                    s.speed_y = ((random(1 + FACTOR / 16 - 1) - (FACTOR / 32))) + ss.speed_y;
                    s.desired_x = (random(FACTOR / 4 - 1)) - FACTOR / 8;
                    s.desired_y = ((random(1 + FACTOR / 4 - 1) - (FACTOR / 8))) - FACTOR / 4;
                    s.timer = 0;

                    smokes.add(s);
                }
            }
        }

        smokesources.removeAll(todelete2);

        for (Smoke s : smokes) {
            s.timer++;
            s.x += s.speed_x;
            s.y += s.speed_y;
            if (s.speed_x > s.desired_x)
                s.speed_x -= 2;
            if (s.speed_x < s.desired_x)
                s.speed_x += 2;
            if (s.speed_y > s.desired_y)
                s.speed_y -= 1;
            if (s.speed_y < s.desired_y)
                s.speed_y += 1;
            if (s.timer > 255 || s.y < -8 * FACTOR) {
                todelete.add(s);
            }
        }

        smokes.removeAll(todelete);
    }

    private void updateSwitches() {
        for (Switch s : switches) {
            if (s.state > 0)
                s.state--;
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

    private void updateEnemies(int ship_x, int ship_y, int map_x, int map_y, ShapeRenderer renderer) {
        ArrayList<Enemy> enemiestodelete = new ArrayList<Enemy>();
        ArrayList<Enemy> newenemies = new ArrayList<Enemy>();

        for (Enemy e : enemies) {
            switch (e.type) {
            case BULLET:
                boolean collision = checkEnemyWithMapCollision(e, map_x, map_y, renderer);
                if (!e.cycleBullet(sx * 16, sy * 16, collision)) {
                    enemiestodelete.add(e);
                }
                break;
            case CANON:
                if (!e.cycle_canon(ship_x, ship_y, newenemies)) {
                    enemiestodelete.add(e);
                }
                break;
            case FAST_CANON:
                if (!e.cycle_fastcanon(ship_x, ship_y, newenemies)) {
                    enemiestodelete.add(e);
                }
                break;
            default:
                //TODO: tank, directional canons and so on
                break;
            }
        }

        enemies.removeAll(enemiestodelete);
        enemies.addAll(newenemies);
    }

    private boolean checkEnemyWithMapCollision(Enemy enemy, int map_x, int map_y, ShapeRenderer renderer) {
        int x = enemy.x / FACTOR - 32;
        int y = enemy.y / FACTOR - 32;
        int sx = 64;
        int sy = 64;

        int objectX = enemy.x / FACTOR - map_x;
        int objectY = enemy.y / FACTOR - map_y;

        int piece = enemy.getBulletTile();

        Polygon objectPolygon = Assets.assets.graphicAssets.tilePolygons[piece];
        if (objectPolygon == null) {
            return false;
        }

        objectPolygon.setPosition(objectX - 8, INTERNAL_SCREEN_HEIGHT - objectY + 8);
        return checkCollision(objectX, objectY, x, y, sx, sy, objectPolygon, enemy, renderer);
    }

    public void render(SpriteBatch batch, int x, int y, int ww, int wh) {
        stars.render(batch, x, y);
        background.render(batch, x, y, ww, wh);

        drawWalls(batch, x, y, ww, wh, null);

        renderSmoke(batch, x, y, ww, wh);

        drawEnemies(batch, x, y, ww, wh, null, null);
    }

    private void drawWalls(SpriteBatch batch, int x, int y, int ww, int wh, ICollisionChecker checker) {
        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        int step_x = tiles.get(0).originalWidth;
        int step_y = tiles.get(0).originalHeight;

        for (int j = 0, act_y = -y; j < sy; j++, act_y += step_y) {
            if (act_y >= -step_y && act_y < wh) {
                for (int i = 0, act_x = -x; i < sx; i++, act_x += step_x) {
                    if (act_x >= -step_x && act_x < ww) {
                        int piece = -1;

                        if (j >= 0) {
                            piece = map[i + j * sx];
                        }

                        piece = animpiece(piece);

                        if (piece >= 0) {
                            if (piece == 113 || piece == 114) {
                                // DOOR
                                int state = 0;

                                for (Door d : doors) {
                                    if ((d.x == i || d.x == i - 1) && d.y == j)
                                        state = d.state;
                                }

                                int offset = (piece == 113) ? -state : state;
                                drawWithOffset(act_x, act_y, offset, batch, piece, checker, step_y);
                            } else {
                                if ((piece >= 116 && piece < 120) || (piece >= 136 && piece < 140)
                                        || (piece >= 156 && piece < 160)) {
                                    // SWITCH
                                    for (Switch s : switches) {
                                        if (s.x == i && s.y == j) {
                                            int tileIndex;
                                            if (s.state != 0) {
                                                tileIndex = piece + 140;
                                            } else {
                                                tileIndex = piece;
                                            }
                                            if (batch != null) {
                                                batch.draw(tiles.get(tileIndex), act_x,
                                                        INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                            }
                                            if (checker != null) {
                                                checker.checkCollision(act_x, act_y, tileIndex);
                                            }
                                        }
                                    }
                                } else {
                                    if (batch != null) {
                                        batch.draw(tiles.get(piece), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                    }

                                    if (checker != null) {
                                        checker.checkCollision(act_x, act_y, piece);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawWithOffset(int act_x, int act_y, int offset, SpriteBatch batch, int piece,
            ICollisionChecker detector, int step_y) {
        int x = act_x;
        int y = INTERNAL_SCREEN_HEIGHT - act_y - step_y;

        AtlasRegion tile = Assets.assets.graphicAssets.tiles.get(piece);
        if (offset > 0 && offset < tile.getRegionWidth()) {
            Sprite sprite = new Sprite(tile);
            sprite.setX(x + offset);
            sprite.setY(y);
            sprite.setRegionWidth(sprite.getRegionWidth() - offset);
            if (batch != null) {
                sprite.draw(batch);
            }
            if (detector != null) {
                detector.checkCollision(act_x + offset, act_y, piece);
            }
        } else if (offset == 0) {
            if (batch != null) {
                batch.draw(tile, x, y);
            }

            if (detector != null) {
                detector.checkCollision(act_x, act_y, piece);
            }
        } else if (offset < 0) {
            Sprite sprite = new Sprite(tile);
            sprite.setX(x + offset);
            sprite.setY(y);
            sprite.setRegionWidth(sprite.getRegionWidth() + offset);

            if (batch != null) {
                sprite.draw(batch);
            }

            if (detector != null) {
                detector.checkCollision(act_x + offset, act_y, piece);
            }
        }
    }

    private void drawEnemies(SpriteBatch batch, int x, int y, int ww, int wh, Enemy enemy, ICollisionChecker detector) {
        for (Enemy e : enemies) {
            if (e != enemy) {
                switch (e.type) {
                case BULLET:
                    if (e.x > (-16 + x) * FACTOR && e.x < (ww + x) * FACTOR && e.y > (-16 + y) * FACTOR
                            && e.y < (wh + y) * FACTOR)
                        e.drawBullet(batch, x, y, detector);
                    break;
                case DIRECTIONAL_CANON:
                    if (e.x > (-16 + x) && e.x < (ww + x) && e.y > (-16 + y) && e.y < (wh + y))
                        e.drawDirectionalCanon(batch, map[(e.x) / 16 + (e.y / 16) * sx], x, y, detector);
                    break;
                case TANK:
                    if (e.x > (-32 + x) && e.x < (ww + x + 32) && e.y > (-32 + y) && e.y < (wh + y + 32))
                        e.drawTank(batch, x, y, detector);
                    break;
                case DESTOYED_TANK:
                    if (e.x > (-32 + x) && e.x < (ww + x + 32) && e.y > (-32 + y) && e.y < (wh + y + 32))
                        e.drawDestroyedTank(batch, x, y, detector);
                    break;
                case EXPLOSION:
                    if (e.x > (-16 + x) && e.x < (ww + x) && e.y > (-16 + y) && e.y < (wh + y))
                        e.drawExplosion(batch, x, y, detector);
                    break;
                case DIRECTIONAL_CANON_2:
                    if (e.x > (-16 + x) && e.x < (ww + x) && e.y > (-16 + y) && e.y < (wh + y))
                        e.drawDirectionalCanon2(batch, map[(e.x) / 16 + (e.y / 16) * sx], x, y, detector);
                    break;
                default:
                    break;
                }
            }
        }

    }

    private void renderSmoke(SpriteBatch batch, int x, int y, int ww, int wh) {
        if (batch == null)
            return;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        for (Smoke s : smokes) {
            int tile = ((s.timer) >> 3) % 3;
            int rx = (s.x / FACTOR) - x;
            int ry = (s.y / FACTOR) - y;
            if (rx > -16 && rx < ww && ry > -16 && ry < wh) {
                int alpha = 255 - s.timer;
                alpha = (alpha * alpha) / (255);

                if (alpha < 0)
                    alpha = 0;
                if (alpha > 255)
                    alpha = 255;

                AtlasRegion image = tiles.get(272 + tile);

                Sprite sprite = new Sprite(image);

                int x_ = rx + 8;
                int y_ = ry + 8;

                sprite.setCenter(x_, INTERNAL_SCREEN_HEIGHT - y_);

                sprite.draw(batch, alpha / 255.0f);
            }
        }
    }

    private int animpiece(int piece) {
        if (piece < 0)
            return piece;

        if (piece == 110 && animtimer > 16)
            return 111;
        if (piece == 110 && animtimer > 8)
            return 112;

        if (piece == 64 && animtimer > 16)
            return 66;
        if (piece == 64 && animtimer > 8)
            return 65;

        if (piece == 67 && (animflag & 0x01) == 1)
            return 69;
        if (piece == 68 && (animflag & 0x01) == 1)
            return 70;

        if (piece == 26 && animtimer > 12 && (animflag & 0x01) == 0)
            return 24;
        if (piece == 26 && animtimer > 12 && (animflag & 0x01) == 1)
            return 25;

        if (piece == 146 && animtimer > 12 && (animflag & 0x01) == 0)
            return 144;
        if (piece == 146 && animtimer > 12 && (animflag & 0x01) == 1)
            return 145;

        if (piece == 27 && (animflag & 0x01) == 1)
            return 28;
        if (piece == 147 && (animflag & 0x01) == 1)
            return 148;

        if (piece == 115 && animflag > 3)
            return -1;
        if (piece == 130 && animflag > 3)
            return -1;

        if (piece == 32 && animflag > 3)
            return 30;
        if (piece == 33 && animflag > 3)
            return 31;
        if (piece == 36 && animflag > 3)
            return 34;
        if (piece == 37 && animflag > 3)
            return 35;

        if (piece == 422 && animflag > 3)
            return 420;
        if (piece == 423 && animflag > 3)
            return 421;

        if (piece == 162 && animflag > 3)
            return 160;
        if (piece == 163 && animflag > 3)
            return 161;
        if (piece == 166 && animflag > 3)
            return 164;
        if (piece == 167 && animflag > 3)
            return 165;

        if (piece == 76 && animtimer <= 12 && (animflag & 0x03) == 0)
            return -1;
        if (piece == 76 && animtimer > 12 && (animflag & 0x03) == 0)
            return 79;
        if (piece == 76 && animtimer <= 12 && (animflag & 0x03) == 1)
            return 78;
        if (piece == 76 && animtimer > 12 && (animflag & 0x03) == 1)
            return 77;
        if (piece == 76 && animtimer > 12 && (animflag & 0x03) == 2)
            return 77;
        if (piece == 76 && animtimer <= 12 && (animflag & 0x03) == 3)
            return 78;
        if (piece == 76 && animtimer > 12 && (animflag & 0x03) == 3)
            return 79;

        if (piece == 150 && animtimer <= 12 && (animflag & 0x03) == 0)
            return -1;
        if (piece == 150 && animtimer > 12 && (animflag & 0x03) == 0)
            return 153;
        if (piece == 150 && animtimer <= 12 && (animflag & 0x03) == 1)
            return 152;
        if (piece == 150 && animtimer > 12 && (animflag & 0x03) == 1)
            return 151;
        if (piece == 150 && animtimer > 12 && (animflag & 0x03) == 2)
            return 151;
        if (piece == 150 && animtimer <= 12 && (animflag & 0x03) == 3)
            return 152;
        if (piece == 150 && animtimer > 12 && (animflag & 0x03) == 3)
            return 153;

        return piece;
    }

    public int collideShipBullet(int x, int y, int strength) {
        int retval = 0;

        Enemy selected = null;
        int mindistance = -1;

        for (Enemy e : enemies) {
            int ex = e.x;
            int ey = e.y;

            if (e.type == EnemyType.BULLET) {
                ex /= FACTOR;
                ey /= FACTOR;
            }

            if (e.type == EnemyType.CANON || e.type == EnemyType.FAST_CANON || e.type == EnemyType.DIRECTIONAL_CANON
                    || e.type == EnemyType.DIRECTIONAL_CANON_2) {
                ex += 8;
                ey += 8;
            }

            int distance = (x - ex) * (x - ex) + (y - ey) * (y - ey);

            int tolerance = 100;

            if (e.type == EnemyType.DIRECTIONAL_CANON || e.type == EnemyType.DIRECTIONAL_CANON_2
                    || (e.type == EnemyType.TANK && e.tank_type == 3))
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

        if (selected != null && selected.collision(strength)) {
            int generate_smoke = -1;

            if (selected.type != EnemyType.BULLET)
                retval = 2; // If it's not a bullet, then you have destroyed an enemy

            if (selected.type == EnemyType.CANON || selected.type == EnemyType.FAST_CANON
                    || selected.type == EnemyType.DIRECTIONAL_CANON || selected.type == EnemyType.DIRECTIONAL_CANON_2) {
                int x_, y_, i;

                x_ = selected.x / 16;
                y_ = selected.y / 16;
                i = x_ + y_ * sx;

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
                    ss.speed_x = 0;
                    ss.speed_y = -FACTOR / 4;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 1) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = 0;
                    ss.speed_y = FACTOR / 4;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 2) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = FACTOR / 4;
                    ss.speed_y = 0;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 3) {
                    SmokeSource ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = -FACTOR / 4;
                    ss.speed_y = 0;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                Assets.assets.soundAssets.explosion.play();
            }

            if (selected.type != EnemyType.TANK && selected.type != EnemyType.DESTOYED_TANK
                    && selected.type != EnemyType.EXPLOSION) {
                if (selected.type == EnemyType.BULLET) {
                    selected.type = EnemyType.EXPLOSION;
                    selected.state = 0;
                } else {
                    selected.state = -1;
                }
            } else {
                if (selected.type == EnemyType.TANK) {
                    Assets.assets.soundAssets.explosion.play();
                    selected.type = EnemyType.DESTOYED_TANK;
                    selected.state = 0;
                    generate_smoke = 0;
                    if (generate_smoke == 0) {
                        SmokeSource ss = new SmokeSource();
                        ss.x = selected.x;
                        ss.y = selected.y;
                        ss.speed_x = 0;
                        ss.speed_y = -FACTOR / 4;
                        ss.timer = 0;
                        smokesources.add(ss);
                    }
                }
            }
        }

        return retval;
    }

    public int getCols() {
        return sx;
    }

    public int getRows() {
        return sy;
    }

    public int getBallPositionX() {
        for (int i = 0; i < sx * sy; i++) {
            if (map[i] == 110)
                return i % sx;
        }
        return 0;
    }

    public int getBallPositionY() {
        for (int i = 0; i < sx * sy; i++) {
            if (map[i] == 110)
                return i / sx;
        }
        return 0;
    }

    public void takeBall() {
        for (Door d : doors) {
            // The doors with event==0 are activated when the ball is taken 
            if (d.event == 0)
                d.activate();
        }
    }

    public void collideBall(int x, int y) {
        Switch selected = null;
        int mindistance = -1;

        for (Switch s : switches) {
            int distance = (x - (s.x * 16 + 8)) * (x - (s.x * 16 + 8)) + (y - (s.y * 16 + 8)) * (y - (s.y * 16 + 8));
            if ((mindistance == -1 && distance < 64) || distance < mindistance) {
                selected = s;
                mindistance = distance;
            }
        }

        if (selected != null) {
            selected.state = 16;
            Assets.assets.soundAssets.switchship.play();
            for (Door d : doors) {
                if (d.event == selected.number)
                    d.activate();
            }
        }
    }

    public boolean isShipInFuelRecharge(int ship_x, int ship_y) {
        for (FuelRecharge f : fuel_recharges) {
            if (ship_x >= f.x * 16 && ship_x < (f.x * 16 + 32) && ship_y >= f.y * 16 && ship_y < (f.y * 16 + 32))
                return true;
        }
        return false;
    }

    public boolean checkCollision(int objectX, int objectY, int x, int y, int sx, int sy, Polygon objectPolygon,
            Enemy enemy, ShapeRenderer renderer) {
        if (objectPolygon == null)
            return false;

        Polygon[] objectPolygons = CollisionDetectionUtils.tiangulate(objectPolygon);

        ICollisionChecker checker = new CollisionChecker(renderer, objectX, objectY, objectPolygons);

        if (renderer != null) {
            CollisionDetectionUtils.drawPolygons(renderer, objectPolygons);
        }

        drawWalls(null, x, y, sx, sy, checker);

        if (checker.wasCollision())
            return true;

        drawEnemies(null, x, y, sx, sy, enemy, checker);

        return checker.wasCollision();
    }

}
