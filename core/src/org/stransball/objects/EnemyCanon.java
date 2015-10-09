package org.stransball.objects;

import static org.stransball.Constants.FACTOR;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemyCanon extends Enemy {

    public EnemyCanon(GameMap map) {
        super(EnemyType.CANON, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen, List<Enemy> enemiesToDelete,
            List<Enemy> newEnemies, ShapeRenderer renderer) {
        if (!updateSimpleCanon(shipXScreenF, shipYScreenF, newEnemies)) {
            enemiesToDelete.add(this);
        }
    }

    private boolean updateSimpleCanon(int shipXScreenF, int shipYScreenF, List<Enemy> enemies) {
        if (state == 0) {
            switch (direction) {
            case 0:
                if (shipXScreenF >= (x - 8) && shipXScreenF <= (x + 24) && shipYScreenF < y && shipYScreenF > y - 160) {
                    Enemy e;
                    e = new EnemyBullet(map);
                    e.state = 12;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 0) * FACTOR;
                    e.speedX = 0;
                    e.speedY = -FACTOR;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 1:
                if (shipXScreenF >= (x - 8) && shipXScreenF <= (x + 24) && shipYScreenF > y && shipYScreenF < y + 160) {
                    Enemy e;
                    e = new EnemyBullet(map);
                    e.state = 12;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 16) * FACTOR;
                    e.speedX = 0;
                    e.speedY = FACTOR;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 2:
                if (shipYScreenF >= (y - 8) && shipYScreenF <= (y + 24) && shipXScreenF > x && shipXScreenF < x + 160) {
                    Enemy e;
                    e = new EnemyBullet(map);
                    e.state = 12;
                    e.x = (x + 16) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speedX = FACTOR;
                    e.speedY = 0;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 3:
                if (shipYScreenF >= (y - 8) && shipYScreenF <= (y + 24) && shipXScreenF < x && shipXScreenF > x - 160) {
                    Enemy e;
                    e = new EnemyBullet(map);
                    e.state = 12;
                    e.x = (x + 0) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speedX = -FACTOR;
                    e.speedY = 0;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            }
        } else {
            if (state > 0)
                state--;
            if (state < 0)
                return false;
        }
        return true;
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }
}
