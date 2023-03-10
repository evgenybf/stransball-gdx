package org.stransball.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

public class CollisionDetectionUtils {

    // Check if groups of polygons are overlapped
    public static boolean overlapPolygons(Polygon shipPolygon, Iterable<Polygon> poligons) {
        for (Polygon poly0 : poligons) {
            if (Intersector.overlapConvexPolygons(shipPolygon, poly0)) {
                return true;
            }
        }
        return false;
    }

    // Check if groups of polygons are overlapped
    public static boolean overlapPolygons(Iterable<Polygon> shipPolygons, Iterable<Polygon> poligons) {
        for (Polygon shipPoly0 : shipPolygons) {
            for (Polygon poly0 : poligons) {
                if (Intersector.overlapConvexPolygons(shipPoly0, poly0)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Draw polygons in array. Use for debug purposes only
    public static void drawPolygons(ShapeRenderer renderer, Iterable<Polygon> polygons) {
        for (Polygon poly0 : polygons) {
            renderer.polygon(poly0.getTransformedVertices());
        }
    }
}
