package org.stransball.desktop;

import org.stransball.Constants;
import org.stransball.GameMain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("SuperTransball GDX");
        config.setForegroundFPS(200);

        config.setWindowedMode(Constants.DEFAULT_SCREEN_WIDTH, Constants.DEFAULT_SCREEN_HEIGHT);

        config.setResizable(true);
        config.useVsync(true);
        config.setAutoIconify(true);

        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void maximized(boolean isMaximized) {
                if (isMaximized)
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                else
                    Gdx.graphics.setWindowedMode(Constants.DEFAULT_SCREEN_WIDTH, Constants.DEFAULT_SCREEN_HEIGHT);
            }

            @Override
            public void focusLost() {
                Gdx.app.postRunnable(() -> maximized(false));
            }
        });

        new Lwjgl3Application(new GameMain(), config);
    }
}
