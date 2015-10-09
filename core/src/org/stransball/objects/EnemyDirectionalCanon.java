package org.stransball.objects;

import java.util.List;

import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemyDirectionalCanon extends Enemy {

    public EnemyDirectionalCanon(GameMap map) {
        super(EnemyType.DIRECTIONAL_CANON, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen, List<Enemy> enemiesToDelete,
            List<Enemy> newEnemies, ShapeRenderer renderer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-16 + mapXScreen) && x < (screenWidth + mapXScreen) && y > (-16 + mapYScreen)
                && y < (screenHeight + mapYScreen)) {
            drawDirectionalCanon(batch, map.map[(x) / 16 + (y / 16) * map.getCols()], mapXScreen, mapYScreen, detector);
        }
    }

    private void drawDirectionalCanon(SpriteBatch batch, int i, int mapXScreen, int mapYScreen,
            ICollisionDetector detector) {
        // TODO Auto-generated method stub
        
    }

}
