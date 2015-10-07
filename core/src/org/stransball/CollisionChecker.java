package org.stransball;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import org.stransball.util.CollisionDetectorUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public final class CollisionChecker implements ICollisionChecker {

    private final ShapeRenderer renderer;
    private final int objectX;
    private final int objectY;
    private final Polygon[] objectPolygons;
    private final ICollisionHandler handler;

    public CollisionChecker(ShapeRenderer renderer, int objectX, int objectY, Polygon[] shipPolygons,
            ICollisionHandler handler) {
        this.renderer = renderer;
        this.objectX = objectX;
        this.objectY = objectY;
        this.objectPolygons = shipPolygons;
        this.handler = handler;
    }

    @Override
    public void checkCollision(int act_x, int act_y, int piece) {
        Polygon poly = Assets.assets.graphicAssets.tilePolygons[piece];
        if (poly != null) {
            poly.setPosition(objectX + act_x - 32, INTERNAL_SCREEN_HEIGHT - (objectY + act_y - 32));

            Polygon[] polygons = CollisionDetectorUtils.tiangulate(poly);

            if (CollisionDetectorUtils.overlapPolygons(objectPolygons, polygons)) {
                if (renderer != null) {
                    renderer.setColor(Color.RED);
                }

                handler.handleCollision();
            } else {
                if (renderer != null) {
                    renderer.setColor(Color.WHITE);
                }
            }

            if (renderer != null) {
                CollisionDetectorUtils.drawPolygons(renderer, polygons);
            }
        }
    }
}