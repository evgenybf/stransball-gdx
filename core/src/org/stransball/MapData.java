package org.stransball;

import java.util.ArrayList;

import org.stransball.objects.BackgroundLayer;
import org.stransball.objects.Door;
import org.stransball.objects.Enemy;
import org.stransball.objects.FuelRecharge;
import org.stransball.objects.Smoke;
import org.stransball.objects.SmokeSource;
import org.stransball.objects.StarsLayer;
import org.stransball.objects.Switch;

public class MapData {
    public int[] map;
    public int cols;
    public int rows;

    public ArrayList<Enemy> enemies;
    public ArrayList<Door> doors;
    public ArrayList<Switch> switches;
    public ArrayList<FuelRecharge> fuelRecharges;
    public ArrayList<SmokeSource> smokeSources;
    public ArrayList<Smoke> smokes;

    public int animTimer;
    public int animFlag;

    public StarsLayer stars;
    public BackgroundLayer background;
}
