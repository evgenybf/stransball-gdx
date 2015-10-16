package org.stransball.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DebugUtils {

    public static ShapeRenderer passDebugRenderer(ShapeRenderer renderer, boolean enabled) {
        return enabled ? renderer : null;
    }

}
