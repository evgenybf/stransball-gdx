package org.stransball.objects;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.ArrayList;

import org.stransball.Assets;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Enemy {

    public enum EnemyType {
        BULLET, CANON, FAST_CANON, DIRECTIONAL_CANON, TANK, DESTROYED_TANK, EXPLOSION, DIRECTIONAL_CANON_2,
    }

    public final EnemyType type;
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

    protected Enemy(EnemyType type) {
        this.type = type;
    }

    public boolean collision(int strength) {
        life -= strength;
        return life <= 0;
    }

    public boolean updateSimpleCanon(int shipXScreenF, int shipYScreenF, ArrayList<Enemy> enemies) {
        throw new UnsupportedOperationException("implemented for canons only");
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

    public void copyTo(Enemy enemy) {
        enemy.state = state;
        enemy.life = life;
        enemy.x = x;
        enemy.y = x;
        enemy.direction = direction;
        enemy.turretAngle = turretAngle;
        enemy.tankAngle = tankAngle;
        enemy.state2 = state2;
        enemy.tankType = tankType;
        enemy.speedX = speedX;
        enemy.speedY = speedY;
        enemy.tileIndex = tileIndex;

    }
}
