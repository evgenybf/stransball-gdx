package org.stransball;

public interface ICollisionDetector {

    void handlePolygon(int actXScreen, int actYScreen, int tileIndex);

    boolean wasCollision();

    void reset();
}
