package org.stransball;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.ClampToEdge;
import static com.badlogic.gdx.graphics.Texture.TextureWrap.Repeat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;

public class ContourLoader {

    final Array<Region> regions = new Array<Region>();
    
    public static class Region {
        String name;
        int index;
        float[] vertiies;
    }
    
    public ContourLoader(String vtxFileName) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(vtxFileName), 64);
        try {
            Page pageImage = null;
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    break;
                if (line.trim().length() == 0)
                    pageImage = null;
                else if (pageImage == null) {
//                    FileHandle file = imagesDir.child(line);

                    float width = 0, height = 0;
                    if (readTuple(reader) == 2) { // size is only optional for an atlas packed with an old TexturePacker.
                        width = Integer.parseInt(tuple[0]);
                        height = Integer.parseInt(tuple[1]);
                        readTuple(reader);
                    }
                    Format format = Format.valueOf(tuple[0]);

                    readTuple(reader);
                    TextureFilter min = TextureFilter.valueOf(tuple[0]);
                    TextureFilter max = TextureFilter.valueOf(tuple[1]);

                    String direction = readValue(reader);
                    TextureWrap repeatX = ClampToEdge;
                    TextureWrap repeatY = ClampToEdge;
                    if (direction.equals("x"))
                        repeatX = Repeat;
                    else if (direction.equals("y"))
                        repeatY = Repeat;
                    else if (direction.equals("xy")) {
                        repeatX = Repeat;
                        repeatY = Repeat;
                    }

                    //                    pageImage = new Page(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
                    //                    pages.add(pageImage);
                } else {
                    boolean rotate = Boolean.valueOf(readValue(reader));

                    readTuple(reader);
                    int left = Integer.parseInt(tuple[0]);
                    int top = Integer.parseInt(tuple[1]);

                    readTuple(reader);
                    int width = Integer.parseInt(tuple[0]);
                    int height = Integer.parseInt(tuple[1]);

                    Region region = new Region();
//                    region.page = pageImage;
//                    region.left = left;
//                    region.top = top;
//                    region.width = width;
//                    region.height = height;
                    region.name = line;
//                    region.rotate = rotate;

//                    if (readTuple(reader) == 4) { // split is optional
//                        region.splits = new int[] { Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
//                                Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3]) };
//
//                        if (readTuple(reader) == 4) { // pad is optional, but only present with splits
//                            region.pads = new int[] { Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
//                                    Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3]) };
//
//                            readTuple(reader);
//                        }
//                    }
//
//                    region.originalWidth = Integer.parseInt(tuple[0]);
//                    region.originalHeight = Integer.parseInt(tuple[1]);

//                    readTuple(reader);
//                    region.offsetX = Integer.parseInt(tuple[0]);
//                    region.offsetY = Integer.parseInt(tuple[1]);

                    region.index = Integer.parseInt(readValue(reader));

                    //                    if (flip) region.flip = true;

                    //                    regions.add(region);
                }
            }
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error reading pack file: " + vtxFileName, ex);
        } finally {
            StreamUtils.closeQuietly(reader);
        }

                regions.sort(indexComparator);
    }

    public Polygon[] findContours(String name) {
        Array<Polygon> matched = new Array<Polygon>();
        for (int i = 0, n = regions.size; i < n; i++) {
            Region region = regions.get(i);
            if (region.name.equals(name)) {
                Polygon shipPolygon = regionToPolygon(region);
                matched.add(shipPolygon);
            }
        }
        return matched.toArray();
    }

    private Polygon regionToPolygon(Region region) {
        Polygon shipPolygon = new Polygon();
        shipPolygon.setVertices(region.vertiies);
        return shipPolygon;
    }

    public Polygon findRegion (String name) {
        for (int i = 0, n = regions.size; i < n; i++)
            if (regions.get(i).name.equals(name)) return regionToPolygon(regions.get(i));
        return null;
    }

    public Polygon findRegion (String name, int index) {
        for (int i = 0, n = regions.size; i < n; i++) {
            Region region = regions.get(i);
            if (!region.name.equals(name)) continue;
            if (region.index != index) continue;
            return regionToPolygon(region);
        }
        return null;
    }
    
    public Polygon findCountour(String string) {
        Polygon shipPolygon = new Polygon();
        shipPolygon.setVertices(new float[] { 14, 3, 13, 4, 12, 5, 12, 6, 11, 7, 11, 8, 10, 9, 9, 10, 8, 11, 7, 12, 6,
                13, 6, 14, 5, 15, 5, 16, 4, 17, 4, 18, 4, 19, 5, 20, 6, 20, 7, 20, 8, 21, 9, 21, 10, 20, 11, 19, 12, 18,
                13, 17, 14, 18, 15, 18, 16, 18, 17, 18, 18, 17, 19, 18, 20, 19, 21, 20, 22, 21, 23, 21, 24, 20, 25, 20,
                26, 20, 27, 19, 27, 18, 27, 17, 26, 16, 26, 15, 25, 14, 25, 13, 24, 12, 23, 11, 22, 10, 21, 9, 20, 8,
                20, 7, 19, 6, 19, 5, 18, 4, 17, 3, 16, 3, 15, 3 });

        //shipRegion.originalHeight, shipRegion.originalWidth
        if (true) {
            centrializeSpritePolygon(32, 32, shipPolygon.getVertices(), true);
        }

        return shipPolygon;
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

    static final Comparator<Region> indexComparator = new Comparator<Region>() {
        public int compare(Region region1, Region region2) {
            int i1 = region1.index;
            if (i1 == -1)
                i1 = Integer.MAX_VALUE;
            int i2 = region2.index;
            if (i2 == -1)
                i2 = Integer.MAX_VALUE;
            return i1 - i2;
        }
    };

    static final String[] tuple = new String[4];

    static String readValue(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new GdxRuntimeException("Invalid line: " + line);
        return line.substring(colon + 1).trim();
    }

    /** Returns the number of tuple values read (1, 2 or 4). */
    static int readTuple(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        int colon = line.indexOf(':');
        if (colon == -1)
            throw new GdxRuntimeException("Invalid line: " + line);
        int i = 0, lastMatch = colon + 1;
        for (i = 0; i < 3; i++) {
            int comma = line.indexOf(',', lastMatch);
            if (comma == -1)
                break;
            tuple[i] = line.substring(lastMatch, comma).trim();
            lastMatch = comma + 1;
        }
        tuple[i] = line.substring(lastMatch).trim();
        return i + 1;
    }

}
