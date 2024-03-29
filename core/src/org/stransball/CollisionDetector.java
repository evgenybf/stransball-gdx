package org.stransball;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import org.stransball.util.CollisionDetectionUtils;
import org.stransball.util.Triangulator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;

public final class CollisionDetector implements ICollisionDetector, ICollisionHandler {

    private final Triangulator triangulator;
    private final ShapeRenderer renderer;
    private final Iterable<Polygon> objectPolygons;
    private boolean collision;
    private int regionXScreenF;
    private int regionYScreenF;
    private int mapXScreen;
    private int mapYScreen;

    public CollisionDetector(ShapeRenderer renderer, Iterable<Polygon> objectPolygons, int regionXScreenF,
            int regionYScreenF, int mapXScreen, int mapYScreen) {
        this.renderer = renderer;
        this.objectPolygons = objectPolygons;
        this.regionXScreenF = regionXScreenF;
        this.regionYScreenF = regionYScreenF;
        this.mapXScreen = mapXScreen;
        this.mapYScreen = mapYScreen;
        this.triangulator = new Triangulator();
    }

    @Override
    public void handlePolygon(int actXScreen, int actYScreen, int tileIndex) {
        Polygon poly = Assets.assets.graphicAssets.tilePolygons[tileIndex];
        // Some objects like bullets can do not have contours in some states like
        // explosion ans so on
        if (poly == null)
            return;

        poly.setPosition(actXScreen, actYScreen);

        handlePolygon(poly);
    }

    @Override
    public void handlePolygon(Polygon poly) {
        poly.setPosition(regionXScreenF + poly.getX() - mapXScreen,
                INTERNAL_SCREEN_HEIGHT - (regionYScreenF + poly.getY() - mapYScreen));

        Iterable<Polygon> polygons = triangulator.tiangulate(poly);

        boolean wasCollision = CollisionDetectionUtils.overlapPolygons(objectPolygons, polygons);
        if (wasCollision) {
            handleCollision();
        }

        if (renderer != null) {
            renderer.setColor(wasCollision ? Color.RED : Color.WHITE);

            CollisionDetectionUtils.drawPolygons(renderer, polygons);
        }
    }

    public void handleCollision() {
        collision = true;
    }

    public void reset() {
        collision = false;
    }

    public boolean wasCollision() {
        return collision;
    }
}