package org.stransball.objects;

import java.util.List;

import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Enemy {

    public enum EnemyType {
        BULLET, CANON, FAST_CANON, DIRECTIONAL_CANON, TANK, DESTROYED_TANK, EXPLOSION, DIRECTIONAL_CANON_2,
    }

    protected final GameMap map;
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

    public Enemy(EnemyType type, GameMap map) {
        this.type = type;
        this.map = map;
    }

    public boolean takeShot(int strength) {
        life -= strength;
        return life <= 0;
    }

    public abstract void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector);

    public abstract void update(int shipXScreenF, int shipYScreenF, int mapXScreen, int mapYScreen,
            List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer);

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
