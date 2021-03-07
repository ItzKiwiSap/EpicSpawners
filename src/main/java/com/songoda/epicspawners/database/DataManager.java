package com.songoda.epicspawners.database;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void updateSpawnerAsync(PlacedSpawner spawner) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "placed_spawners SET spawn_count = ?, placed_by = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());
                statement.setInt(3, spawner.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void updateSpawnerSync(PlacedSpawner spawner) {
        this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "placed_spawners SET spawn_count = ?, placed_by = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());
                statement.setInt(3, spawner.getId());
                statement.executeUpdate();
            }
        });
    }

    public void updateSpawnerStack(SpawnerStack spawnerStack) {
        updateSpawnerStack(spawnerStack, spawnerStack.getCurrentTier().getIdentifyingName());
    }

    public void updateSpawnerStack(SpawnerStack stack, String tierBeforeUpdate) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateSpawnerStack = "UPDATE " + this.getTablePrefix() + "spawner_stacks SET amount = ?, tier = ? WHERE spawner_id = ? AND data_type = ? AND tier = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawnerStack)) {
                statement.setInt(1, stack.getStackSize());
                statement.setString(2, stack.getCurrentTier().getIdentifyingName());
                statement.setInt(3, stack.getSpawner().getId());
                statement.setString(4, stack.getSpawnerData().getIdentifyingName());
                statement.setString(5, tierBeforeUpdate);
                statement.executeUpdate();
            }
        }));
    }

    public void deleteSpawnerStack(SpawnerStack stack) {
        deleteSpawnerStack(stack, stack.getCurrentTier());
    }

    public void deleteSpawnerStack(SpawnerStack stack, SpawnerTier tier) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteSpawnerStacks = "DELETE FROM " + this.getTablePrefix() + "spawner_stacks WHERE spawner_id = ? AND data_type = ? AND tier = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawnerStacks)) {
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setString(3, tier.getIdentifyingName());
                statement.executeUpdate();
            }
        }));
    }

    public void createSpawner(PlacedSpawner spawner) {
        this.queueAsync(() -> this.databaseConnector.connect(connection -> {
            String createSpawner = "INSERT INTO " + this.getTablePrefix() + "placed_spawners (spawn_count, placed_by, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawner)) {
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());

                statement.setString(3, spawner.getWorld().getName());
                statement.setInt(4, spawner.getX());
                statement.setInt(5, spawner.getY());
                statement.setInt(6, spawner.getZ());
                statement.executeUpdate();
            }

            int spawnerId = this.lastInsertedId(connection, "placed_spawners");
            spawner.setId(spawnerId);

            String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, tier, amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawnerStack)) {
                for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                    statement.setInt(1, spawnerId);
                    statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                    statement.setString(3, stack.getCurrentTier().getIdentifyingName());
                    statement.setInt(4, stack.getStackSize());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }), "create");
    }

    public void createSpawnerStack(SpawnerStack stack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, tier, amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawnerStack)) {
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setString(3, stack.getCurrentTier().getIdentifyingName());
                statement.setInt(4, stack.getStackSize());
                statement.executeUpdate();
            }
        }));
    }


    public void createEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createEntityKill = "INSERT INTO " + this.getTablePrefix() + "entity_kills (player, entity_type, count) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createEntityKill)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.setInt(3, count);
                statement.executeUpdate();
            }
        }));
    }


    public void updateEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateEntityKill = "UPDATE " + this.getTablePrefix() + "entity_kills SET count = ? WHERE player = ? AND entity_type = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateEntityKill)) {
                statement.setInt(1, count);
                statement.setString(2, player.getUniqueId().toString());
                statement.setString(3, entityType.name());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteEntityKills(OfflinePlayer player, EntityType entityType) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteEntityKills = "DELETE FROM " + this.getTablePrefix() + "entity_kills WHERE player = ? AND entity_type = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteEntityKills)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.executeUpdate();
            }
        }));
    }



    public void getEntityKills(Consumer<Map<UUID, Map<EntityType, Integer>>> callback) {
        Map<UUID, Map<EntityType, Integer>> entityKills = new HashMap<>();
        this.async(() -> this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                String selectEntityKills = "SELECT * FROM " + this.getTablePrefix() + "entity_kills";
                ResultSet result = statement.executeQuery(selectEntityKills);
                while (result.next()) {
                    UUID player = UUID.fromString(result.getString("player"));
                    String typeStr = result.getString("entity_type");
                    EntityType type = typeStr.equals("PIG_ZOMBIE")
                            && ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16)
                            ? EntityType.valueOf("ZOMBIFIED_PIGLIN") : EntityType.valueOf(typeStr);
                    int count = result.getInt("count");
                    if (!entityKills.containsKey(player))
                        entityKills.put(player, new HashMap<>());
                    entityKills.get(player).put(type, count);
                }
            }
            this.sync(() -> callback.accept(entityKills));
        }));
    }

    public void deleteSpawner(PlacedSpawner spawner) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteSpawner = "DELETE FROM " + this.getTablePrefix() + "placed_spawners WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawner)) {
                statement.setInt(1, spawner.getId());
                statement.executeUpdate();
            }

            String deleteSpawnerStack = "DELETE FROM " + this.getTablePrefix() + "spawner_stacks WHERE spawner_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawnerStack)) {
                statement.setInt(1, spawner.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void getSpawners(Consumer<Map<Location, PlacedSpawner>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            EpicSpawners plugin = EpicSpawners.getInstance();

            Map<Integer, PlacedSpawner> spawners = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                String selectSpawners = "SELECT * FROM " + this.getTablePrefix() + "placed_spawners";
                ResultSet result = statement.executeQuery(selectSpawners);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));

                    if (world == null)
                        continue;

                    int id = result.getInt("id");
                    int spawns = result.getInt("spawn_count");

                    String placedByStr = result.getString("placed_by");
                    UUID placedBy = placedByStr == null ? null : UUID.fromString(result.getString("placed_by"));

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

                    PlacedSpawner spawner = new PlacedSpawner(location);
                    spawner.setId(id);
                    spawner.setSpawnCount(spawns);
                    spawner.setPlacedBy(placedBy);
                    spawners.put(id, spawner);
                }
            }

            try (Statement statement = connection.createStatement()) {
                String selectSpawnerStacks = "SELECT * FROM " + this.getTablePrefix() + "spawner_stacks";
                ResultSet result = statement.executeQuery(selectSpawnerStacks);
                while (result.next()) {
                    PlacedSpawner spawner = spawners.get(result.getInt("spawner_id"));
                    if (spawner == null)
                        continue;

                    String type = result.getString("data_type");
                    String tier = result.getString("tier");
                    int amount = result.getInt("amount");
                    SpawnerData data = plugin.getSpawnerManager().getSpawnerData(type);
                    if (data == null) continue;
                    SpawnerTier spawnerTier = data.getTierOrFirst(tier);
                    SpawnerStack stack = new SpawnerStack(spawner, spawnerTier);
                    stack.setStackSize(amount);
                    spawner.addSpawnerStack(stack);
                }
            }

            Map<Location, PlacedSpawner> returnableSpawners = new HashMap<>();
            for (PlacedSpawner spawner : spawners.values())
                returnableSpawners.put(spawner.getLocation(), spawner);

            this.sync(() -> callback.accept(returnableSpawners));
        }));
    }
}
