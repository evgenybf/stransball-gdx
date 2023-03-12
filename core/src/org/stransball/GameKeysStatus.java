package org.stransball;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class GameKeysStatus {
    public static boolean bLeft;
    public static boolean bRight;
    public static boolean bThrust;
    public static boolean bAntiThrust;
    public static boolean bAtractor;
    public static boolean bFire;
    public static boolean bExit;

    public static void scan() {
        bLeft = Gdx.input.isKeyPressed(Constants.LEFT_KEY);
        bRight = Gdx.input.isKeyPressed(Constants.RIGHT_KEY);
        bThrust = Gdx.input.isKeyPressed(Constants.THRUST_KEY);
        bAntiThrust = Gdx.input.isKeyPressed(Constants.ANTITHRUST_KEY);
        bAtractor = Gdx.input.isKeyPressed(Constants.ATRACTOR_KEY);
        bFire = Gdx.input.isKeyJustPressed(Constants.FIRE_KEY);
        bExit = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }
}
