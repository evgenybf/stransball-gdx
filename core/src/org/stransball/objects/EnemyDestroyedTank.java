package org.stransball.objects;

import java.util.List;

import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemyDestroyedTank extends Enemy {

    protected EnemyDestroyedTank(GameMap map) {
        super(EnemyType.DESTROYED_TANK, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen, List<Enemy> enemiesToDelete,
            List<Enemy> newEnemies, ShapeRenderer renderer) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
        
    }

}
