package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;

public class SpawnConditionNearbyEntities implements SpawnCondition {

    private final int max;

    public SpawnConditionNearbyEntities(int max) {
        this.max = max;
    }

    @Override
    public String getName() {
        return "nearby_entities";
    }

    @Override
    public String getDescription() {
        return "Must be less than " + max + " around this spawner.";
    }

    @Override
    public boolean isMet(Spawner spawner) {

        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);

        String[] arr = EpicSpawners.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");

        Collection<Entity> amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
        amt.removeIf(e -> !(e instanceof LivingEntity) || e.getType() == EntityType.PLAYER || e.getType() == EntityType.ARMOR_STAND);

        return amt.size() < max;
    }

    public int getMax() {
        return max;
    }
}