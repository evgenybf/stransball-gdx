package org.stransball.objects;

import static java.lang.String.format;
import static org.stransball.Constants.FACTOR;

import java.util.List;

import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Enemy {

    public enum EnemyType {
        BULLET, CANON, FAST_CANON, DIRECTIONAL_CANON, TANK, DESTROYED_TANK, EXPLOSION, DIRECTIONAL_CANON_2,
    }

    public enum CanonDirection {
        UP, DOWN, RIGHT, LEFT;

        public static CanonDirection fromInt(int directione) {
            for (CanonDirection value : values()) {
                if (value.ordinal() == directione) {
                    return value;
                }
            }
            throw new IllegalArgumentException(format("Invalid direction: %d", directione));
        }
    }

    protected final GameMap map;
    public final EnemyType type;
    public int state;
    public int life;
    public int x; //TODO: for several types of enemies (explosion) it's not in internal coordinates
    public int y;
    public CanonDirection direction;
    protected int turretAngle;
    protected int tankAngle;
    public int tankType;
    protected int speedX;
    protected int speedY;
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

    public abstract void update(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer);

    public void copyTo(Enemy enemy) {
        enemy.state = state;
        enemy.life = life;
        enemy.x = x;
        enemy.y = y;
        enemy.direction = direction;
        enemy.turretAngle = turretAngle;
        enemy.tankAngle = tankAngle;
        enemy.tankType = tankType;
        enemy.speedX = speedX;
        enemy.speedY = speedY;
        enemy.tileIndex = tileIndex;
    }

    @Deprecated
    protected void fixPosition(Enemy e) {
        e.x -= 8 * FACTOR;
        e.y -= 8 * FACTOR;
    }

}
