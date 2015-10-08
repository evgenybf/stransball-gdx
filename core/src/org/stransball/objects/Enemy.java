package org.stransball.objects;

import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.ArrayList;

import org.stransball.Assets;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {

    public enum EnemyType {
        BULLET, CANON, FAST_CANON, DIRECTIONAL_CANON, TANK, DESTOYED_TANK, EXPLOSION, DIRECTIONAL_CANON_2,
    }

    public EnemyType type; //TODO: make it final
    public int state;
    public int life;
    public int x; //FIXME: for several types of enemies (explosion) it's not in internal coordinates
    public int y; //FIXME: for several types of enemies (explosion) it's not in internal coordinates
    public int direction;
    public int turretAngle;
    public int tankAngle;
    public int state2;
    public int tankType;
    public int speedX;
    public int speedY;
    public int tileIndex;

    public Enemy(EnemyType type) {
        this.type = type;
    }

    public boolean collision(int strength) {
        life -= strength;
        if (life <= 0)
            return true;
        else
            return false;
    }

    public boolean updateCanon(int shipXScreenF, int shipYScreenF, ArrayList<Enemy> enemies) {
        if (state == 0) {
            switch (direction) {
            case 0:
                if (shipXScreenF >= (x - 8) && shipXScreenF <= (x + 24) && shipYScreenF < y && shipYScreenF > y - 160) {
                    Enemy e;
                    e = new EnemyBullet();
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
                    e = new EnemyBullet();
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
                    e = new EnemyBullet();
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
                    e = new EnemyBullet();
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

    public boolean updateFastcanon(int shipXScreenF, int shipYScreenF, ArrayList<Enemy> enemies) {
        if (state == 0) {
            switch (direction) {
            case 0:
                if (shipXScreenF >= (x - 8) && shipXScreenF <= (x + 24) && shipYScreenF < y && shipYScreenF > y - 160) {
                    Enemy e;
                    e = new EnemyBullet();
                    e.state = 8;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 0) * FACTOR;
                    e.speedX = 0;
                    e.speedY = -FACTOR * 3;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 1:
                if (shipXScreenF >= (x - 8) && shipXScreenF <= (x + 24) && shipYScreenF > y && shipYScreenF < y + 160) {
                    Enemy e;
                    e = new EnemyBullet();
                    e.state = 8;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 16) * FACTOR;
                    e.speedX = 0;
                    e.speedY = FACTOR * 3;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 2:
                if (shipYScreenF >= (y - 8) && shipYScreenF <= (y + 24) && shipXScreenF > x && shipXScreenF < x + 160) {
                    Enemy e;
                    e = new EnemyBullet();
                    e.state = 8;
                    e.x = (x + 16) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speedX = FACTOR * 3;
                    e.speedY = 0;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 3:
                if (shipYScreenF >= (y - 8) && shipYScreenF <= (y + 24) && shipXScreenF < x && shipXScreenF > x - 160) {
                    Enemy e;
                    e = new EnemyBullet();
                    e.state = 8;
                    e.x = (x + 0) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speedX = -FACTOR * 3;
                    e.speedY = 0;
                    e.life = 1;
                    e.tileIndex = 344;

                    enemies.add(e);
                    state = 64;
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

    public void drawDirectionalCanon(SpriteBatch batch, int i, int x2, int y2, ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }

    public void drawTank(SpriteBatch batch, int x2, int y2, ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }

    public void drawDestroyedTank(SpriteBatch batch, int x2, int y2, ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }

    public void drawExplosion(SpriteBatch batch, int x2, int y2, ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }

    public void drawDirectionalCanon2(SpriteBatch batch, int i, int x2, int y2, ICollisionDetector detector) {
        // TODO Auto-generated method stub

    }

    public void drawExplosion(SpriteBatch batch, int mapXScreen, int mapYScreen) {
        int frames[] = { 240, 241, 260, 261, 280, 281 };

        if (state <= 47) {
            //FIXME: x and y must be in internal coordinates
            batch.draw(Assets.assets.graphicAssets.tiles.get(frames[state / 8]), x - mapXScreen,
                    INTERNAL_SCREEN_HEIGHT - (y - mapYScreen));
        }
    }

    public boolean updateExplosion() {
        state++;
        return state < 48;
    }
}
