package org.stransball.desktop;

import org.stransball.Constants;
import org.stransball.TransballMain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Just an experiment";
        config.foregroundFPS = 60;
        config.backgroundFPS = 60;
        config.width = Constants.DEFAULT_SCREEN_WIDTH;
        config.height = Constants.DEFAULT_SCREEN_HEIGHT;
        config.vSyncEnabled = false;

        new LwjglApplication(new TransballMain(), config);
    }
}
