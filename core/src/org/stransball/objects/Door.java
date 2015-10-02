package org.stransball.objects;

public class Door {

    public int x;
    public int y;
    public int action;
    public int state;
    public int event;

    public void activate() {
        if (action == 0) {
            if (state == 0) {
                action = 1;
            } else {
                action = -1;
            }
        } else {
            action = -action;
        }
    }

}
