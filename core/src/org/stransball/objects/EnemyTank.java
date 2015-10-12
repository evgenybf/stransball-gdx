package org.stransball.objects;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.atan2;
import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.degreesToRadians;
import static com.badlogic.gdx.math.MathUtils.radiansToDegrees;
import static com.badlogic.gdx.math.MathUtils.sin;
import static java.lang.Math.abs;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

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
    public void update(int ship_x, int ship_y, int ship_speed_x, int ship_speed_y, int mapXScreen, int mapYScreen,
            List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer) {
        //TODO: Not implemented yet

        int gdist1 = -1, gdist2 = -1;
        boolean lcol = false, rcol = false;
        // TANK: 
        //        draw_tank(enemy_sfc, masks, e.x - 32, e.y - 32);
        //        draw_map_noenemies(back_sfc, masks, e.x - 32, e.y - 32, 64, 64);

        // Compute the distance of the tracks to the ground:  
        //        for (int i = 32; i < 64 && (gdist1 == -1 || gdist2 == -1); i++) {
        //            if (getpixel(back_sfc, 24, i) != 0 && gdist1 == -1)
        //                gdist1 = i - 45;
        //            if (getpixel(back_sfc, 40, i) != 0 && gdist2 == -1)
        //                gdist2 = i - 45;
        //        }
        if (gdist1 == -1)
            gdist1 = 19;
        if (gdist2 == -1)
            gdist2 = 19;
        //        if (getpixel(back_sfc, 16, 28) != 0)
        //                    lcol = true;
        //        if (getpixel(back_sfc, 48, 28) != 0)
        //                    rcol = true;

        cycle_tank(ship_x, ship_y, ship_speed_x, ship_speed_y, gdist1, gdist2, lcol, rcol, newEnemies);
    }

    boolean cycle_tank(int ship_x, int ship_y, int ship_sx, int ship_sy, int gdist1, int gdist2, boolean lcol,
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
            //            y++; //FIXME
            gdist1--;
            gdist2--;
        }
        if (((gdist1 + gdist2) / 2) < 0) {
            //            y--; //FIXME
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
        if (x > (-32 + x) && x < (mapXScreen + x + 32) && y > (-32 + y) && y < (mapYScreen + y + 32))
            draw_tank(batch, mapXScreen, mapYScreen, detector);
    }

    private void draw_tank(SpriteBatch batch, int map_x, int map_y, ICollisionDetector detector) {
        int tmp = 0;

        if ((state2 & 0x8) == 0)
            tmp = 2;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;
        Polygon[] tilePolygons = assets.graphicAssets.tilePolygons;

        int dx = (x - map_x) - 24;
        int dy = (y - map_y) - 16;

        int tankAngle_ = tankAngle;
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
                //draw(0,0,tank_sfc);
                Sprite s1 = new Sprite(tiles.get(t1idx));
                s1.setOrigin(16, 8);
                s1.setPosition(dx + 0 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s1.rotate(tankAngle_);
                s1.draw(batch);
            }
            {
                //draw(16,0,tank_sfc);
                Sprite s2 = new Sprite(tiles.get(t2idx));
                s2.setOrigin(16, 8);
                s2.rotate(tankAngle_);
                s2.setPosition(dx + 16 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s2.draw(batch);
            }
            //sge_transform(tank_sfc,tank_sfc2, (float)(tankAngle), 1.0F, 1.0F, 16, 8, 24, 24, 0);
        }

        if (detector != null) {
            {
                Polygon p1 = tilePolygons[t1idx];
                p1.setOrigin(16, 8);
                p1.setPosition(dx + 0 + 8, (dy + 0 + 30));
                p1.rotate(tankAngle_);
                detector.handlePolygon(p1);
            }
            {
                Polygon p2 = tilePolygons[t2idx];
                p2.setOrigin(16, 8);
                p2.rotate(tankAngle_);
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
                        //draw(16,8,tank_sfc3);
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
                    //.draw(16,8,tank_sfc3);
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
                    //.draw(0,0,canon_sfc);
                    Sprite s5 = new Sprite(tiles.get(t5idx));
                    s5.setOrigin(0, 4);
                    s5.setScale(0.75f);
                    s5.rotate(turretAngle_);
                    s5.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 14));
                    s5.draw(batch);
                    // sge_transform(canon_sfc,canon_sfc2, (float)(-turretAngle), 0.75F, 0.75F, 0, 4, 16, 16, 0);
                    // d.x=8;
                    // d.y=0;
                    // SDL_BlitSurface(canon_sfc2,0,tank_sfc3,&d);
                }

                if (detector != null) {
                    Polygon p5 = tilePolygons[t5idx];
                    p5.setOrigin(0, 4);
                    p5.setScale(0.75f, 0.75f);
                    p5.rotate(turretAngle_);
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

        // SDL_BlitSurface(tank_sfc2,0,tank_sfc3,0);
        // int d.x=(x-map_x)-24;
        // int d.y=(y-map_y)-16;
        // SDL_BlitSurface(tank_sfc3,0,screen,&d);
    }

    public EnemyDestroyedTank toDestroyedTank() {
        EnemyDestroyedTank enemy = new EnemyDestroyedTank(map);
        copyTo(enemy);
        enemy.state2 = state2; // Do we really need to copy it?
        return enemy;
    }

}
