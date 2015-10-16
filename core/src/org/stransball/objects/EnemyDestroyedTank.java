package org.stransball.objects;

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

public class EnemyDestroyedTank extends Enemy {

    int state2;

    protected EnemyDestroyedTank(GameMap map) {
        super(EnemyType.DESTROYED_TANK, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer) {
        if (state < 48) {
            state++;
        }
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-32 + mapXScreen) && x < (screenWidth + mapXScreen + 32) && y > (-32 + mapYScreen)
                && y < (screenHeight + mapYScreen + 32)) {
            drawDestroyedTank(batch, mapXScreen, mapYScreen, detector);
        }
    }

    private void drawDestroyedTank(SpriteBatch batch, int mapXScreen, int mapYScreen, ICollisionDetector detector) {
        if (x > (-32 + x) && x < (mapXScreen + x + 32) && y > (-32 + y) && y < (mapYScreen + y + 32))
            draw_destroyedtank(batch, mapXScreen, mapYScreen, detector);
    }

    private void draw_destroyedtank(SpriteBatch batch, int map_x, int map_y, ICollisionDetector detector) {
        int tmp = 0;

        if ((state2 & 0x8) == 0)
            tmp = 2;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;
        Polygon[] tilePolygons = Assets.assets.graphicAssets.tilePolygons;

        int dx = (x - map_x) - 24;
        int dy = (y - map_y) - 16;

        int tankAngle_ = 360 - tankAngle;

        //
        // 1. Tracks

        int t1idx;
        int t2idx;

        if (tankType < 3) {
            t1idx = 282 + 4 * tankType + tmp;
            t2idx = 283 + 4 * tankType + tmp;
        } else {
            t1idx = 461 + ((state / 2) % 4) * 2;
            t2idx = 462 + ((state / 2) % 4) * 2;
        }

        if (batch != null) {
            {
                //origin: draw(0,0,tank_sfc);
                Sprite s1 = new Sprite(tiles.get(t1idx));
                s1.setOrigin(16, 8);
                s1.setPosition(dx + 0 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s1.setRotation(tankAngle_);
                s1.draw(batch);
            }
            {
                //origin: draw(16,0,tank_sfc);
                Sprite s2 = new Sprite(tiles.get(t2idx));
                s2.setOrigin(16 - 16, 8);
                s2.setRotation(tankAngle_);
                s2.setPosition(dx + 16 + 8, INTERNAL_SCREEN_HEIGHT - (dy + 0 + 30));
                s2.draw(batch);
            }
            //origin: sge_transform(tank_sfc,tank_sfc2, (float)(tankAngle), 1.0F, 1.0F, 16, 8, 24, 24, 0);
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
            int t3idx = -1;

            if (state < 48) {
                t3idx = 248 + state / 8; //.draw(16,8,tank_sfc3);
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

        } else {
            int t4idx = -1;

            if (state < 48) {
                t4idx = 248 + state / 8; //.draw(16,6,tank_sfc3);
            }

            if (batch != null) {
                if (t4idx >= 0) {
                    Sprite s4 = new Sprite(tiles.get(t4idx));
                    s4.setPosition(dx + 16, INTERNAL_SCREEN_HEIGHT - (dy + 6 + 14));
                    s4.draw(batch);
                }
            }

            if (detector != null) {
                if (t4idx >= 0) {
                    Polygon p4 = tilePolygons[t4idx];
                    p4.setPosition(dx + 16, (dy + 6 + 14));
                    detector.handlePolygon(p4);
                }
            }
        }
    }

}
