package org.stransball;

import static java.lang.String.format;
import static org.stransball.GameAssets.assets;
import static org.stransball.GameConstants.INTERNAL_SCREEN_HEIGHT;
import static org.stransball.GameConstants.INTERNAL_SCREEN_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainScene extends ScreenAdapter {

    private final SuperTransballGame game;
    private final FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private Vector3 tmpPos;
    private Sprite sprite;
    private Animation shipThrottleAnimation;
    private float shipStateTime;
    private TextureRegion shipRegion;
    private Sound shipSound;
    private float time;
    private Transball tr;
    private boolean playThrustSound;
	private ShapeRenderer shapeRenderer;

    public MainScene(SuperTransballGame game) {
        this.game = game;

        viewport = new FitViewport(INTERNAL_SCREEN_WIDTH, INTERNAL_SCREEN_HEIGHT);
        create();
    }

    public void create() {
        time = 0;

        batch = new SpriteBatch();

        font = assets.fontAssets.defaultFont;

        shipRegion = assets.shipAssets.shipRegion;

        sprite = new Sprite(shipRegion);
        sprite.setScale(0.5f, 0.5f);

        shipThrottleAnimation = assets.shipAssets.shipThrustAnimation;

        shipSound = assets.soundAssets.thrust;

        shipStateTime = 0.0f;

        tmpPos = new Vector3();

        tr = new Transball();

        shapeRenderer = new ShapeRenderer();
        
        assets.shipAssets.shipPolygon.setScale(0.5f, 0.5f);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tr.cycle();

        int x = tr.getShipX();
        int y = tr.getShipY();

        boolean bThrust = Gdx.input.isKeyPressed(GameConstants.THRUST_KEY);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin();

        batch.begin();

        shipStateTime += delta;
        if (!bThrust) {
            sprite.setRegion(shipRegion);
            shipSound.stop();
            playThrustSound = false;
        } else {
            sprite.setRegion(shipThrottleAnimation.getKeyFrame(shipStateTime));
            if (!playThrustSound) {
                shipSound.play();
                shipSound.loop();
                playThrustSound = true;
            }
        }

        sprite.setRotation(tr.getShipAngle());
        sprite.setCenterX(x);
        sprite.setCenterY(y);


        sprite.draw(batch);

        assets.shipAssets.shipPolygon.setRotation(tr.getShipAngle());

        Rectangle rectangle = assets.shipAssets.shipPolygon.getBoundingRectangle();
        assets.shipAssets.shipPolygon.setPosition(
        		x - (rectangle.x - assets.shipAssets.shipPolygon.getX() + rectangle.width/2), 
        		y - (rectangle.y - assets.shipAssets.shipPolygon.getY() + rectangle.height/2));
        shapeRenderer.polygon(assets.shipAssets.shipPolygon.getTransformedVertices());
        
        font.draw(batch, format("TRANSBALL! %s %s %s %.4f", Gdx.graphics.getFramesPerSecond(), x, y, delta), 2,
                viewport.getWorldHeight() - 2);

        {
            time += delta;
            int sec = (int) (time % 60);
            int min = (int) (time / 60);
            font.draw(batch, format("%02d:%02d", min, sec), viewport.getWorldWidth(), viewport.getWorldHeight(), 0,
                    Align.right, true);
        }

        
        batch.end();
        shapeRenderer.end();
        
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

}
