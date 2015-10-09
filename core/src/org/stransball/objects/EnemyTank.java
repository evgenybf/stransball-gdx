package org.stransball.objects;

public class EnemyTank extends Enemy {

    public EnemyTank() {
        super(EnemyType.TANK);
    }

    public EnemyDestroyedTank toDestroyedTank() {
        EnemyDestroyedTank enemy = new EnemyDestroyedTank();
        copyTo(enemy);
        return enemy;
    }

}
