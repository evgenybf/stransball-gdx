package org.stransball;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.stransball.objects.BackgroundLayer;
import org.stransball.objects.Door;
import org.stransball.objects.Enemy;
import org.stransball.objects.EnemyCanon;
import org.stransball.objects.EnemyDirectionalCanon;
import org.stransball.objects.EnemyDirectionalCanon2;
import org.stransball.objects.EnemyFastCanon;
import org.stransball.objects.EnemyTank;
import org.stransball.objects.FuelRecharge;
import org.stransball.objects.Smoke;
import org.stransball.objects.SmokeSource;
import org.stransball.objects.StarsLayer;
import org.stransball.objects.Switch;

public class MapLoader {

    private static final int EMPTY_ROWS = 8;

    private GameMap map;

    public MapLoader(GameMap map) {
        this.map = map;
    }

    public MapData load(Reader input) {
        Scanner scanner = new Scanner(input);
        try {
            return load(scanner);
        } finally {
            scanner.close();
        }
    }

    private MapData load(Scanner scanner) {
        MapData mapData = new MapData();

        mapData.enemies = new ArrayList<Enemy>();
        mapData.doors = new ArrayList<Door>();
        mapData.switches = new ArrayList<Switch>();
        mapData.fuelRecharges = new ArrayList<FuelRecharge>();
        mapData.smokeSources = new ArrayList<SmokeSource>();
        mapData.smokes = new ArrayList<Smoke>();

        int cols = scanner.nextInt();
        int rows = scanner.nextInt();
        rows += EMPTY_ROWS;

        int[] mapArray = new int[cols * rows];

        mapData.map = mapArray;
        mapData.cols = cols;
        mapData.rows = rows;

        Arrays.fill(mapArray, -1);

        for (int i = cols * EMPTY_ROWS; i < cols * rows; i++) {
            mapArray[i] = scanner.nextInt();
            mapArray[i]--;
        }

        int switchNumber = 1;

        // Look for enemies, doors, etc.:
        for (int i = 0; i < cols * rows; i++) {
            // ENEMIES:
            if ((mapArray[i] >= 176 && mapArray[i] < 180) || (mapArray[i] >= 196 && mapArray[i] < 200) || (mapArray[i] >= 216 && mapArray[i] < 220)
                    || (mapArray[i] >= 236 && mapArray[i] < 240)
                    || (mapArray[i] == 154 || mapArray[i] == 155 || mapArray[i] == 174 || mapArray[i] == 175)
                    || (mapArray[i] == 386 || mapArray[i] == 387 || mapArray[i] == 406 || mapArray[i] == 407)) {

                int direction = 0;
                int x = (i % cols) * 16;
                int y = (i / cols) * 16;

                if (mapArray[i] == 176 || mapArray[i] == 216 || mapArray[i] == 236 || mapArray[i] == 196 || mapArray[i] == 154 || mapArray[i] == 386)
                    direction = 0;
                if (mapArray[i] == 177 || mapArray[i] == 217 || mapArray[i] == 237 || mapArray[i] == 197 || mapArray[i] == 155 || mapArray[i] == 387)
                    direction = 1;
                if (mapArray[i] == 178 || mapArray[i] == 218 || mapArray[i] == 238 || mapArray[i] == 198 || mapArray[i] == 174 || mapArray[i] == 406)
                    direction = 2;
                if (mapArray[i] == 179 || mapArray[i] == 219 || mapArray[i] == 239 || mapArray[i] == 199 || mapArray[i] == 175 || mapArray[i] == 407)
                    direction = 3;

                if ((mapArray[i] >= 176 && mapArray[i] < 180) || (mapArray[i] >= 216 && mapArray[i] < 220)
                        || (mapArray[i] >= 236 && mapArray[i] < 240)) {
                    // CANON:
                    Enemy e = new EnemyCanon(map);
                    e.state = 0;
                    e.life = 4;
                    e.x = x;
                    e.y = y;
                    e.direction = Enemy.CanonDirection.fromInt(direction);
                    mapData.enemies.add(e);
                }

                if (mapArray[i] >= 196 && mapArray[i] < 200) {
                    // FAST CANON:
                    Enemy e = new EnemyFastCanon(map);
                    e.state = 0;
                    e.life = 8;
                    e.x = x;
                    e.y = y;
                    e.direction = Enemy.CanonDirection.fromInt(direction);
                    mapData.enemies.add(e);
                }

                if (mapArray[i] == 154 || mapArray[i] == 155 || mapArray[i] == 174 || mapArray[i] == 175) {
                    // DIRECTIONAL CANON:
                    Enemy e = new EnemyDirectionalCanon(map);
                    e.state = i % 128;
                    e.life = 12;
                    e.x = x;
                    e.y = y;
                    e.direction = Enemy.CanonDirection.fromInt(direction);

                    mapData.enemies.add(e);
                }
                if (mapArray[i] == 386 || mapArray[i] == 387 || mapArray[i] == 406 || mapArray[i] == 407) {
                    // DIRECTIONAL CANON 2:
                    Enemy e = new EnemyDirectionalCanon2(map);
                    e.state = i % 128;
                    e.life = 12;
                    e.x = x;
                    e.y = y;
                    e.direction = Enemy.CanonDirection.fromInt(direction);
                    mapData.enemies.add(e);
                }
            }

            // DOORS:
            if (mapArray[i] == 113) {
                Door d = new Door();

                d.col = i % cols;
                d.row = i / cols;
                d.action = 0;

                d.state = scanner.nextInt();
                d.event = scanner.nextInt();
                mapData.doors.add(d);
            }

            // SWITCHES:
            if ((mapArray[i] >= 116 && mapArray[i] < 120) || (mapArray[i] >= 136 && mapArray[i] < 140) || (mapArray[i] >= 156 && mapArray[i] < 160)) {
                Switch s = new Switch();
                s.col = i % cols;
                s.row = i / cols;
                s.number = switchNumber++;
                s.state = 0;
                mapData.switches.add(s);
            }

            // FUEL RECHARGES:
            if (mapArray[i] == 132) {
                FuelRecharge f = new FuelRecharge();
                f.col = i % cols;
                f.row = i / cols;
                mapData.fuelRecharges.add(f);
            }
        }

        // Tanks:
        {
            int ntanks;

            ntanks = scanner.nextInt();
            for (int i = 0; i < ntanks; i++) {
                Enemy e = new EnemyTank(map);

                int col = scanner.nextInt();
                int row = scanner.nextInt();
                int type = scanner.nextInt();

                e.state = 1;
                e.x = col * 16;
                e.y = row * 16;
                e.life = 10;
                e.tankType = type;
                mapData.enemies.add(e);
            }
        }

        int backgroundType = scanner.nextInt();
        mapData.background = new BackgroundLayer(backgroundType, cols, rows);

        mapData.animTimer = 0;
        mapData.animFlag = 0;

        mapData.stars = new StarsLayer(cols);

        return mapData;
    }
}
