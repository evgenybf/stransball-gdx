package org.stransball;

import static java.lang.String.format;
import static org.stransball.Assets.assets;
import static org.stransball.Constants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.Constants.INTERNAL_SCREEN_WIDTH;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen extends ScreenAdapter {

    @SuppressWarnings("unused")
    private GameMain game;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private float time;
    private WorldController worldController;
    private ShapeRenderer renderer;
    private boolean paused;

    public GameScreen(GameMain game) {
        this.game = game;

        viewport = new FitViewport(INTERNAL_SCREEN_WIDTH, INTERNAL_SCREEN_HEIGHT);
        create();
    }

    public void create() {
        time = 0;

        batch = new SpriteBatch();
        batch.enableBlending();

        renderer = new ShapeRenderer();

        font = assets.fontAssets.defaultFont;

        GameMap map = loadMap();
        worldController = new WorldController(map);
    }

    private GameMap loadMap() {
        GameMap map = new GameMap();
        FileHandle fileHandle = Gdx.files.internal("maps/map5.map");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
        map.load(reader);
        return map;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameKeysStatus.scan();

        if (!paused) {
            worldController.update(delta, null);
        }

        {
            batch.begin();
            worldController.render(batch, null);
            renderGui(delta);
            batch.end();
        }

        { // Debug rendering: show contours of the game objects
            renderer.begin();

            worldController.render(null, renderer);
            renderer.end();
        }
    }

    private void renderGui(float delta) {
        font.draw(batch, format("TRANSBALL! %s", Gdx.graphics.getFramesPerSecond()), 2, viewport.getWorldHeight() - 2);

        {
            time += delta;
            int sec = (int) (time % 60);
            int min = (int) (time / 60);
            font.draw(batch, format("%02d:%02d", min, sec), viewport.getWorldWidth(), viewport.getWorldHeight(), 0,
                    Align.right, true);
        }
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        renderer.setAutoShapeType(true);
        renderer.setProjectionMatrix(viewport.getCamera().combined);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

}
