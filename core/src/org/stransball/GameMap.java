package org.stransball;

import static com.badlogic.gdx.math.MathUtils.random;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.stransball.objects.Door;
import org.stransball.objects.Enemy;
import org.stransball.objects.FuelRecharge;
import org.stransball.objects.Smoke;
import org.stransball.objects.SmokeSource;
import org.stransball.objects.Switch;

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
    @SuppressWarnings("unused")
    private int background_type;
    private int animtimer;
    private int animflag;

    private List<Enemy> enemies;
    private List<Door> doors;
    private List<Switch> switches;
    private int switchnumber;
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
        int i;

        enemies = new ArrayList<Enemy>();
        doors = new ArrayList<Door>();
        switches = new ArrayList<Switch>();
        switchnumber = 0;
        fuel_recharges = new ArrayList<FuelRecharge>();
        smokesources = new ArrayList<SmokeSource>();
        smokes = new ArrayList<Smoke>();

        sx = scanner.nextInt();
        sy = scanner.nextInt();
        sy += EMPTY_ROWS;

        map = new int[sx * sy];

        Arrays.fill(map, -1);

        for (i = sx * EMPTY_ROWS; i < sx * sy; i++) {
            map[i] = scanner.nextInt();
            map[i]--;
        }

        // Look for enemies, doors, etc.:
        for (i = 0; i < sx * sy; i++) {

            // ENEMIES:
            if ((map[i] >= 176 && map[i] < 180) || (map[i] >= 196 && map[i] < 200) || (map[i] >= 216 && map[i] < 220)
                    || (map[i] >= 236 && map[i] < 240)
                    || (map[i] == 154 || map[i] == 155 || map[i] == 174 || map[i] == 175)
                    || (map[i] == 386 || map[i] == 387 || map[i] == 406 || map[i] == 407)) {
                int x, y, direction;

                direction = 0;

                x = (i % sx) * 16;
                y = (i / sx) * 16;

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
                    Enemy e = new Enemy();
                    e.type = 1;
                    e.state = 0;
                    e.life = 4;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    enemies.add(e);
                }

                if (map[i] >= 196 && map[i] < 200) {
                    // FAST CANON:
                    Enemy e = new Enemy();
                    e.type = 2;
                    e.state = 0;
                    e.life = 8;
                    e.x = x;
                    e.y = y;
                    e.direction = direction;
                    enemies.add(e);
                }

                if (map[i] == 154 || map[i] == 155 || map[i] == 174 || map[i] == 175) {
                    // DIRECTIONAL CANON:
                    Enemy e = new Enemy();
                    e.type = 3;
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
                    Enemy e = new Enemy();
                    e.type = 7;
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
            for (i = 0; i < ntanks; i++) {
                Enemy e = new Enemy();

                int x, y, type;

                x = scanner.nextInt();
                y = scanner.nextInt();
                type = scanner.nextInt();

                x *= 16;
                y *= 16;

                e.type = 4;
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

        background_type = scanner.nextInt();

        animtimer = 0;
        animflag = 0;
    }

    public void update(float delta) {
        // Tile animation
        animtimer++;
        if (animtimer > 24) {
            animflag++;
            if (animflag < 0 || animflag > 7)
                animflag = 0;
            animtimer = 0;
        }

        // Smoke
        {
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

            for (SmokeSource ss : todelete2) {
                smokesources.remove(ss);
            }

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

            for (Smoke s : todelete) {
                smokes.remove(s);
            }
        }
    }

    public void drawWithoutEnemies(SpriteBatch batch, ShapeRenderer shapeRenderer, int x, int y, int ww, int wh,
            IPolygonDetector detector) {

        int step_x = 0, step_y = 0;
        int act_x, act_y;
        int i, j;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;
        Polygon[] tilesPolygons = Assets.assets.graphicAssets.tilePolygons;

        step_x = tiles.get(0).originalWidth;
        step_y = tiles.get(0).originalHeight;

        if (batch != null) {
            /* Draw Background: */
            for (j = 0, act_y = -(int) (y * 0.75F); j < sy; j++, act_y += step_y) {
                if (act_y > -step_y && act_y < wh) {
                    for (i = 0, act_x = -(int) (x * 0.75F); i < sx; i++, act_x += step_x) {
                        if (act_x > -step_x && act_x < ww) {
                            switch (background_type) {
                            case 0:
                                if (j == 10)
                                    batch.draw(tiles.get(294), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                if (j > 10)
                                    batch.draw(tiles.get(314), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                break;
                            case 1:
                                if (j == 10)
                                    batch.draw(tiles.get(295), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                if (j > 10)
                                    batch.draw(tiles.get(315), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                break;
                            case 2:
                                if (j == 10)
                                    batch.draw(tiles.get(335), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                if (j > 10) {
                                    if (((j >> 1) & 0x03) == 0) {
                                        if (animflag < 2) {
                                            int t[] = { 316, 317, 318, 319, 336, 337, 338, 339, 358, 359, 378, 379 };
                                            int step;
                                            step = (animtimer + animflag * 24) / 4;
                                            if (step > 11)
                                                step = 11;
                                            batch.draw(tiles.get(t[step]), act_x,
                                                    INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                        } else {
                                            batch.draw(tiles.get(316), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                        }
                                    } else {
                                        batch.draw(tiles.get(275), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Draw map:
        for (j = 0, act_y = -y; j < sy; j++, act_y += step_y) {
            if (act_y >= -step_y && act_y < wh) {
                for (i = 0, act_x = -x; i < sx; i++, act_x += step_x) {
                    if (act_x >= -step_x && act_x < ww) {
                        int piece = -1;

                        if (j >= 0)
                            piece = map[i + j * sx];

                        piece = animpiece(piece);
                        if (piece >= 0) {
                            if (piece == 113 || piece == 114) {
                                // TODO: DOOR
                            } else {
                                if ((piece >= 116 && piece < 120) || (piece >= 136 && piece < 140)
                                        || (piece >= 156 && piece < 160)) {
                                    // TODO: SWITCH
                                } else {

                                    if (batch != null) {
                                        batch.draw(tiles.get(piece), act_x,
                                                INTERNAL_SCREEN_HEIGHT - act_y - /*FIXME: !!!*/ step_y);
                                    }
                                    if (shapeRenderer != null) {
                                        Polygon poly = tilesPolygons[piece];
                                        if (poly != null) {
                                            poly.setPosition(act_x,
                                                    Constants.INTERNAL_SCREEN_HEIGHT - act_y /*- step_y*/);

                                            shapeRenderer.polygon(poly.getTransformedVertices());
                                        }
                                    }

                                    if (detector != null) {
                                        detector.detect(act_x, act_y, piece);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        renderSmoke(batch, x, y, ww, wh);
    }

    private void renderSmoke(SpriteBatch batch, int x, int y, int ww, int wh) {
        if (batch == null)
            return;

        Array<AtlasRegion> tiles;
        // Draw smoke

        tiles = Assets.assets.graphicAssets.tiles;
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

    int animpiece(int piece) {
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

    public int shipbullet_collision(int x, int y, int strength) {
        Enemy selected = null;
        int mindistance = -1;
        int tolerance;
        int retval = 0;

        for (Enemy e : enemies) {
            int ex, ey, distance;
            ex = e.x;
            ey = e.y;
            if (e.type == 0) {
                ex /= FACTOR;
                ey /= FACTOR;
            }
            if (e.type == 1 || e.type == 2 || e.type == 3 || e.type == 7) {
                ex += 8;
                ey += 8;
            }
            distance = (x - ex) * (x - ex) + (y - ey) * (y - ey);

            tolerance = 100;
            if (e.type == 3 || e.type == 7 || (e.type == 4 && e.tank_type == 3))
                tolerance = 200;

            if (((mindistance == -1 && distance < tolerance) || distance < mindistance)
                    && (e.type == 0 || e.type == 1 || e.type == 2 || e.type == 3 || e.type == 4 || e.type == 7)) {
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

            if (selected.type != 0)
                retval = 2; // If it's not a bullet, then you have destroyed an enemy

            if (selected.type == 1 || selected.type == 2 || selected.type == 3 || selected.type == 7) {
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
                    SmokeSource ss;
                    ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = 0;
                    ss.speed_y = -FACTOR / 4;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 1) {
                    SmokeSource ss;
                    ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = 0;
                    ss.speed_y = FACTOR / 4;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 2) {
                    SmokeSource ss;
                    ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = FACTOR / 4;
                    ss.speed_y = 0;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                if (generate_smoke == 3) {
                    SmokeSource ss;
                    ss = new SmokeSource();
                    ss.x = selected.x + 6;
                    ss.y = selected.y + 6;
                    ss.speed_x = -FACTOR / 4;
                    ss.speed_y = 0;
                    ss.timer = 0;
                    smokesources.add(ss);
                }
                Assets.assets.soundAssets.explosion.play();
            }

            if (selected.type != 4 && selected.type != 5 && selected.type != 6) {
                if (selected.type == 0) {
                    selected.type = 6;
                    selected.state = 0;
                } else {
                    selected.state = -1;
                }
            } else {
                if (selected.type == 4) {
                    Assets.assets.soundAssets.explosion.play();
                    selected.type = 5;
                    selected.state = 0;
                    generate_smoke = 0;
                    if (generate_smoke == 0) {
                        SmokeSource ss;
                        ss = new SmokeSource();
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

    public int get_sx() {
        return sx;
    }

    public int get_sy() {
        return sy;
    }

    public int get_ball_position_x() {
        for (int i = 0; i < sx * sy; i++) {
            if (map[i] == 110)
                return i % sx;
        }
        return 0;
    }

    public int get_ball_position_y() {
        for (int i = 0; i < sx * sy; i++) {
            if (map[i] == 110)
                return i / sx;
        }
        return 0;
    }

    public void ball_taken() {
        for (Door d : doors) {
            // The doors with event==0 are activated when the ball is taken 
            if (d.event == 0)
                d.activate();
        }
    }

    public void ball_collision(int x, int y) {
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
}
