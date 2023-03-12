package org.stransball;

public class LevelPack {
    private Level[] levels;

    public void load() {
        levels = Constants.LEVELS;
    }

    public Level[] getLevels() {
        return levels;
    }

    public Level getLevel(int levelIdx) {
        return levels[levelIdx];
    }
}
