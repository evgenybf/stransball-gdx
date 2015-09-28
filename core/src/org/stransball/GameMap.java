package org.stransball;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.stransball.objects.DOOR;
import org.stransball.objects.ENEMY;
import org.stransball.objects.FUELRECHARGE;
import org.stransball.objects.SWITCH;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class GameMap {

    private static final int EMPTY_ROWS = 4;

    private int[] map;
    private int sx;
    private int sy;
    private int background_type;
    private int animtimer;
    private int animflag;

    private ArrayList<ENEMY> enemies;
    private ArrayList<DOOR> doors;
    private ArrayList<SWITCH> switches;
    private int switchnumber;
    private ArrayList<FUELRECHARGE> fuel_recharges;

    // TODO: Move to MapLoader
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
        enemies = new ArrayList<ENEMY>();
        doors = new ArrayList<DOOR>();
        switches = new ArrayList<SWITCH>();
        switchnumber = 0;
        fuel_recharges = new ArrayList<FUELRECHARGE>();

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
                    ENEMY e = new ENEMY();
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
                    ENEMY e = new ENEMY();
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
                    ENEMY e = new ENEMY();
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
                    ENEMY e = new ENEMY();
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
                DOOR d = new DOOR();

                d.x = i % sx;
                d.y = i / sx;
                d.action = 0;

                d.state = scanner.nextInt();
                d.event = scanner.nextInt();
                doors.add(d);
            }

            // SWITCHES:
            if ((map[i] >= 116 && map[i] < 120) || (map[i] >= 136 && map[i] < 140) || (map[i] >= 156 && map[i] < 160)) {
                SWITCH s = new SWITCH();
                s.x = i % sx;
                s.y = i / sx;
                s.number = switchnumber++;
                s.state = 0;
                switches.add(s);
            }

            // FUEL RECHARGES:
            if (map[i] == 132) {
                FUELRECHARGE f = new FUELRECHARGE();
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
                ENEMY e = new ENEMY();

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
        animtimer++;
        if (animtimer > 24) {
            animflag++;
            if (animflag < 0 || animflag > 7)
                animflag = 0;
            animtimer = 0;
        }

    }

    public void drawWithoutEnemies(SpriteBatch batch, ShapeRenderer shapeRenderer, int x, int y, int ww, int wh,
            IPolygonDetector detector) {

        int step_x = 0, step_y = 0;
        int act_x, act_y;
        int i, j;

        Array<AtlasRegion> tiles = Assets.assets.shipAssets.tiles;
        Polygon[] tilesPolygons = Assets.assets.shipAssets.tilePolygons;

        step_x = tiles.get(0).originalWidth;
        step_y = tiles.get(0).originalHeight;

        /* Draw map: */
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

    public int get_sx() {
        return sx;
    }

    public int get_sy() {
        return sy;
    }

}
