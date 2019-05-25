package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collection;

public class SpawnConditionNearbyPlayers implements SpawnCondition {

    private final int distance;
    private final int amount;

    public SpawnConditionNearbyPlayers(int distance, int amount) {
        this.amount = amount;
        this.distance = distance;
    }

    @Override
    public String getName() {
        return "nearby_player";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionNearbyPlayers", amount, distance);
    }

    @Override
    public boolean isMet(Spawner spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);

        int size = Math.toIntExact(location.getWorld().getNearbyEntities(location, distance, distance, distance)
                .stream().filter(e -> e.getType() == EntityType.PLAYER).count());

        return size >= amount;
    }

    public int getDistance() {
        return distance;
    }

    public int getAmount() {
        return amount;
    }
}