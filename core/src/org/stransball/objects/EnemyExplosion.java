package org.stransball.objects;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemyExplosion extends Enemy {

    protected EnemyExplosion(GameMap map) {
        super(EnemyType.EXPLOSION, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen, List<Enemy> enemiesToDelete,
            List<Enemy> newEnemies, ShapeRenderer renderer) {
        // TODO Auto-generated method stub

    }

    public boolean updateExplosion() {
        state++;
        return state < 48;
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-16 + mapXScreen) && x < (screenWidth + mapXScreen) && y > (-16 + mapYScreen)
                && y < (screenHeight + mapYScreen)) {
            drawExplosion(batch, mapXScreen, mapYScreen, detector);
        }
    }

    private void drawExplosion(SpriteBatch batch, int mapXScreen, int mapYScreen, ICollisionDetector detector) {
        int frames[] = { 240, 241, 260, 261, 280, 281 };

        if (state <= 47) {
            batch.draw(Assets.assets.graphicAssets.tiles.get(frames[state / 8]), x - mapXScreen,
                    INTERNAL_SCREEN_HEIGHT - (y - mapYScreen));
        }
    }

}
