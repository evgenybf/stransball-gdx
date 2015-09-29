package org.stransball;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.ShortArray;

public class CollisionDetectorUtils {

    public static boolean collide(Polygon[] shipPolygons, Polygon[] poligons) {
        for (Polygon shipPoly0 : shipPolygons) {
            for (Polygon poly0 : poligons) {
                if (Intersector.overlapConvexPolygons(shipPoly0, poly0)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Polygon[] tiangulate(Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();

        EarClippingTriangulator triangulator = new EarClippingTriangulator();

        ShortArray pointsCoords = triangulator.computeTriangles(vertices);
        
        int step = 2 * 3; // skip some triangles - it should not affect on the result
        Polygon[] triangles = new Polygon[pointsCoords.size / step];

        for (int i = 0; i < pointsCoords.size / step; i++) {
            Polygon newPoly = new Polygon(new float[] { 
                    vertices[pointsCoords.get(i * step) * 2],
                    vertices[pointsCoords.get(i * step) * 2 + 1], 
                    vertices[pointsCoords.get(i * step + 1) * 2],
                    vertices[pointsCoords.get(i * step + 1) * 2 + 1], 
                    vertices[pointsCoords.get(i * step + 2) * 2],
                    vertices[pointsCoords.get(i * step + 2) * 2 + 1], });
            triangles[i] = newPoly;
        }

        return triangles;
    }

    // Draw polygons in array. Use for debug purposes only
    public static void renderPolygons(ShapeRenderer renderer, Polygon[] polygons) {
        for (Polygon poly0 : polygons) {
            renderer.polygon(poly0.getTransformedVertices());
        }
    }

}
