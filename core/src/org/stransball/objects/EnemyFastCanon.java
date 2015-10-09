package org.stransball.objects;

import static org.stransball.Constants.FACTOR;

import java.util.ArrayList;

import org.stransball.Assets;

public class EnemyFastCanon extends Enemy {

    public EnemyFastCanon() {
        super(EnemyType.FAST_CANON);
    }

    @Override
    public boolean updateSimpleCanon(int shipXScreenF, int shipYScreenF, ArrayList<Enemy> enemies) {
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
}
