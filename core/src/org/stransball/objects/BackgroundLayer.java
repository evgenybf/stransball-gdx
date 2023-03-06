package org.stransball.objects;

import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;

import org.stransball.Assets;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class BackgroundLayer {

    private final int background_type;
    private final int sx;
    private final int sy;
    private int animtimer;
    private int animflag;

    public BackgroundLayer(int background_type, int sx, int sy) {
        this.background_type = background_type;
        this.sx = sx;
        this.sy = sy;
        animtimer = 0;
        animflag = 0;
    }

    public void update() {
        animtimer++;
        if (animtimer > 24) {
            animflag++;
            if (animflag < 0 || animflag > 7)
                animflag = 0;
            animtimer = 0;
        }
    }

    public void render(SpriteBatch batch, int x, int y, int ww, int wh) {
        if (batch == null)
            return;

        Array<AtlasRegion> tiles = Assets.assets.graphicAssets.tiles;

        int step_x = tiles.get(0).originalWidth;
        int step_y = tiles.get(0).originalHeight;

        // Draw Background:
        for (int j = 0, act_y = -(int) (y * 0.75F); j < sy; j++, act_y += step_y) {
            if (act_y > -step_y && act_y < wh) {
                for (int i = 0, act_x = -(int) (x * 0.75F); i < sx; i++, act_x += step_x) {
                    if (act_x > -step_x && act_x < ww) {
                        switch (background_type) {
                        case 0:
                            if (j == 10)
                                batch.draw(tiles.get(294), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                            if (j > 10)
                                batch.draw(tiles.get(314), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                            break;
                        case 1:
                            if (j == 10)
                                batch.draw(tiles.get(295), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                            if (j > 10)
                                batch.draw(tiles.get(315), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                            break;
                        case 2:
                            if (j == 10)
                                batch.draw(tiles.get(335), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                            if (j > 10) {
                                if (((j >> 1) & 0x03) == 0) {
                                    if (animflag < 2) {
                                        int t[] = { 316, 317, 318, 319, 336, 337, 338, 339, 358, 359, 378, 379 };
                                        int step = (animtimer + animflag * 24) / 4;
                                        if (step > 11)
                                            step = 11;
                                        batch.draw(tiles.get(t[step]), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                    } else {
                                        batch.draw(tiles.get(316), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                    }
                                } else {
                                    batch.draw(tiles.get(275), act_x, INTERNAL_SCREEN_HEIGHT - act_y - step_y);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}
