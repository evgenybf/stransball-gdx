package org.stransball;

import org.stransball.util.ContourLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

public class Assets {

    public static final Assets assets = new Assets();

    private final AssetManager assetManager;

    public FontAssets fontAssets;
    public SoundAssets soundAssets;
    public GraphicAssets graphicAssets;

    // Singleton class
    private Assets() {
        assetManager = new AssetManager();
    }

    public void init() {
        assetManager.load("graphics/tiles.pack", TextureAtlas.class);

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

        graphicAssets = new GraphicAssets(assetManager, atlas);
        fontAssets = new FontAssets(assetManager);
        soundAssets = new SoundAssets(assetManager);
    }

    public void dispose() {
        graphicAssets.dispose();
        assetManager.dispose();
    }

    public static class FontAssets {
        public BitmapFont defaultFont;

        public FontAssets(AssetManager assetManager) {
            defaultFont = assetManager.get("graphics/font3.fnt", BitmapFont.class);
            defaultFont.setColor(Color.WHITE);
            // defaultFont.getData().setScale(5f);
        }
    }

    public static class GraphicAssets {
        public final Array<AtlasRegion> tiles;
        public final Polygon[] tilePolygons;

        public final AtlasRegion shipRegion;
        public final Animation shipThrustAnimation;
        public final Polygon shipPolygon;
        public final Animation shipExplosionAnimation;
        private final Pixmap whiteSpotPixmap;
        public final Texture whiteSpot;

        public GraphicAssets(AssetManager assetManager, TextureAtlas atlas) {
            ContourLoader contourLoader = new ContourLoader(Gdx.files.internal("graphics/tiles.vtx"));

            tiles = atlas.findRegions("tile");
            tilePolygons = contourLoader.findPolygons("tile");

            shipRegion = atlas.findRegion("ship");
            shipPolygon = contourLoader.findPolygon("ship", true);

            shipThrustAnimation = new Animation(0.1f, atlas.findRegions("shipThrust"), PlayMode.LOOP);

            shipExplosionAnimation = new Animation(0.1f, atlas.findRegions("shipExplosion"), PlayMode.LOOP);

            whiteSpotPixmap = new Pixmap(1, 1, Format.RGB888);
            whiteSpotPixmap.setColor(Color.WHITE);
            whiteSpotPixmap.fill();

            whiteSpot = new Texture(whiteSpotPixmap);
        }

        public void dispose() {
            whiteSpot.dispose();
            whiteSpotPixmap.dispose();
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
