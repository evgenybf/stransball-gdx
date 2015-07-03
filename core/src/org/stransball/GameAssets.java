package org.stransball;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class GameAssets {

    public static final GameAssets assets = new GameAssets();

    private final AssetManager assetManager;

    public FontAssets fontAssets;
    public SoundAssets soundAssets;
    public ShipAssets shipAssets;

    // Singleton class
    private GameAssets() {
        assetManager = new AssetManager();
    }

    public void init() {
        assetManager.load("graphics/tiles.pack", TextureAtlas.class);

        assetManager.load("verdana39.fnt", BitmapFont.class);
//        assetManager.load("font3.fnt", BitmapFont.class);
        assetManager.load("graphics/font3.fnt", BitmapFont.class);

        assetManager.load("graphics/tittle.png", Texture.class);
        assetManager.load("graphics/brain.png", Texture.class);
        assetManager.load("graphics/font3.png", Texture.class);

        assetManager.load("sound/enemyhit.ogg", Sound.class);
        assetManager.load("sound/explosion.ogg", Sound.class);
        assetManager.load("sound/fuel.ogg", Sound.class);
        assetManager.load("sound/shipshot.ogg", Sound.class);
        assetManager.load("sound/shot.ogg", Sound.class);
        assetManager.load("sound/start.ogg", Sound.class);
        assetManager.load("sound/switch.ogg", Sound.class);
        assetManager.load("sound/takeball.ogg", Sound.class);
        assetManager.load("sound/thrust.ogg", Sound.class);

        assetManager.finishLoading();

        TextureAtlas atlas = assetManager.get("graphics/tiles.pack", TextureAtlas.class);

        shipAssets = new ShipAssets(atlas);
        fontAssets = new FontAssets(assetManager);
        soundAssets = new SoundAssets(assetManager);
    }

    public void dispose() {
        assetManager.dispose();
    }
    
    public static class FontAssets {
        public BitmapFont defaultFont;
        private BitmapFont verdanaFont;

        public FontAssets(AssetManager assetManager) {
            verdanaFont = assetManager.get("verdana39.fnt", BitmapFont.class);
            verdanaFont.setColor(Color.WHITE);
            verdanaFont.getData().setScale(0.5f);
            defaultFont = assetManager.get("graphics/font3.fnt", BitmapFont.class);
            defaultFont.setColor(Color.WHITE);
//            defaultFont.getData().setScale(5f);
        }
    }

    public static class ShipAssets {
        public final AtlasRegion shipRegion;
        public final Animation shipThrustAnimation;

        public ShipAssets(TextureAtlas atlas) {
            shipRegion = atlas.findRegion("ship");
            shipThrustAnimation = new Animation(0.1f, atlas.findRegions("shipThrust"), PlayMode.LOOP);
        }
    }

    public static class SoundAssets {
        public Sound enemyHit;
        public Sound explosion;
        public Sound fuel;
        public Sound shipshot;
        public Sound shot;
        public Sound start;
        public Sound switchship;
        public Sound takeball;
        public Sound thrust;

        public SoundAssets(AssetManager assetManager) {
            enemyHit = assetManager.get("sound/enemyhit.ogg", Sound.class);
            explosion = assetManager.get("sound/explosion.ogg", Sound.class);
            fuel = assetManager.get("sound/fuel.ogg", Sound.class);
            shipshot = assetManager.get("sound/shipshot.ogg", Sound.class);
            shot = assetManager.get("sound/shot.ogg", Sound.class);
            start = assetManager.get("sound/start.ogg", Sound.class);
            switchship = assetManager.get("sound/switch.ogg", Sound.class);
            takeball = assetManager.get("sound/takeball.ogg", Sound.class);
            thrust = assetManager.get("sound/thrust.ogg", Sound.class);
        }
    }

}
