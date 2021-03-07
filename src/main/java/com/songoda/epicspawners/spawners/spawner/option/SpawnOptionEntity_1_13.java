package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.hooks.PluginHook;
import com.songoda.core.hooks.stackers.WildStacker;
import com.songoda.core.utils.EntityUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
import com.songoda.epicspawners.utils.PaperUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity_1_13 implements SpawnOption {

    private final EntityType[] types;

    private final ScriptEngine engine;

    private final EpicSpawners plugin = EpicSpawners.getInstance();

    private boolean useUltimateStacker;

    private Enum<?> SpawnerEnum;

    private boolean mcmmo;

    private final Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzMobSpawnerData,
            clazzWorldServer,
            clazzWorldAccess,
            clazzGeneratorAccess,
            clazzCraftWorld,
            clazzWorld,
            clazzEntity,
            clazzEntityInsentient,
            clazzBlockPosition,
            clazzIWorldReader,
            clazzICollisionAccess;
    private Method methodGetEntity,
            methodGetChunkCoordinates,
            methodSetString,
            methodSetPosition,
            methodA,
            methodAddEntity,
            methodGetHandle,
            methodChunkRegionLoaderA,
            methodEntityGetBukkitEntity,
            methodCraftEntityTeleport,
            methodEntityInsentientPrepare,
            methodChunkRegionLoaderA2,
            methodGetDamageScaler,
            methodGetCubes,
            methodGetBoundingBox;
    private Field fieldWorldRandom,
            fieldSpawnReason;

    public SpawnOptionEntity_1_13(EntityType... types) {
        this.types = types;
        this.engine = new ScriptEngineManager(null).getEngineByName("JavaScript");

        if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) {
            this.useUltimateStacker = ((Plugin) com.songoda.ultimatestacker.UltimateStacker.getInstance()).getConfig().getBoolean("Entities.Enabled");
        }

        init();
    }

    public SpawnOptionEntity_1_13(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[0]));
    }

    private void init() {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);

            Class<?> clazzNBTTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound"),
                    clazzChunkRegionLoader = Class.forName("net.minecraft.server." + ver + ".ChunkRegionLoader"),
                    clazzCraftEntity = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftEntity"),
                    clazzGroupDataEntity = Class.forName("net.minecraft.server." + ver + ".GroupDataEntity"),
                    clazzDifficultyDamageScaler = Class.forName("net.minecraft.server." + ver + ".DifficultyDamageScaler"),
                    clazzAxisAlignedBB = Class.forName("net.minecraft.server." + ver + ".AxisAlignedBB"),
                    clazzEntityTypes = Class.forName("net.minecraft.server." + ver + ".EntityTypes");

            clazzMobSpawnerData = Class.forName("net.minecraft.server." + ver + ".MobSpawnerData");
            clazzCraftWorld = Class.forName("org.bukkit.craftbukkit." + ver + ".CraftWorld");
            clazzWorld = Class.forName("net.minecraft.server." + ver + ".World");
            clazzEntity = Class.forName("net.minecraft.server." + ver + ".Entity");
            clazzEntityInsentient = Class.forName("net.minecraft.server." + ver + ".EntityInsentient");
            clazzBlockPosition = Class.forName("net.minecraft.server." + ver + ".BlockPosition");
            clazzIWorldReader = Class.forName("net.minecraft.server." + ver + ".IWorldReader");

            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16))
                methodGetChunkCoordinates = clazzEntity.getMethod("getChunkCoordinates");

            try {
                clazzICollisionAccess = Class.forName("net.minecraft.server." + ver + ".ICollisionAccess");
                methodGetCubes = clazzICollisionAccess.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
            } catch (ClassNotFoundException e) {
                clazzIWorldReader = Class.forName("net.minecraft.server." + ver + ".IWorldReader");
                methodGetCubes = clazzIWorldReader.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
            }

            if (ServerVersion.isServerVersionBelow(ServerVersion.V1_16)
                    || ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16) && ver.equals("v1_16_R1"))
                clazzGeneratorAccess = Class.forName("net.minecraft.server." + ver + ".GeneratorAccess");
            else
                clazzWorldAccess = Class.forName("net.minecraft.server." + ver + ".WorldAccess");

            try {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("getEntity");
            } catch (NoSuchMethodException e) {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("b");
            }
            methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodSetPosition = clazzEntity.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            methodGetHandle = clazzCraftWorld.getDeclaredMethod("getHandle");
            try {
                methodChunkRegionLoaderA = clazzChunkRegionLoader.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld, double.class, double.class, double.class, boolean.class);
                methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzDifficultyDamageScaler, clazzGroupDataEntity, clazzNBTTagCompound);
                methodChunkRegionLoaderA2 = clazzChunkRegionLoader.getDeclaredMethod("a", clazzEntity, clazzGeneratorAccess, Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason"));
            } catch (NoSuchMethodException e) {
                methodA = clazzEntityTypes.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld);

                Class<?> clazzEnumMobSpawn = Class.forName("net.minecraft.server." + ver + ".EnumMobSpawn");
                for (Object enumValue : clazzEnumMobSpawn.getEnumConstants()) {
                    Enum<?> mobSpawnEnum = (Enum<?>) enumValue;
                    if (mobSpawnEnum.name().equals("SPAWNER")) {
                        this.SpawnerEnum = mobSpawnEnum;
                        break;
                    }
                }

                clazzWorldServer = Class.forName("net.minecraft.server." + ver + ".WorldServer");

                methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzGeneratorAccess == null ? clazzWorldAccess : clazzGeneratorAccess, clazzDifficultyDamageScaler, clazzEnumMobSpawn, clazzGroupDataEntity, clazzNBTTagCompound);
                methodAddEntity = clazzWorldServer.getDeclaredMethod("addEntity", clazzEntity, Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason"));
            }

            methodEntityGetBukkitEntity = clazzEntity.getDeclaredMethod("getBukkitEntity");
            methodCraftEntityTeleport = clazzCraftEntity.getDeclaredMethod("teleport", Location.class);
            methodGetDamageScaler = clazzWorld.getDeclaredMethod("getDamageScaler", clazzBlockPosition);

            fieldWorldRandom = clazzWorld.getDeclaredField("random");
            fieldWorldRandom.setAccessible(true);

            if (PaperUtils.isPaper()) {
                fieldSpawnReason = clazzEntity.getDeclaredField("spawnReason");
            }
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.mcmmo = Bukkit.getPluginManager().isPluginEnabled("mcMMO");
    }

    @Override
    public void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, .5, .5);
        if (location.getWorld() == null) return;

        Chunk chunk = location.getChunk();
        if(Arrays.stream(chunk.getEntities()).filter(e -> e instanceof LivingEntity && e.getType() != EntityType.PLAYER && e.getType() != EntityType.ARMOR_STAND && e.isValid()).count() >= 10) {
            return;
        }

        // Get the amount of entities to spawn per spawner in the stack.
        int spawnCount = 0;
        for (int i = 0; i < stack.getStackSize(); i++) {
            spawnCount += ThreadLocalRandom.current().nextInt(1, 3);
        }

        // Check to make sure we're not spawning a stack smaller than the minimum stack size.
        boolean useUltimateStacker = this.useUltimateStacker && com.songoda.ultimatestacker.settings
                .Settings.DISABLED_WORLDS.getStringList().stream()
                .noneMatch(worldStr -> location.getWorld().getName().equalsIgnoreCase(worldStr))
                && spawnCount >= com.songoda.ultimatestacker.settings.Settings.MIN_STACK_ENTITIES.getInt();

        int spawnCountUsed = useUltimateStacker ? 1 : spawnCount;

        if (spawnCountUsed > 0) {
            LivingEntity livingEntity = SpawnConditionNearbyEntities.getStackEntity(spawner.getLocation().add(0.5, 0.5, 0.5));

            if(livingEntity != null) {
                EntityStackerManager.add(livingEntity, spawnCountUsed);
                return;
            }

            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            Entity entity = spawnEntity(type, spawner, data);

            if (entity != null) {
                EntityStackerManager.add((LivingEntity) entity, spawnCountUsed - 1);
                spawner.setSpawnCount(spawner.getSpawnCount() + spawnCountUsed);
            }
        }
    }

    private Entity spawnEntity(EntityType type, PlacedSpawner spawner, SpawnerTier tier) {
        try {
            Object objMobSpawnerData = clazzMobSpawnerData.newInstance();
            Object objNTBTagCompound = methodGetEntity.invoke(objMobSpawnerData);

            String name = type.name().toLowerCase().replace("pig_zombie", "zombie_pigman").replace("snowman", "snow_golem").replace("mushroom_cow", "mooshroom");
            methodSetString.invoke(objNTBTagCompound, "id", "minecraft:" + name);

            short spawnRange = 4;
            for (int i = 0; i < 50; i++) {
                Object objNBTTagCompound = methodGetEntity.invoke(objMobSpawnerData);
                Object objCraftWorld = clazzCraftWorld.cast(spawner.getWorld());
                objCraftWorld = methodGetHandle.invoke(objCraftWorld);
                Object objWorld = clazzWorld.cast(objCraftWorld);

                Random random = (Random) fieldWorldRandom.get(objWorld);
                double x = (double) spawner.getX() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;
                double y = spawner.getY() + random.nextInt(3) - 1;
                double z = (double) spawner.getZ() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;

                Object objEntity;
                if (methodChunkRegionLoaderA != null) {
                    objEntity = methodChunkRegionLoaderA.invoke(null, objNBTTagCompound, objWorld, x, y, z, false);
                } else {
                    Optional optional = (Optional) methodA.invoke(null, objNBTTagCompound, objWorld);

                    if (!optional.isPresent()) continue;

                    objEntity = optional.get();

                    methodSetPosition.invoke(objEntity, x, y, z);
                }

                Object objBlockPosition;
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16))
                    objBlockPosition = methodGetChunkCoordinates.invoke(objEntity);
                else
                    objBlockPosition = clazzBlockPosition.getConstructor(clazzEntity).newInstance(objEntity);
                Object objDamageScaler = methodGetDamageScaler.invoke(objWorld, objBlockPosition);

                Object objEntityInsentient = clazzEntityInsentient.isInstance(objEntity) ? clazzEntityInsentient.cast(objEntity) : null;

                Location spot = new Location(spawner.getWorld(), x, y, z);
                if (!canSpawn(objWorld, objEntityInsentient, tier, spot))
                    continue;

                if (methodChunkRegionLoaderA != null) {
                    methodEntityInsentientPrepare.invoke(objEntity, objDamageScaler, null, null);
                } else {
                    methodEntityInsentientPrepare.invoke(objEntity, objWorld, objDamageScaler, SpawnerEnum, null, null);
                }

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    craftEntity.remove();
                    return null;
                }

                if (methodChunkRegionLoaderA != null) {
                    methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);
                } else {
                    methodAddEntity.invoke(clazzWorldServer.cast(objWorld), objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (fieldSpawnReason != null) {
                    fieldSpawnReason.set(objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (tier.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ESData", new FixedMetadataValue(plugin, tier.getSpawnerData().getIdentifyingName()));
                craftEntity.setMetadata("ESTier", new FixedMetadataValue(plugin, tier.getIdentifyingName()));

                if (mcmmo)
                    craftEntity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(plugin, true));

                if (Settings.NO_AI.getBoolean())
                    EntityUtils.setUnaware(objEntity);

                Object objBukkitEntity = methodEntityGetBukkitEntity.invoke(objEntity);
                spot.setYaw(random.nextFloat() * 360.0F);
                methodCraftEntityTeleport.invoke(objBukkitEntity, spot);

                plugin.getSpawnManager().addUnnaturalSpawn(craftEntity.getUniqueId());

                return craftEntity;
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean canSpawn(Object objWorld, Object objEntityInsentient, SpawnerTier data, Location location) {
        try {
            Object objIWR = clazzIWorldReader == null ? clazzICollisionAccess.cast(objWorld) : clazzIWorldReader.cast(objWorld);

            if (!(boolean) methodGetCubes.invoke(objIWR, objEntityInsentient, methodGetBoundingBox.invoke(objEntityInsentient)))
                return false;

            CompatibleMaterial[] spawnBlocks = data.getSpawnBlocks();

            CompatibleMaterial spawnedIn = CompatibleMaterial.getMaterial(location.getBlock());
            CompatibleMaterial spawnedOn = CompatibleMaterial.getMaterial(location.getBlock().getRelative(BlockFace.DOWN));

            if (!spawnedIn.isAir()
                    && !spawnedIn.isWater()
                    && !spawnedIn.name().contains("PRESSURE")
                    && !spawnedIn.name().contains("SLAB")) {
                return false;
            }

            for (CompatibleMaterial material : spawnBlocks) {
                if (material == null) continue;

                if (spawnedOn.equals(material) || material.isAir())
                    return true;
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ENTITY;
    }

    @Override
    public int hashCode() {
        return 31 * (types != null ? Arrays.hashCode(types) : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionEntity_1_13)) return false;

        SpawnOptionEntity_1_13 other = (SpawnOptionEntity_1_13) object;
        return Arrays.equals(types, other.types);
    }
}
