package org.stransball;

import com.badlogic.gdx.Input;

public class Constants {

    public static final boolean DEBUG_GOD_MODE = false;

    public static final int DEFAULT_SCREEN_WIDTH = 320 * 3;
    public static final int DEFAULT_SCREEN_HEIGHT = 240 * 3;

    public static final int INTERNAL_SCREEN_WIDTH = 320;
    public static final int INTERNAL_SCREEN_HEIGHT = 240;

    // 55 frames per scond
    public static final float REDRAWING_PERIOD = 0.18f;

    public static int THRUST_KEY = Input.Keys.Q;
    public static int ANTITHRUST_KEY = Input.Keys.A;
    public static int LEFT_KEY = Input.Keys.O;
    public static int RIGHT_KEY = Input.Keys.P;
    public static int FIRE_KEY = Input.Keys.SPACE;
    public static int ATRACTOR_KEY = Input.Keys.ENTER;
    public static int PAUSE_KEY = Input.Keys.Z;

    public static final int FACTOR = 512;
    public static final int MAX_ATRACTOR_P = 64;

    public static final int fuelfactor[] = { 64, 64, 96 };
    public static final int shotfuel[] = { 40, 64, 96 };
}
