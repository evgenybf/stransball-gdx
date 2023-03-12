package org.stransball;

import static java.lang.String.format;
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

public class InterphaseScreen extends ScreenAdapter {
    private final GameMain game;
    private final int levelIdx;
    private final Level level;
    private final FitViewport viewport;
    private final BitmapFont font;
    private SpriteBatch batch;

    public InterphaseScreen(GameMain game, int levelIdx, Level level) {
        this.game = game;
        this.levelIdx = levelIdx;
        this.level = level;

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

        if (GameKeysStatus.bFire) {
            game.startLevel();
        }

        int centerX = INTERNAL_SCREEN_WIDTH / 2;
        int centerY = INTERNAL_SCREEN_HEIGHT / 2;

        batch.begin();

        font.draw(batch, format("LEVEL %s", levelIdx + 1), centerX, INTERNAL_SCREEN_HEIGHT - (centerY - 24), 0,
                Align.center, false);
        font.draw(batch, level.title, centerX, INTERNAL_SCREEN_HEIGHT - (centerY - 16), 0, Align.center, false);
        if (level.password != null) {
            font.draw(batch, level.password, centerX, INTERNAL_SCREEN_HEIGHT - centerY, 0, Align.center, false);
        }

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
