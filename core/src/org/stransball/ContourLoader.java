package org.stransball;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.StreamUtils;

// Implementation of ContourLoader class is based on TextureAtlas but has even more scruffy code... 
// TODO: Refactoring
public class ContourLoader {

    final Array<Contour> contours = new Array<Contour>();

    private static class Contour {
        String name;
        float[] vertiies;
        public int sizeX;
        public int sizeY;
        int index;
    }

    public ContourLoader(String vtxFileName) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(vtxFileName), 64);
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                Contour contour = new Contour();
                contour.name = line;

                contour.vertiies = readTuple(reader);
                int[] sizeXY = readIntTuple(reader);
                contour.sizeX = sizeXY[0];
                contour.sizeY = sizeXY[1];

                contour.index = Integer.parseInt(readValue(reader));

                contours.add(contour);
            }
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error reading vtx file: " + vtxFileName, ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }

        contours.sort(indexComparator);
    }

    public Polygon[] findPolygons(String name) {
        return findPolygons(name, false);
    }

    public Polygon[] findPolygons(String name, boolean centralize) {
        Array<Polygon> matched = new Array<Polygon>();
        for (int i = 0, n = contours.size; i < n; i++) {
            Contour contour = contours.get(i);
            if (contour.name.equals(name)) {
                matched.add(contourToPolygon(contour, centralize));
            }
        }
        return matched.toArray(Polygon.class);
    }

    public Polygon findPolygon(String name) {
        return findPolygon(name, false);
    }

    public Polygon findPolygon(String name, boolean centralize) {
        for (int i = 0, n = contours.size; i < n; i++)
            if (contours.get(i).name.equals(name))
                return contourToPolygon(contours.get(i), centralize);
        return null;
    }

    public Polygon findPolygon(String name, int index) {
        return findPolygon(name, index, false);
    }

    public Polygon findPolygon(String name, int index, boolean centralize) {
        for (int i = 0, n = contours.size; i < n; i++) {
            Contour contour = contours.get(i);
            if (!contour.name.equals(name))
                continue;
            if (contour.index != index)
                continue;
            return contourToPolygon(contour, centralize);
        }
        return null;
    }

    private static void centrializeSpritePolygon(int height, int width, float[] vertices, boolean vflip) {
        float dx = width / 2.0f;
        float dy = height / 2.0f;
        int vflipSign = vflip ? (-1) : 1;
        for (int i = 0; i < vertices.length; i += 2) {
            vertices[i] = (vertices[i] - dx);
        }
        for (int i = 1; i < vertices.length; i += 2) {
            vertices[i] = vflipSign * (vertices[i] - dy);
        }
    }

    private static Polygon contourToPolygon(Contour contour, boolean centralize) {
        Polygon shipPolygon = new Polygon();
        shipPolygon.setVertices(contour.vertiies);
        if (centralize) {
            centrializeSpritePolygon(contour.sizeX, contour.sizeY, shipPolygon.getVertices(), true);
        }
        return shipPolygon;
    }

    private static final Comparator<Contour> indexComparator = new Comparator<Contour>() {
        public int compare(Contour contour1, Contour contour2) {
            int i1 = contour1.index;
            if (i1 == -1)
                i1 = Integer.MAX_VALUE;
            int i2 = contour2.index;
            if (i2 == -1)
                i2 = Integer.MAX_VALUE;
            return i1 - i2;
        }
    };

    private static String readValue(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new GdxRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    private static float[] readTuple(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new GdxRuntimeException("Invalid line: " + line);

        int lastMatch = colon + 1;

        FloatArray arr = new FloatArray();
        while (true) {
            int comma = line.indexOf(',', lastMatch);
            if (comma == -1)
                break;
            arr.add(Float.parseFloat(line.substring(lastMatch, comma).trim()));
            lastMatch = comma + 1;
        }
        arr.add(Float.parseFloat(line.substring(lastMatch).trim()));
        return arr.toArray();
    }

    private static int[] readIntTuple(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new GdxRuntimeException("Invalid line: " + line);

        int lastMatch = colon + 1;

        IntArray arr = new IntArray();
        while (true) {
            int comma = line.indexOf(',', lastMatch);
            if (comma == -1)
                break;
            arr.add(Integer.parseInt(line.substring(lastMatch, comma).trim()));
            lastMatch = comma + 1;
        }
        arr.add(Integer.parseInt(line.substring(lastMatch).trim()));
        return arr.toArray();
    }

}
