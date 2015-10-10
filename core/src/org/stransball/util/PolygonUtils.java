package org.stransball.util;

import com.badlogic.gdx.math.Polygon;

public class PolygonUtils {

    public static void flipVertically(Polygon polygon, int height) {
        float[] vertices = polygon.getVertices();
        for (int i = 1; i < vertices.length; i += 2) {
            vertices[i] = height - vertices[i];
        }
        polygon.dirty();
    }

}
