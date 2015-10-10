package org.stransball.objects;

import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public class EnemyBullet extends Enemy {

    public EnemyBullet(GameMap map) {
        super(EnemyType.BULLET, map);
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen, List<Enemy> enemiesToDelete,
            List<Enemy> newEnemies, ShapeRenderer renderer) {
        boolean collision = checkCollisionOfEnemyAndMap(mapXScreen, mapYScreen, renderer);
        if (!(updateBullet(map.getCols() * 16, map.getRows() * 16, collision))) {
            enemiesToDelete.add(this);
        }
    }

    private boolean checkCollisionOfEnemyAndMap(int mapXScreen, int mapYScreen, ShapeRenderer renderer) {
        int tileIndex = getBulletTileIndex();

        Polygon objectPolygon = Assets.assets.graphicAssets.tilePolygons[tileIndex];
        if (objectPolygon == null) {
            return false;
        }

        int objectXScreenF = x / FACTOR;
        int objectYScreenF = y / FACTOR;

        int objectXScreen = x / FACTOR - mapXScreen;// - 8;
        int objectYScreen = y / FACTOR - mapYScreen;// - 8;

        objectPolygon.setPosition(objectXScreen, INTERNAL_SCREEN_HEIGHT - (objectYScreen + map.stepY));

        return map.checkCollision(objectXScreenF, objectYScreenF, mapXScreen, mapYScreen, objectPolygon, this,
                renderer);
    }

    private boolean updateBullet(int mapWidth, int mapHeight, boolean collision) {
        if (state != 0)
            state--;

        if (x < 0 || x > mapWidth * FACTOR || y < 0 || y > mapHeight * FACTOR)
            return false;

        if (state == 0 && collision)
            state = -1;
        if (state <= -40)
            return false;

        if (state >= 0) {
            x += speedX;
            y += speedY;
        }

        return true;
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-16 + mapXScreen) * FACTOR && x < (screenWidth + mapXScreen) * FACTOR
                && y > (-16 + mapYScreen) * FACTOR && y < (screenHeight + mapYScreen) * FACTOR) {
            drawBullet(batch, mapXScreen, mapYScreen, detector);
        }
    }

    private void drawBullet(SpriteBatch batch, int mapXScreen, int mapYScreen, ICollisionDetector detector) {
        int tileIndex = getBulletTileIndex();

        int bulletXScreen = x / FACTOR - mapXScreen;// - 8;
        int bulletYScreen = y / FACTOR - mapYScreen;// - 8;

        if (batch != null) {
            Sprite sprite = new Sprite(Assets.assets.graphicAssets.tiles.get(tileIndex));
            //sprite.setCenter(bulletXScreen + 8, INTERNAL_SCREEN_HEIGHT - (bulletYScreen + 8));
            sprite.setPosition(bulletXScreen, INTERNAL_SCREEN_HEIGHT - (bulletYScreen + map.stepY));
            sprite.draw(batch);
        }

        if (detector != null) {
            detector.handlePolygon(bulletXScreen, bulletYScreen + map.stepY, tileIndex);
        }
    }

    public int getBulletTileIndex() {
        if (state >= 0) {
            return tileIndex;
        } else {
            return 243 - state / 8;
        }
    }

    public EnemyExplosion toExplosion() {
        EnemyExplosion enemy = new EnemyExplosion(map);
        copyTo(enemy);
        return enemy;
    }

}
