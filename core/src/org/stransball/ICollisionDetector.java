package org.stransball;

import com.badlogic.gdx.math.Polygon;

public interface ICollisionDetector {

    void handlePolygon(int actXScreen, int actYScreen, int tileIndex);
    
    void handlePolygon(Polygon poly);

}
