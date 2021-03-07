package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnConditionNearbyEntities implements SpawnCondition {

    private final int max;

    private static Map<Location, Integer> cachedAmounts = new HashMap<>();

    public SpawnConditionNearbyEntities(int max) {
        this.max = max;
    }

    @Override
    public String getName() {
        return "nearby_entities";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionNearbyEntities")
                .processPlaceholder("max", max).getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {
        return getEntitiesAroundSpawner(spawner.getLocation().add(0.5, 0.5, 0.5), false) < max;
    }

    public static int getEntitiesAroundSpawner(Location location, boolean cached) {
        if (cached) {
            if (cachedAmounts.containsKey(location))
                return cachedAmounts.get(location);
            return 0;
        }
        String[] arr = Settings.SEARCH_RADIUS.getString().split("x");

        int amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]))
                .stream().filter(e -> e instanceof LivingEntity && e.getType() != EntityType.PLAYER && e.getType() != EntityType.ARMOR_STAND && e.isValid())
                .mapToInt(e -> 1).sum();
        cachedAmounts.put(location, amt);
        return amt;
    }

    public static List<LivingEntity> getEntitiesAroundSpawner(Location location) {
        String[] arr = Settings.SEARCH_RADIUS.getString().split("x");

        return location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]))
                .stream().filter(e -> e instanceof LivingEntity && e.getType() != EntityType.PLAYER && e.getType() != EntityType.ARMOR_STAND && e.isValid())
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());
    }

    public static LivingEntity getStackEntity(Location location) {
        List<LivingEntity> entities = getEntitiesAroundSpawner(location);
        return entities.stream().min(Comparator.comparing((e) -> e.getLocation().distance(location))).orElse(null);
    }

    public int getMax() {
        return max;
    }
}