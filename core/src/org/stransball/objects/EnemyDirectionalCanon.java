package org.stransball.objects;

import static org.stransball.Assets.assets;
import static org.stransball.Constants.FACTOR;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import java.util.List;

import org.stransball.Assets;
import org.stransball.GameMap;
import org.stransball.ICollisionDetector;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;

public class EnemyDirectionalCanon extends Enemy {

    public EnemyDirectionalCanon(GameMap map) {
        super(EnemyType.DIRECTIONAL_CANON, map);
        turretAngle = 0;
    }

    @Override
    public void update(int shipXScreenF, int shipYScreenF, int shipSpeedX, int shipSpeedY, int mapXScreen,
            int mapYScreen, List<Enemy> enemiesToDelete, List<Enemy> newEnemies, ShapeRenderer renderer) {
        if (!updateDirectionalCanon(shipXScreenF, shipYScreenF, newEnemies)) {
            enemiesToDelete.add(this);
        }
    }

    private boolean updateDirectionalCanon(int shipXScreenF, int shipYScreenF, List<Enemy> enemies) {
        // Turret's angle:
        int dx = shipXScreenF - (x + 8);
        int dy = shipYScreenF - (y + 8);
        float radians = (float) (MathUtils.atan2((float) dy, (float) dx));
        int desired_turretAngle;

        if (state >= 0) {
            turretAngle = (int) ((radians * 180) / 3.141592F);
            if (turretAngle < 0)
                turretAngle += 360;
            desired_turretAngle = turretAngle;

            switch (direction) {
            case UP:
                if (turretAngle >= 345 || turretAngle < 90)
                    turretAngle = 345;
                if (turretAngle < 205)
                    turretAngle = 205;
                break;
            case DOWN:
                if (turretAngle < 15 || turretAngle >= 270)
                    turretAngle = 15;
                if (turretAngle >= 175)
                    turretAngle = 175;
                break;
            case RIGHT:
                if (turretAngle >= 75 && turretAngle < 180)
                    turretAngle = 75;
                if (turretAngle >= 180 && turretAngle < 285)
                    turretAngle = 285;
                break;
            case LEFT:
                if (turretAngle < 105)
                    turretAngle = 105;
                if (turretAngle >= 255)
                    turretAngle = 255;
                break;
            }

            state++;

            if (state >= 128) {
                if (turretAngle == desired_turretAngle) {
                    if ((dx * dx) + (dy * dy) < 30000) {
                        /* Fire!: */
                        EnemyBullet e;
                        e = new EnemyBullet(map);
                        e.state = 8;
                        e.speedX = (int) (MathUtils.cos(radians) * FACTOR);
                        e.speedY = (int) (MathUtils.sin(radians) * FACTOR);
                        e.x = (x + 8) * FACTOR + (e.speedX * 8);
                        e.y = (y + 8) * FACTOR + (e.speedY * 8);
                        e.life = 1;
                        e.tileIndex = 344;

                        fixPosition(e);

                        enemies.add(e);
                        Assets.assets.soundAssets.shot.play();
                    }
                }
                state = 0;
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public void draw(SpriteBatch batch, int mapXScreen, int mapYScreen, int screenWidth, int screenHeight,
            ICollisionDetector detector) {
        if (x > (-16 + mapXScreen) && x < (screenWidth + mapXScreen) && y > (-16 + mapYScreen)
                && y < (screenHeight + mapYScreen)) {
            drawDirectionalCanon(batch, map.map[(x) / 16 + (y / 16) * map.getCols()], mapXScreen, mapYScreen, detector);
        }
    }

    private void drawDirectionalCanon(SpriteBatch batch, int tileIndex, int mapXScreen, int mapYScreen,
            ICollisionDetector detector) {
        if (state < 0)
            return;

        int xScreen = x - mapXScreen;
        int yScreen = y - mapYScreen;

        if (batch != null) {
            Sprite sprite = new Sprite(Assets.assets.graphicAssets.tiles.get(254));
            sprite.setScale(0.75f, 0.75f);

            sprite.setOrigin(16, 14);
            sprite.setRotation(180 - turretAngle);

            sprite.setCenter(xScreen, INTERNAL_SCREEN_HEIGHT - (yScreen + 14));
            sprite.draw(batch);
        }

        if (detector != null) {
            Polygon objectPolygon = assets.graphicAssets.tilePolygons[254];

            objectPolygon.setScale(0.75f, 0.75f);
            objectPolygon.setOrigin(16, 14);
            objectPolygon.setRotation(180 - turretAngle);

            // TODO: sync with shipPolygon. It's not clear why we need (-8, + 8) here
            objectPolygon.setPosition(xScreen - 8, yScreen + 14 + 8);

            detector.handlePolygon(objectPolygon);
        }

        if (batch != null) {
            Sprite sprite2 = new Sprite(Assets.assets.graphicAssets.tiles.get(tileIndex));
            sprite2.setPosition(xScreen, INTERNAL_SCREEN_HEIGHT - (yScreen + map.stepY));
            sprite2.draw(batch);
        }

        // Looks like we don't need to check collision with the stand...
        // if (detector != null) {
        // Polygon objectPolygon2 = assets.graphicAssets.tilePolygons[tileIndex];
        // objectPolygon2.setPosition(xScreen, (yScreen + map.stepY));
        // detector.handlePolygon(objectPolygon2);
        // }
    }

}
