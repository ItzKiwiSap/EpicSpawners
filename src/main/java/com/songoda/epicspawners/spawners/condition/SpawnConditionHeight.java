package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;

public class SpawnConditionHeight implements SpawnCondition {

    private final int min, max;

    public SpawnConditionHeight(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getName() {
        return "height";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionHeight")
                .processPlaceholder("min", min)
                .processPlaceholder("max", max)
                .getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {
        double y = spawner.getLocation().getY();
        return y >= min && y <= max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}