package org.stransball.objects;

public class FuelRecharge {

    public int col;
    public int row;

    public boolean isInside(int shipXScreenF, int shipYScreenF) {
        return shipXScreenF >= col * 16 && shipXScreenF < col * 16 + 32 && shipYScreenF >= row * 16
                && shipYScreenF < row * 16 + 32;
    }

}
