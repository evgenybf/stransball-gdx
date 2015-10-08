package org.stransball;

public interface ICollisionDetector {

    void handlePolygon(int act_x, int act_y, int tileIndex);

    boolean wasCollision();

    void reset();
}
