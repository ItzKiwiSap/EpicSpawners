package com.songoda.epicspawners.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SpawnerTierBuilder {

    private final SpawnerTier spawnerTier;

    public SpawnerTierBuilder(SpawnerData data) {
        this.spawnerTier = new SpawnerTier(data);
        spawnerTier.reloadSpawnMethods();
    }

    public SpawnerTierBuilder setDisplayName(String name) {
        this.spawnerTier.setDisplayName(name);
        return this;
    }

    public SpawnerTierBuilder displayItem(CompatibleMaterial material) {
        this.spawnerTier.setDisplayItem(material);
        return this;
    }


    public SpawnerTierBuilder setEntities(List<EntityType> entities) {
        this.spawnerTier.setEntities(entities);
        return this;
    }

    public SpawnerTierBuilder setBlocks(List<CompatibleMaterial> blocks) {
        this.spawnerTier.setBlocks(blocks);
        return this;
    }

    public SpawnerTierBuilder setItems(List<ItemStack> items) {
        this.spawnerTier.setItems(items);
        return this;
    }

    public SpawnerTierBuilder setCommands(List<String> commands) {
        this.spawnerTier.setCommands(commands);
        return this;
    }

    public SpawnerTierBuilder setSpawnBlocks(List<CompatibleMaterial> spawnBlocks) {
        this.spawnerTier.setSpawnBlocks(spawnBlocks);
        return this;
    }

    public SpawnerTierBuilder setSpawnOnFire(boolean spawnOnFire) {
        this.spawnerTier.setSpawnOnFire(spawnOnFire);
        return this;
    }

    public SpawnerTierBuilder setPickupCost(double pickupCost) {
        this.spawnerTier.setPickupCost(pickupCost);
        return this;
    }

    public SpawnerTierBuilder setPickDamage(short pickDamage) {
        this.spawnerTier.setPickDamage(pickDamage);
        return this;
    }

    public SpawnerTierBuilder setCostEconomy(double costEconomy) {
        this.spawnerTier.setCostEconomy(costEconomy);
        return this;
    }

    public SpawnerTierBuilder setCostLevels(int levels) {
        this.spawnerTier.setCostLevels(levels);
        return this;
    }

    public SpawnerTierBuilder setTickRate(String tickRate) {
        this.spawnerTier.setTickRate(tickRate);
        return this;
    }

    public SpawnerTierBuilder setSpawnLimit(int spawnLimit) {
        this.spawnerTier.setSpawnLimit(spawnLimit);
        return this;
    }

    public SpawnerTier build() {
        return spawnerTier;
    }
}