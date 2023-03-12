package org.stransball;

import static org.stransball.Assets.assets;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.Constants.INTERNAL_SCREEN_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class LevelFinishedScreen extends ScreenAdapter {
    private static final boolean REPLAYS_IMPLEMENTED = false;
    private final GameMain game;
    private final FitViewport viewport;
    private final BitmapFont font;
    private SpriteBatch batch;
    private final int levelIdx;

    public LevelFinishedScreen(GameMain game, int levelIdx) {
        this.game = game;
        this.levelIdx = levelIdx;

        viewport = new FitViewport(INTERNAL_SCREEN_WIDTH, INTERNAL_SCREEN_HEIGHT);

        batch = new SpriteBatch();
        batch.enableBlending();

        font = assets.fontAssets.defaultFont;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameKeysStatus.scan();

        if (GameKeysStatus.bFire || GameKeysStatus.bExit) {
            game.exitLevel();
        }

        int centerX = INTERNAL_SCREEN_WIDTH / 2;
        int centerY = INTERNAL_SCREEN_HEIGHT / 2;

        batch.begin();

        font.draw(batch, String.format("LEVEL %S COMPLETE", levelIdx + 1), centerX,
                INTERNAL_SCREEN_HEIGHT - (centerY - 24), 0, Align.center, false);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
