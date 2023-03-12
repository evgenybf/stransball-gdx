package org.stransball;

import static org.stransball.Assets.assets;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.FPSLogger;

public class GameMain extends Game {
    private final FPSLogger fpsLogger;
    private Screen mainScene;
    private LevelPack levelPack;
    private int currentLevel = Constants.START_LEVEL;

    public GameMain() {
        fpsLogger = new FPSLogger();
        levelPack = new LevelPack();
    }

    @Override
    public void create() {
        assets.init();
        levelPack.load();

        mainScene = new InterphaseScreen(this, currentLevel, levelPack.getLevel(currentLevel));
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

    public void startLevel() {
        GameMap map = loadMap();
        if (mainScene != null) {
            mainScene.dispose();
        }
        mainScene = new GameScreen(this, map);
        setScreen(mainScene);
    }

    private GameMap loadMap() {
        Level level = levelPack.getLevel(currentLevel);
        FileHandle fileHandle = Gdx.files.internal(level.fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
        GameMap map = new GameMap();
        map.load(reader);
        return map;
    }

    public void exitLevel() {
    }

    public void winLevel() {
        if (mainScene != null) {
            mainScene.dispose();
        }
        mainScene = new LevelFinishedScreen(this, currentLevel);
        setScreen(mainScene);
    }

    public void failLevel() {
        if (mainScene != null) {
            mainScene.dispose();
        }
        mainScene = new GameOverScreen(this);
        setScreen(mainScene);
    }
}
