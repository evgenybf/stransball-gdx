package org.stransball.objects;

import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import org.stransball.Assets;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnemyBullet extends Enemy {

    public EnemyBullet() {
        super(EnemyType.BULLET);
    }

    public boolean updateBullet(int mapWidth, int mapHeight, boolean collision) {
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

    public void drawBullet(SpriteBatch batch, int mapXScreen, int mapYScreen, ICollisionDetector detector) {
        int tileIndex = getBulletTileIndex();

        int bulletXScreen = x / FACTOR - mapXScreen;
        int bulletYScreen = y / FACTOR - mapYScreen;

        if (batch != null) {
            Sprite sprite = new Sprite(Assets.assets.graphicAssets.tiles.get(tileIndex));
            sprite.setCenter(bulletXScreen, INTERNAL_SCREEN_HEIGHT - bulletYScreen);
            sprite.draw(batch);
        }

        if (detector != null) {
            detector.handlePolygon(bulletXScreen - 8, bulletYScreen - 8, tileIndex);
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
        EnemyExplosion enemy = new EnemyExplosion();
        copyTo(enemy);
        return enemy;
    }

}
