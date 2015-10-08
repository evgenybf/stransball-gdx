package org.stransball;

public interface ICollisionDetector {

    void checkCollision(int act_x, int act_y, int piece);

    boolean wasCollision();

    void reset();

}
