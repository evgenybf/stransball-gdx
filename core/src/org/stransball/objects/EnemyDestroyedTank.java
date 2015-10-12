package org.stransball.objects;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class EnemyDestroyedTank extends Enemy {

    protected EnemyDestroyedTank(GameMap map) {
        super(EnemyType.DESTROYED_TANK, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer) {
        if (state < 48)
            state++;
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
            draw_destroyedtank(batch, x, y, detector);
    }

    void draw_destroyedtank(SpriteBatch batch, int map_x, int map_y, ICollisionDetector detector) {
        int tmp = 0;

        if ((state2 & 0x8) == 0)
            tmp = 2;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        //        SDL_Rect d;

        if (tankType < 3) {
            tiles.get(282 + 4 * tankType + tmp); //.draw(0,0,tank_sfc);
            tiles.get(283 + 4 * tankType + tmp); //.draw(16,0,tank_sfc);
        } else {
            tiles.get(461 + ((state / 2) % 4) * 2); //.draw(0,0,tank_sfc);
            tiles.get(462 + ((state / 2) % 4) * 2); //.draw(16,0,tank_sfc);
        }

        //        sge_transform(tank_sfc,tank_sfc2, (float)(tankAngle), 1.0F, 1.0F, 16, 8, 24, 24, 0);

        /* Turret: */
        if (tankType < 3) {
            if (state < 48)
                tiles.get(248 + state / 8); //.draw(16,8,tank_sfc3);
        } else {
            if (state < 48)
                tiles.get(248 + state / 8); //.draw(16,6,tank_sfc3);
        }

        //        SDL_BlitSurface(tank_sfc2,0,tank_sfc3,0);

        //        d.x=(x-map_x)-24;
        //        d.y=(y-map_y)-16;
        //        SDL_BlitSurface(tank_sfc3,0,screen,&d);

    }

}
