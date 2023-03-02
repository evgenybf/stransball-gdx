package org.stransball.desktop;

import org.stransball.Constants;
import org.stransball.GameMain;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;


public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        //System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        config.setTitle("Just an experiment");
        config.setForegroundFPS(60);
        config.setWindowedMode(Constants.DEFAULT_SCREEN_WIDTH, Constants.DEFAULT_SCREEN_HEIGHT);
        config.useVsync(true);

        new Lwjgl3Application(new GameMain(), config);
    }
}
