package org.stransball;

import com.badlogic.gdx.Input;

public class Constants {

    public static final boolean DEBUG_GOD_MODE = false;

    public static final boolean DEBUG_SHOW_SHIP_COLLISION = false;
    public static final boolean DEBUG_SHOW_BALL_COLLISION = false;
    public static final boolean DEBUG_SHOW_BULLET_COLLISION = false;
    public static final boolean DEBUG_SHOW_ENEMY_COLLISION = false;
    public static final boolean DEBUG_SHOW_TANK_TRACK_COLLISION = false;
    public static final boolean DEBUG_SHOW_COLLISION_DETECTION = DEBUG_SHOW_SHIP_COLLISION || DEBUG_SHOW_BALL_COLLISION
            || DEBUG_SHOW_BULLET_COLLISION || DEBUG_SHOW_ENEMY_COLLISION || DEBUG_SHOW_TANK_TRACK_COLLISION;

    public static final int DEFAULT_SCREEN_WIDTH = 320 * 3;
    public static final int DEFAULT_SCREEN_HEIGHT = 240 * 3;

    public static final int INTERNAL_SCREEN_WIDTH = 320;
    public static final int INTERNAL_SCREEN_HEIGHT = 240;

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

    public static final int START_LEVEL = 0;

    // @formatter:off
    public static final Level[] LEVELS = {
        new Level("maps/map1.map", "You and the ball", 50, "U1RBUlQh"),
        new Level("maps/map2.map", "A small passage", 50),
        new Level("maps/map3.map", "Introducing lasers", 50),
        new Level("maps/map4.map", "The first cannon", 50),
        new Level("maps/canons.map", "More cannons for you", 50),
        new Level("maps/map5.map", "Doors and switches", 50),
        new Level("maps/map6.map", "The first ability challenge", 50),
        new Level("maps/map7.map", "The tanks, your knightmare...", 50),
        new Level("maps/map8.map", "Fuel recharge", 10),
        new Level("maps/map9.map", "Directional cannons", 50),
        new Level("maps/map10.map", "The first big level", 50),
        new Level("maps/map11.map", "Things begin to be difficult", 50),
        new Level("maps/map12.map", "Death in the snow", 50),
        new Level("maps/map13.map", "Many choices...", 50),
        new Level("maps/map14.map", "The head quarters", 50)
    };
    // @formatter:on
}
