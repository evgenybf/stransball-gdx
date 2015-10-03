package org.stransball.objects;

import static org.stransball.Constants.FACTOR;

import java.util.ArrayList;

import org.stransball.Assets;
import org.stransball.Constants;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

public class Enemy {

    public enum EnemyType {
        BULLET, CANON, FAST_CANON, DIRECTIONAL_CANON, TANK, DESTOYED_TANK, EXPLOSION, DIRECTIONAL_CANON_2,
    }

    public EnemyType type;
    public int state;
    public int life;
    public int x;
    public int y;
    public int direction;
    public int turret_angle;
    public int tank_angle;
    public int state2;
    public int tank_type;
    public int speed_x;
    public int speed_y;
    public int tile;

    public boolean collision(int strength) {
        life -= strength;
        if (life <= 0)
            return true;
        else
            return false;
    }

    public boolean cycle_canon(int ship_x, int ship_y, ArrayList<Enemy> enemies) {
        if (state == 0) {
            switch (direction) {
            case 0:
                if (ship_x >= (x - 8) && ship_x <= (x + 24) && ship_y < y && ship_y > y - 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 12;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 0) * FACTOR;
                    e.speed_x = 0;
                    e.speed_y = -FACTOR;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 1:
                if (ship_x >= (x - 8) && ship_x <= (x + 24) && ship_y > y && ship_y < y + 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 12;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 16) * FACTOR;
                    e.speed_x = 0;
                    e.speed_y = FACTOR;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 2:
                if (ship_y >= (y - 8) && ship_y <= (y + 24) && ship_x > x && ship_x < x + 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 12;
                    e.x = (x + 16) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speed_x = FACTOR;
                    e.speed_y = 0;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 128;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 3:
                if (ship_y >= (y - 8) && ship_y <= (y + 24) && ship_x < x && ship_x > x - 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 12;
                    e.x = (x + 0) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speed_x = -FACTOR;
                    e.speed_y = 0;
                    e.life = 1;
                    e.tile = 344;

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

    public boolean cycle_fastcanon(int ship_x, int ship_y, ArrayList<Enemy> enemies) {
        if (state == 0) {
            switch (direction) {
            case 0:
                if (ship_x >= (x - 8) && ship_x <= (x + 24) && ship_y < y && ship_y > y - 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 8;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 0) * FACTOR;
                    e.speed_x = 0;
                    e.speed_y = -FACTOR * 3;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 1:
                if (ship_x >= (x - 8) && ship_x <= (x + 24) && ship_y > y && ship_y < y + 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 8;
                    e.x = (x + 8) * FACTOR;
                    e.y = (y + 16) * FACTOR;
                    e.speed_x = 0;
                    e.speed_y = FACTOR * 3;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 2:
                if (ship_y >= (y - 8) && ship_y <= (y + 24) && ship_x > x && ship_x < x + 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 8;
                    e.x = (x + 16) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speed_x = FACTOR * 3;
                    e.speed_y = 0;
                    e.life = 1;
                    e.tile = 344;

                    enemies.add(e);
                    state = 64;
                    Assets.assets.soundAssets.shot.play();
                }
                break;
            case 3:
                if (ship_y >= (y - 8) && ship_y <= (y + 24) && ship_x < x && ship_x > x - 160) {
                    Enemy e;
                    e = new Enemy();
                    e.type = EnemyType.BULLET;
                    e.state = 8;
                    e.x = (x + 0) * FACTOR;
                    e.y = (y + 7) * FACTOR;
                    e.speed_x = -FACTOR * 3;
                    e.speed_y = 0;
                    e.life = 1;
                    e.tile = 344;

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

    public boolean cycle_bullet(int sx, int sy, boolean collision) {
        if (state != 0)
            state--;

        if (x < 0 || x > sx * FACTOR || y < 0 || y > sy * FACTOR)
            return false;

        if (state == 0 && collision)
            state = -1;
        if (state <= -40)
            return false;

        if (state >= 0) {
            x += speed_x;
            y += speed_y;
        }

        return true;
    }

    public void draw_bullet(SpriteBatch batch, ShapeRenderer renderer, int map_x, int map_y) {
        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;
        AtlasRegion tiler;
        if (state >= 0) {
            tiler = tiles.get(tile);
        } else {
            int i;
            i = (-state) / 8;
            tiler = tiles.get(243 + i);
        } /* if */
        if (batch != null) {
            batch.draw(tiler, (x / FACTOR) - map_x - 8,
                    Constants.INTERNAL_SCREEN_HEIGHT - ((y / FACTOR) - map_y /*???- 8*/) - 8);
        }
    }

}
