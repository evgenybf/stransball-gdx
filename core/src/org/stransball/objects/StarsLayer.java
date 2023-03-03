package org.stransball.objects;

import static com.badlogic.gdx.math.MathUtils.random;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.Constants.INTERNAL_SCREEN_WIDTH;

import org.stransball.Assets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StarsLayer {

    private static final int STAR_FACTOR = 8;

    private int nstars;
    private int[] star_x;
    private int[] star_y;
    private int[] star_color;

    public StarsLayer(int mapWidth) {
        nstars = mapWidth * STAR_FACTOR;

        star_x = new int[nstars];
        star_y = new int[nstars];
        star_color = new int[nstars];

        for (int i = 0; i < nstars; i++) {
            star_color[i] = 3 * random(255 - 1);
            star_x[i] = random(mapWidth * 16 - 1);
            star_y[i] = 160 - (int) (Math.sqrt(random(25600 - 1)));
        }
    }

    public void render(SpriteBatch batch, int map_x, int map_y) {
        if (batch == null)
            return;

        int sx = INTERNAL_SCREEN_WIDTH;
        int sy = INTERNAL_SCREEN_HEIGHT;

        Sprite sprite = new Sprite(Assets.assets.graphicAssets.whiteSpot);

        for (int i = 0; i < nstars; i++) {
            int x = star_x[i] - map_x / 2;
            int y = star_y[i] - map_y / 2;

            if (x >= 0 && x < sx && y >= 0 && y < sy) {
                sprite.setPosition(x, INTERNAL_SCREEN_HEIGHT - y);
                sprite.draw(batch, star_color[i]);
            }
        }
    }

}
