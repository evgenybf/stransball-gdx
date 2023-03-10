package org.stransball.util;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

public class Triangulator {
    private final Pool<Polygon> trianglePool;
    private final Array<Polygon> triangles;

    public Triangulator() {
        this.trianglePool = new Pool<Polygon>() {
            @Override
            protected Polygon newObject() {
                return new Polygon(new float[6]);
            }
        };
        this.triangles = new Array<Polygon>();
    }

    // Split concave polygon into several convex polygons (triangles)
    public Iterable<Polygon> tiangulate(Polygon polygon) {
        float[] vertices = polygon.getTransformedVertices();

        EarClippingTriangulator triangulator = new EarClippingTriangulator();

        ShortArray pointsCoords = triangulator.computeTriangles(vertices);

        int step = 1 * 3; // skip some triangles - it should not affect on the result

        trianglePool.freeAll(triangles);
        triangles.setSize(pointsCoords.size / step);

        for (int i = 0; i < pointsCoords.size / step; i++) {
            int k = i * step;
            Polygon triangle = trianglePool.obtain();
            triangle.setVertex(0, vertices[pointsCoords.get(k) * 2], vertices[pointsCoords.get(k) * 2 + 1]);
            triangle.setVertex(1, vertices[pointsCoords.get(k + 1) * 2], vertices[pointsCoords.get(k + 1) * 2 + 1]);
            triangle.setVertex(2, vertices[pointsCoords.get(k + 2) * 2], vertices[pointsCoords.get(k + 2) * 2 + 1]);
            triangles.set(i, triangle);
        }

        return triangles;
    }
}
