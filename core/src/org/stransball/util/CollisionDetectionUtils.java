package org.stransball.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.ShortArray;

public class CollisionDetectionUtils {

    // Split concave polygon into several convex polygons (triangles)
    public static Polygon[] tiangulate(Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();

        EarClippingTriangulator triangulator = new EarClippingTriangulator();

        ShortArray pointsCoords = triangulator.computeTriangles(vertices);

        int step = 1 * 3; // skip some triangles - it should not affect on the result
        Polygon[] triangles = new Polygon[pointsCoords.size / step];

        for (int i = 0; i < pointsCoords.size / step; i++) {
            Polygon newPoly = new Polygon(new float[]{vertices[pointsCoords.get(i * step) * 2],
                    vertices[pointsCoords.get(i * step) * 2 + 1], vertices[pointsCoords.get(i * step + 1) * 2],
                    vertices[pointsCoords.get(i * step + 1) * 2 + 1], vertices[pointsCoords.get(i * step + 2) * 2],
                    vertices[pointsCoords.get(i * step + 2) * 2 + 1],});
            triangles[i] = newPoly;
        }

        return triangles;
    }

    // Check if groups of polygons are overlapped
    public static boolean overlapPolygons(Polygon shipPolygon, Polygon[] poligons) {
        for (Polygon poly0 : poligons) {
            if (Intersector.overlapConvexPolygons(shipPolygon, poly0)) {
                return true;
            }
        }
        return false;
    }

    // Check if groups of polygons are overlapped
    public static boolean overlapPolygons(Polygon[] shipPolygons, Polygon[] poligons) {
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
    public static void drawPolygons(ShapeRenderer renderer, Polygon[] polygons) {
        for (Polygon poly0 : polygons) {
            renderer.polygon(poly0.getTransformedVertices());
        }
    }

}
