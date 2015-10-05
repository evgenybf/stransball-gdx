package org.stransball;

import com.badlogic.gdx.Gdx;

public class GameKeysStatus {

    public static boolean bLeft;
    public static boolean bRight;
    public static boolean bThrust;
    public static boolean bAntiThrust;
    public static boolean bAtractor;
    private static boolean bFire;
    private static boolean bFirePrev;

    public static void scan() {
        bLeft = Gdx.input.isKeyPressed(Constants.LEFT_KEY);
        bRight = Gdx.input.isKeyPressed(Constants.RIGHT_KEY);
        bThrust = Gdx.input.isKeyPressed(Constants.THRUST_KEY);
        bAntiThrust = Gdx.input.isKeyPressed(Constants.ANTITHRUST_KEY);
        bAtractor = Gdx.input.isKeyPressed(Constants.ATRACTOR_KEY);
        bFirePrev = bFire;
        bFire = Gdx.input.isKeyPressed(Constants.FIRE_KEY);
    }

    public static boolean isFire() {
        return bFire && !bFirePrev;
    }

}
