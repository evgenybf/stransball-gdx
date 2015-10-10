package org.stransball.util;

import static org.stransball.util.PolygonUtils.centrialize;
import static org.stransball.util.PolygonUtils.flipVertically;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import com.badlogic.gdx.files.FileHandle;
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

    public ContourLoader(FileHandle packFile) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
        try {
            load(reader);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error reading vtx file: " + packFile.name(), ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    public ContourLoader(String vtxFileName) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(vtxFileName), 64);
        try {
            load(reader);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error reading vtx file: " + vtxFileName, ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }
    }

    private void load(BufferedReader reader) throws IOException {
        while (true) {
            String line = reader.readLine();
            if (line == null)
                break;
            Contour contour = new Contour();
            contour.name = line;

            contour.vertiies = readTuple(reader);
            if (contour.vertiies.length == 0) {
                contour.vertiies = null;
            }
            int[] sizeXY = readIntTuple(reader);
            contour.sizeX = sizeXY[0];
            contour.sizeY = sizeXY[1];

            contour.index = Integer.parseInt(readValue(reader));

            contours.add(contour);
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

    private static Polygon contourToPolygon(Contour contour, boolean centralize) {
        if (contour.vertiies == null)
            return null;

        Polygon polygon = new Polygon();
        float[] vertiies = contour.vertiies;
        if (vertiies.length == 4) {
            int d = -1;
            polygon.setVertices(new float[] { vertiies[0], vertiies[1] - d, vertiies[0], vertiies[1], vertiies[2],
                    vertiies[3], vertiies[2], vertiies[3] - d });
        } else {
            polygon.setVertices(contour.vertiies);
        }

        flipVertically(polygon, contour.sizeY);

        if (centralize) {
            centrialize(polygon, contour.sizeX, contour.sizeY);
        }

        return polygon;
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
        String lastPiece = line.substring(lastMatch).trim();
        if (lastPiece.length() > 0) {
            arr.add(Float.parseFloat(lastPiece));
        }
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
