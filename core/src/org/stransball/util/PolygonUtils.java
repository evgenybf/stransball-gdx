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

    public static void centrialize(Polygon polygon, int height, int width) {
        float dx = width / 2.0f;
        float dy = height / 2.0f;
        float[] vertices = polygon.getVertices();
        for (int i = 0; i < vertices.length; i += 2) {
            vertices[i] = vertices[i] - dx;
        }
        for (int i = 1; i < vertices.length; i += 2) {
            vertices[i] = vertices[i] - dy;
        }
        polygon.dirty();
    }
}
