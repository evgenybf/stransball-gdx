package org.stransball.objects;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.atan2;
import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.degreesToRadians;
import static com.badlogic.gdx.math.MathUtils.radiansToDegrees;
import static com.badlogic.gdx.math.MathUtils.sin;
import static java.lang.Math.abs;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.DEBUG_SHOW_TANK_TRACK_COLLISION;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.util.DebugUtils.passDebugRenderer;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;
import org.stransball.util.CollisionDetectionUtils;
import org.stransball.util.Triangulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class EnemyTank extends Enemy {

    private int state2;

    public EnemyTank(GameMap map) {
        super(EnemyType.TANK, map);
        state2 = 0;
        turretAngle = 90;
        tankAngle = 0;
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer) {

        TankAndGroundCollider tankCollider = new TankAndGroundCollider(
                passDebugRenderer(renderer, DEBUG_SHOW_TANK_TRACK_COLLISION), x - 32, y - 32, mapXScreen, mapYScreen);

        map.drawWalls(null, x - 32, y - 32, 64, 64, tankCollider);

        int gdist1 = tankCollider.gdist1;
        int gdist2 = tankCollider.gdist2;
        boolean lcol = tankCollider.lcol;
        boolean rcol = tankCollider.rcol;

        updateTank(shipXScreenF, shipYScreenF, shipSpeedX, shipSpeedY, gdist1, gdist2, lcol, rcol, newEnemies);
    }

    private boolean updateTank(int ship_x, int ship_y, int ship_sx, int ship_sy, int gdist1, int gdist2, boolean lcol,
            boolean rcol, List<Enemy> enemies) {
        int old_tank_angle = tankAngle;

        // Tracks motion: angle, gravity, collision and movement
        if (gdist1 > gdist2) {
            float dif = (float) (gdist1 - gdist2) / 16.0F;
            float radians = (float) (atan2(dif, 1));

            tankAngle = (int) (-radians * radiansToDegrees);
        }

        if (gdist1 < gdist2) {
            float dif = (float) (gdist2 - gdist1) / 16.0F;
            float radians = (float) (atan2(dif, 1));

            tankAngle = (int) (radians * radiansToDegrees);
        }

        if (gdist1 == gdist2)
            tankAngle = 0;

        if (abs(old_tank_angle - tankAngle) > 2) {
            if (old_tank_angle < tankAngle)
                tankAngle = old_tank_angle + 2;
            else
                tankAngle = old_tank_angle - 2;
        }

        if (((gdist1 + gdist2) / 2) > 0) {
            y++;
            gdist1--;
            gdist2--;
        }
        if (((gdist1 + gdist2) / 2) < 0) {
            y--;
            gdist1++;
            gdist2++;
        }

        state2++;

        if (((gdist1 + gdist2) / 2) == 0) {
            if (tankType < 3) {
                switch (state) {
                case 1:
                    if ((state2 & 0x03) == 0) {
                        if (rcol) {
                            state = -1;
                            x--;
                        } else {
                            x++;
                        }
                    }
                    break;
                case -1:
                    if ((state2 & 0x03) == 0) {
                        if (lcol) {
                            state = 1;
                            x++;
                        } else {
                            x--;
                        }
                    }
                    break;
                }
            } else {
                if ((state2 & 0x03) == 0) {
                    if (x + 32 < ship_x && !rcol) {
                        x++;
                        state++;
                    }
                    if (x - 32 > ship_x && !lcol) {
                        x--;
                        state--;
                        if (state < 0)
                            state += 256;
                    }
                }
            }

        }

        // Turret's angle:
        {
            int dx = ship_x - x;
            int dy = ship_y - y;
            float radians;

            if (tankType < 3) {
                radians = (float) (atan2(-(float) (dy), (float) (dx)));
                turretAngle = (int) (radians * radiansToDegrees);
            } else {
                int i;
                float angle_to_ship;
                int desired_turret_angle;

                radians = (float) (atan2((float) (dy), (float) (dx)));
                angle_to_ship = radians;

                if (ship_sx == 0 && ship_sy == 0) {
                    desired_turret_angle = (int) (radians * radiansToDegrees);
                } else {
                    float alpha = 0, best_alpha = 0, min_error = 10000;
                    float s_sx = (float) (ship_sx) / FACTOR, s_sy = (float) (ship_sy) / FACTOR;

                    float error = 0;
                    float d, ls, lb;
                    float b_sx, b_sy;
                    float min, max;

                    min = 3.5779242F;
                    max = 6.0213847F;

                    // Compute the error given an angle "alpha":
                    for (alpha = min; alpha < max; alpha += 0.02F) {
                        b_sx = (float) (cos(alpha));
                        b_sy = (float) (sin(alpha));

                        d = s_sy * b_sx - s_sx * b_sy;
                        if (d != 0) {
                            ls = (dx * b_sy - dy * b_sx) / d;
                            lb = (s_sy * dx - s_sx * dy) / d;

                            if (lb > 0) {
                                error = (float) (abs(ls - lb));
                            } else {
                                error = 10000;
                            }
                        } else {
                            error = 10000;
                        }

                        if (error < min_error) {
                            best_alpha = alpha;
                            min_error = error;
                        }
                    }

                    if (angle_to_ship < 0)
                        angle_to_ship += 6.283184F;
                    if (best_alpha < 0)
                        best_alpha += 6.283184F;

                    if ((float) (abs(angle_to_ship - best_alpha)) > (PI / 2)
                            && (float) (abs(angle_to_ship - best_alpha)) < ((PI * 3) / 2)) {
                        float d_ = angle_to_ship - best_alpha;

                        if (d_ > 0 && d_ < PI)
                            best_alpha = angle_to_ship - (PI / 2);
                        else
                            best_alpha = angle_to_ship + (PI / 2);
                    }

                    desired_turret_angle = (int) ((best_alpha * 180) / PI);
                    radians = best_alpha;
                }

                while (desired_turret_angle < 0) {
                    desired_turret_angle += 360;
                }
                while (desired_turret_angle >= 360) {
                    desired_turret_angle -= 360;
                }
                desired_turret_angle = 360 - desired_turret_angle;
                for (i = 0; i < 2; i++) {
                    if (turretAngle < 0)
                        turretAngle += 360;
                    if (turretAngle >= 360)
                        turretAngle -= 360;
                    if (desired_turret_angle != turretAngle) {
                        if (((desired_turret_angle - turretAngle) < 180 && (desired_turret_angle - turretAngle) > 0)
                                || (desired_turret_angle - turretAngle) < -180) {
                            turretAngle++;
                        } else {
                            turretAngle--;
                        }
                    }
                }
                if (turretAngle < 0 || turretAngle > 270)
                    turretAngle = 0;
                if (turretAngle > 180)
                    turretAngle = 180;
                radians = (float) (turretAngle * degreesToRadians);
            }

            if ((tankType < 3 && state2 >= 128) || (tankType == 3 && state2 >= 96)) {
                if (turretAngle > 15 && turretAngle < 175) {
                    if (dx * dx + y * dy < 30000) {
                        // Fire!:
                        Enemy e = new EnemyBullet(map);
                        e.state = 8;
                        e.speedX = (int) (cos(radians) * FACTOR);
                        e.speedY = -(int) (sin(radians) * FACTOR);
                        if (tankType < 3) {
                            e.x = x * FACTOR + (e.speedX * 6);
                            e.y = y * FACTOR + (e.speedY * 6);
                        } else {
                            e.x = x * FACTOR + (e.speedX * 12);
                            e.y = y * FACTOR + (e.speedY * 12);
                        }
                        e.life = 1;
                        e.tileIndex = 344;

                        fixPosition(e);

                        enemies.add(e);
                        Assets.assets.soundAssets.shot.play();
                    }
                }
                state2 = 0;
            }
        }

        return true;
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-32 + mapXScreen) && x < (screenWidth + mapXScreen + 32) && y > (-32 + mapYScreen)
                && y < (screenHeight + mapYScreen + 32)) {
            drawTank(batch, mapXScreen, mapYScreen, detector);
        }
    }

    private void drawTank(SpriteBatch batch, int mapXScreen, int mapYScreen, ICollisionDetector detector) {
        int tmp = 0;

        if ((state2 & 0x8) == 0)
            tmp = 2;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;
        Polygon[] tilePolygons = assets.graphicAssets.tilePolygons;

        int dx = (x - mapXScreen) - 24;
        int dy = (y - mapYScreen) - 16;

        int tankAngle_ = 360 - tankAngle;
        int turretAngle_ = -turretAngle;

        int t1idx;
        int t2idx;

        //
        // 1. Tank tracks:

        if (tankType < 3) {
            t1idx = 282 + 4 * tankType + tmp;
            t2idx = 283 + 4 * tankType + tmp;
        } else {
            t1idx = 461 + ((state / 2) % 4) * 2;
            t2idx = 462 + ((state / 2) % 4) * 2;
        }

        if (batch != null) {
            {
                Sprite s1 = new Sprite(tiles.get(t1idx));
                s1.setOrigin(16, 8);
                s1.setPosition(dx + 0 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s1.setRotation(tankAngle_);
                s1.draw(batch);
            }
            {
                Sprite s2 = new Sprite(tiles.get(t2idx));
                s2.setOrigin(16 - 16, 8);
                s2.setRotation(tankAngle_);
                s2.setPosition(dx + 16 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s2.draw(batch);
            }
        }

        if (detector != null) {
            {
                Polygon p1 = tilePolygons[t1idx];
                p1.setOrigin(16, 8);
                p1.setPosition(dx + 0 + 8, (dy + 0 + 30));
                p1.setRotation(tankAngle_);
                detector.handlePolygon(p1);
            }
            {
                Polygon p2 = tilePolygons[t2idx];
                p2.setOrigin(16 - 16, 8);
                p2.setRotation(tankAngle_);
                p2.setPosition(dx + 16 + 8, (dy + 0 + 30));
                detector.handlePolygon(p2);
            }
        }

        //
        // 2. Turret:

        if (tankType < 3) {
            {
                int t3idx = -1;
                if (turretAngle < 37) {
                    t3idx = 271;
                } else if (turretAngle >= 37 && turretAngle < 53) {
                    t3idx = 270;
                } else if (turretAngle >= 53 && turretAngle < 75) {
                    t3idx = 269;
                } else if (turretAngle >= 75 && turretAngle < 105) {
                    t3idx = 268;
                } else if (turretAngle >= 105 && turretAngle < 127) {
                    t3idx = 267;
                } else if (turretAngle >= 127 && turretAngle < 143) {
                    t3idx = 266;
                } else if (turretAngle >= 143) {
                    t3idx = 265;
                }

                if (batch != null) {
                    if (t3idx >= 0) {
                        Sprite s3 = new Sprite(tiles.get(t3idx));
                        s3.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 8 + 14));
                        s3.draw(batch);
                    }
                }

                if (detector != null) {
                    if (t3idx >= 0) {
                        Polygon p3 = tilePolygons[t3idx];
                        p3.setPosition(dx + 16, (dy + 8 + 14));
                        detector.handlePolygon(p3);
                    }
                }
            }

            {
                int t4idx = 262 + tankType;
                if (batch != null) {
                    Sprite s4 = new Sprite(tiles.get(t4idx));
                    s4.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 8 + 14));
                    s4.draw(batch);
                }

                if (detector != null) {
                    Polygon p4 = tilePolygons[t4idx];
                    p4.setPosition(dx + 16, dy + 8 + 14);
                    detector.handlePolygon(p4);
                }
            }
        } else {
            {
                int t5idx = 334;

                if (batch != null) {
                    Sprite s5 = new Sprite(tiles.get(t5idx));
                    s5.setOrigin(0, 4);
                    s5.setScale(0.75f);
                    s5.setRotation(turretAngle_);
                    s5.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 14));
                    s5.draw(batch);
                }

                if (detector != null) {
                    Polygon p5 = tilePolygons[t5idx];
                    p5.setOrigin(0, 4);
                    p5.setScale(0.75f, 0.75f);
                    p5.setRotation(turretAngle_);
                    p5.setPosition(dx + 16, (dy + 14));
                    detector.handlePolygon(p5);
                }
            }

            {
                int t6idx = 460;
                if (batch != null) {
                    Sprite s6 = new Sprite(tiles.get(t6idx));
                    s6.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 6 + 14));
                    s6.draw(batch);
                }

                if (detector != null) {
                    Polygon p6 = tilePolygons[t6idx];
                    p6.setPosition(dx + 16, (dy + 6 + 14));
                    detector.handlePolygon(p6);
                }
            }
        }
    }

    public EnemyDestroyedTank toDestroyedTank() {
        EnemyDestroyedTank enemy = new EnemyDestroyedTank(map, state2);
        copyTo(enemy);
        enemy.state = 0;
        return enemy;
    }

    private final class TankAndGroundCollider implements ICollisionDetector {

        private final ShapeRenderer renderer;
        private final int regionXScreenF;
        private final int regionYcreenF;
        private final int mapXScreen;
        private final int mapYScreen;
        public boolean rcol;
        public boolean lcol;
        public int gdist2;
        public int gdist1;
        private final Triangulator triangulator;

        public TankAndGroundCollider(ShapeRenderer renderer, int regionXScreenF, int regionYcreenF, int mapXScreen,
                int mapYScreen) {
            this.renderer = renderer;
            this.regionXScreenF = regionXScreenF;
            this.regionYcreenF = regionYcreenF;
            this.mapXScreen = mapXScreen;
            this.mapYScreen = mapYScreen;
            this.triangulator = new Triangulator();

            gdist1 = 19;
            gdist2 = 19;
            lcol = false;
            rcol = false;
        }

        @Override
        public void handlePolygon(Polygon poly) {
            poly.setPosition(regionXScreenF + poly.getX() - mapXScreen,
                    INTERNAL_SCREEN_HEIGHT - (regionYcreenF + poly.getY() - mapYScreen));

            Iterable<Polygon> polygons = triangulator.tiangulate(poly);

            int dx = (x - mapXScreen) - 24;
            int dy = (y - mapYScreen) - 16;

            int result = 0;
            for (int i = -19; i < 19; ++i) {
                result = drawTracks(dx, dy + i, polygons, true, result);
                if ((result & 0x01) != 0 && gdist1 > i) {
                    gdist1 = i;
                }
                if ((result & 0x02) != 0 && gdist2 > i) {
                    gdist2 = i;
                }
            }

            if (drawTracks(dx - 5, dy - 11, polygons, true, 0x00) != 0)
                lcol = true;
            if (drawTracks(dx + 5, dy - 11, polygons, true, 0x00) != 0)
                rcol = true;
        }

        @Override
        public void handlePolygon(int actXScreen, int actYScreen, int tileIndex) {
            Polygon poly = Assets.assets.graphicAssets.tilePolygons[tileIndex];
            // Some objects like bullets can do not have contours in some states like
            // explosion and so on
            if (poly == null)
                return;

            poly.setPosition(actXScreen, actYScreen);

            handlePolygon(poly);
        }

        public int drawTracks(int dx, int dy, Iterable<Polygon> polygons, boolean drawpoly, int prevResult) {
            int result = prevResult;

            Polygon trackPolygon = Assets.assets.graphicAssets.tankTrackPolygon;

            if ((prevResult & 0x01) == 0) {
                trackPolygon.setPosition(dx + 0 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                trackPolygon.setOrigin(16, 8);

                if (CollisionDetectionUtils.overlapPolygons(trackPolygon, polygons)) {
                    result |= 0x01;
                    if (renderer != null && drawpoly) {
                        renderer.setColor(Color.GOLD);
                        renderer.polygon(trackPolygon.getTransformedVertices());
                    }
                }
            }

            if ((prevResult & 0x02) == 0) {
                trackPolygon.setPosition(dx + 16 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                trackPolygon.setOrigin(16, 8);

                if (CollisionDetectionUtils.overlapPolygons(trackPolygon, polygons)) {
                    result |= 0x02;
                    if (renderer != null && drawpoly) {
                        renderer.setColor(Color.GOLD);
                        renderer.polygon(trackPolygon.getTransformedVertices());
                    }
                }
            }

            return result;
        }
    }

}
