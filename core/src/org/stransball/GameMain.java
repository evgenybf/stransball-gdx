package org.stransball;

import static org.stransball.Assets.assets;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.FPSLogger;

public class GameMain extends Game {

    private final FPSLogger fpsLogger;
    private GameScreen mainScene;

    public GameMain() {
        fpsLogger = new FPSLogger();
    }

    @Override
    public void create() {
        assets.init();

        mainScene = new GameScreen(this);
        setScreen(mainScene);
    }

    @Override
    public void render() {
        fpsLogger.log();
        super.render();

//        if (Gdx.input.isKeyPressed(Keys.TAB)) {
//            boolean fullScreen = !Gdx.graphics.isFullscreen();
//            Gdx.graphics.setFullscreenMode(640, 480, fullScreen);
//        }
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        mainScene.dispose();
        assets.dispose();
    }

}
