package org.stransball;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import org.stransball.util.CollisionDetectionUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public final class CollisionDetector implements ICollisionDetector, ICollisionHandler {

    private final ShapeRenderer renderer;
    private final int objectX;
    private final int objectY;
    private final Polygon[] objectPolygons;
    private boolean collision;

    public CollisionDetector(ShapeRenderer renderer, int objectX, int objectY, Polygon[] objectPolygons) {
        this.renderer = renderer;
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectPolygons = objectPolygons;
    }

    @Override
    public void checkCollision(int act_x, int act_y, int tileIndex) {
        Polygon poly = Assets.assets.graphicAssets.tilePolygons[tileIndex];

        // Some objects like bullets can do not have contours in some states like explosion ans so on
        if (poly == null)
            return;

        poly.setPosition(objectX + act_x - 32, INTERNAL_SCREEN_HEIGHT - (objectY + act_y - 32));

        Polygon[] polygons = CollisionDetectionUtils.tiangulate(poly);

        boolean wasCollision = CollisionDetectionUtils.overlapPolygons(objectPolygons, polygons);
        if (wasCollision) {
            handleCollision();
        }

        if (renderer != null) {
            renderer.setColor(wasCollision ? Color.RED : Color.WHITE);
            CollisionDetectionUtils.drawPolygons(renderer, polygons);
        }
    }

    @Override
    public void handleCollision() {
        collision = true;
    }

    @Override
    public void reset() {
        collision = false;
    }

    @Override
    public boolean wasCollision() {
        return collision;
    }
}