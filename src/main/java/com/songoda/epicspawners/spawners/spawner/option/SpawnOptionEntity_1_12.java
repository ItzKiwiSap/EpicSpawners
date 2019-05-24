package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.settings.Setting;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity_1_12 implements SpawnOption {

    private final EntityType[] types;

    private final ScriptEngineManager mgr;

    private final ScriptEngine engine;

    private EpicSpawners plugin = EpicSpawners.getInstance();

    private boolean stackPlugin;

    private Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzEntityTypes, clazzMobSpawnerData, clazzNBTTagCompound, clazzNBTTagList, clazzCraftWorld, clazzWorld, clazzChunkRegionLoader, clazzEntity, clazzCraftEntity, clazzEntityInsentient, clazzGroupDataEntity, clazzDifficultyDamageScaler, clazzBlockPosition, clazzAxisAlignedBB;
    private Method methodAddEntity, methodCreateEntityByName, methodSetPositionRotation, methodB, methodSetString, methodNTBTagListSize, methodGetHandle, methodTBNTagListK, methodEntityInsentientPrepare, methodChunkRegionLoaderA, methodEntityGetBukkitEntity, methodCraftEntityTeleport, methodEntityInsentientCanSpawn, methodChunkRegionLoaderA2, methodGetDamageScaler, methodGetCubes, methodGetBoundingBox;
    private Field fieldWorldRandom;

    public SpawnOptionEntity_1_12(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
        this.stackPlugin = Bukkit.getPluginManager().isPluginEnabled("UltimateStacker") || Bukkit.getPluginManager().isPluginEnabled("StackMob");
        init();
    }

    public SpawnOptionEntity_1_12(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[entities.size()]));
    }

    private void init() {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            clazzWorld = Class.forName("net.minecraft.server." + ver + ".World");
            clazzEntity = Class.forName("net.minecraft.server." + ver + ".Entity");
            clazzNBTTagList = Class.forName("net.minecraft.server." + ver + ".NBTTagList");
            clazzCraftWorld = Class.forName("org.bukkit.craftbukkit." + ver + ".CraftWorld");
            clazzChunkRegionLoader = Class.forName("net.minecraft.server." + ver + ".ChunkRegionLoader");
            clazzCraftEntity = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftEntity");
            clazzEntityInsentient = Class.forName("net.minecraft.server." + ver + ".EntityInsentient");
            clazzBlockPosition = Class.forName("net.minecraft.server." + ver + ".BlockPosition");
            Class<?> clazzSpawnReason = Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason");
            clazzGroupDataEntity = Class.forName("net.minecraft.server." + ver + ".GroupDataEntity");
            clazzDifficultyDamageScaler = Class.forName("net.minecraft.server." + ver + ".DifficultyDamageScaler");
            clazzAxisAlignedBB = Class.forName("net.minecraft.server." + ver + ".AxisAlignedBB");

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
                clazzMobSpawnerData = Class.forName("net.minecraft.server." + ver + ".MobSpawnerData");
                clazzNBTTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound");

                methodB = clazzMobSpawnerData.getDeclaredMethod("b");
                methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

                methodTBNTagListK = clazzNBTTagList.getDeclaredMethod("f", int.class);
                methodGetDamageScaler = clazzWorld.getDeclaredMethod("D", clazzBlockPosition);

                methodChunkRegionLoaderA = clazzChunkRegionLoader.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld, double.class, double.class, double.class, boolean.class);
                methodChunkRegionLoaderA2 = clazzChunkRegionLoader.getDeclaredMethod("a", clazzEntity, clazzWorld, clazzSpawnReason);
            } else {
                clazzEntityTypes = Class.forName("net.minecraft.server." + ver + ".EntityTypes");
                methodCreateEntityByName = clazzEntityTypes.getDeclaredMethod("createEntityByName", String.class, clazzWorld);
                methodSetPositionRotation = clazzEntity.getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
                methodAddEntity = clazzWorld.getDeclaredMethod("addEntity", clazzEntity, clazzSpawnReason);
                methodGetDamageScaler = clazzWorld.getDeclaredMethod("E", clazzBlockPosition);
            }


            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodGetCubes = clazzWorld.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);

            methodGetHandle = clazzCraftWorld.getDeclaredMethod("getHandle");

            methodEntityGetBukkitEntity = clazzEntity.getDeclaredMethod("getBukkitEntity");
            methodCraftEntityTeleport = clazzCraftEntity.getDeclaredMethod("teleport", Location.class);
            methodEntityInsentientCanSpawn = clazzEntityInsentient.getDeclaredMethod("canSpawn");
            methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzDifficultyDamageScaler, clazzGroupDataEntity);

            fieldWorldRandom = clazzWorld.getDeclaredField("random");
            fieldWorldRandom.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, .5, .5);
        if (location.getWorld() == null) return;

        String[] randomLowHigh = plugin.getConfig().getString("Main.Random Amount Added To Each Spawn").split(":");

        int randomAmt = ThreadLocalRandom.current().nextInt(Integer.valueOf(randomLowHigh[0]), Integer.valueOf(randomLowHigh[1]));

        String equation = plugin.getConfig().getString("Main.Equations.Mobs Spawned Per Spawn");
        equation = equation.replace("{RAND}", Integer.toString(randomAmt));
        equation = equation.replace("{MULTI}", Integer.toString(stack.getStackSize()));


        int spawnCount = 0;
        try {
            if (!cache.containsKey(equation)) {
                spawnCount = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                cache.put(equation, spawnCount);
            } else {
                spawnCount = cache.get(equation);
            }
        } catch (ScriptException e) {
            System.out.println("Your spawner equation is broken, fix it.");
        }

        int limit = 0;
        for (SpawnCondition spawnCondition : data.getConditions()) {
            if (spawnCondition instanceof SpawnConditionNearbyEntities)
                limit = ((SpawnConditionNearbyEntities) spawnCondition).getMax();
        }

        int spawnerBoost = spawner.getBoost();

        String[] arr = Setting.SEARCH_RADIUS.getString().split("x");
        Collection<Entity> amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
        amt.removeIf(entity -> !(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ARMOR_STAND);


        if (amt.size() == limit && spawnerBoost == 0) return;
        spawnCount = (stackPlugin ? spawnCount : Math.min(limit - amt.size(), spawnCount)) + spawner.getBoost();

        while (spawnCount-- > 0) {
            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            spawnEntity(type, spawner, data);
            spawner.setSpawnCount(spawner.getSpawnCount() + 1);
            // TODO: Talk to the author of StackMob to get his ass in gear. lolk (I dropped support, try and add it in later)
        }
    }

    private void spawnEntity(EntityType type, Spawner spawner, SpawnerData data) {
        try {
            Object objMobSpawnerData = null;
            Object objNBTTagCompound;


            Methods.Tuple<String, String> typeTranslation = TypeTranslations.fromType(type);

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
                objMobSpawnerData = clazzMobSpawnerData.newInstance();
                objNBTTagCompound = methodB.invoke(objMobSpawnerData);

                methodSetString.invoke(objNBTTagCompound, "id", plugin.isServerVersionAtLeast(ServerVersion.V1_11) ? "minecraft:" + typeTranslation.getKey().replace(" ", "_") : typeTranslation.getValue());
            }

            int spawnRange = 4;
            for (int i = 0; i < 25; i++) {

                Object objCraftWorld = clazzCraftWorld.cast(spawner.getWorld());
                objCraftWorld = methodGetHandle.invoke(objCraftWorld);
                Object objWorld = clazzWorld.cast(objCraftWorld);


                Random random = (Random) fieldWorldRandom.get(objWorld);
                double x = (double) spawner.getX() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;
                double y = (double) (spawner.getY() + random.nextInt(3) - 1);
                double z = (double) spawner.getZ() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;

                Object objEntity;
                if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
                    objNBTTagCompound = methodB.invoke(objMobSpawnerData);
                    objEntity = methodChunkRegionLoaderA.invoke(null, objNBTTagCompound, objWorld, x, y, z, false);
                } else {
                    objEntity = methodCreateEntityByName.invoke(null, typeTranslation.getValue(), objWorld);
                    methodSetPositionRotation.invoke(
                            objEntity, x, y, z, 360.0F, 0.0F);
                }
                Object objEntityInsentient = null;
                if (clazzEntityInsentient.isInstance(objEntity))
                    objEntityInsentient = clazzEntityInsentient.cast(objEntity);

                Location spot = new Location(spawner.getWorld(), x, y, z);
                if (!canSpawn(objEntityInsentient, data, spot))
                    continue;

                /*
                if (!(boolean)methodEntityInsentientCanSpawn.invoke(objEntityInsentient)) {
                    continue;
                } */

                Object objBlockPosition = clazzBlockPosition.getConstructor(clazzEntity).newInstance(objEntity);
                Object objDamageScaler = methodGetDamageScaler.invoke(objWorld, objBlockPosition);

                methodEntityInsentientPrepare.invoke(objEntity, objDamageScaler, null);

                //((EntityInsentient)entity).prepare(world.D(new BlockPosition(entity)), (GroupDataEntity)null);


                ParticleType particleType = data.getEntitySpawnParticle();

                if (particleType != ParticleType.NONE && plugin.isServerVersionAtLeast(ServerVersion.V1_12)) {
                    float xx = (float) (0 + (Math.random() * 1));
                    float yy = (float) (0 + (Math.random() * 2));
                    float zz = (float) (0 + (Math.random() * 1));
                    spot.getWorld().spawnParticle(Particle.valueOf(particleType.getEffect()), spot, 5, xx, yy, zz, 0);
                }

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
                    methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);
                } else {
                    methodAddEntity.invoke(objWorld, objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (data.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ES", new FixedMetadataValue(plugin, data.getIdentifyingName()));

                if (Setting.NO_AI.getBoolean())
                    ((LivingEntity) craftEntity).setAI(false);

                Object objBukkitEntity = methodEntityGetBukkitEntity.invoke(objEntity);
                spot.setYaw(random.nextFloat() * 360.0F);
                methodCraftEntityTeleport.invoke(objBukkitEntity, spot);

                plugin.getSpawnManager().addUnnaturalSpawn(craftEntity.getUniqueId());
                return;
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean canSpawn(Object objEntityInsentient, SpawnerData data, Location location) {
        try {

            if (!(boolean) methodEntityInsentientCanSpawn.invoke(objEntityInsentient))
                return false;


            List<Material> spawnBlocks = Arrays.asList(data.getSpawnBlocks());

            if (!Methods.isAir(location.getBlock().getType()) && !isWater(location.getBlock().getType())) {
                return false;
            }

            for (Material material : spawnBlocks) {
                Material down = location.getBlock().getRelative(BlockFace.DOWN).getType();
                String str = location.getBlock().getType().name().toLowerCase();
                if ((location.getBlock().getType() == Material.AIR || str.contains("pressure") || str.contains("slab")) && material == Material.AIR)
                    return true;
                if (material == null) continue;
                if (down.toString().equalsIgnoreCase(material.name())
                        || (material.toString().equals("GRASS") && down == Material.GRASS)
                        || isWater(down)) {
                    return true;
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isWater(Material type) {
        return type == Material.WATER || type == Material.valueOf("STATIONARY_WATER");
    }

    public enum TypeTranslations {
        VINDICATOR("vindication illager", "VindicationIllager"),
        SNOWMAN("snowman", "SnowMan"),
        PIG_ZOMBIE("zombie_pigman", "PigZombie"),
        EVOKER("evocation_illager", "EvocationIllager"),
        IRON_GOLEM("villager_golem", "VillagerGolem"),
        MUSHROOM_COW("mooshroom", "MushroomCow"),
        MAGMA_CUBE("magma_cube", "LavaSlime");

        private String lower;
        private String upper;

        TypeTranslations(String lower, String upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public static Methods.Tuple<String, String> fromType(EntityType type) {
            try {
                TypeTranslations typeTranslation = valueOf(type.name());
                return new Methods.Tuple<>(typeTranslation.lower, typeTranslation.upper);
            } catch (Exception e) {
                String lower = type.name().toLowerCase();
                String upper = StringUtils.capitaliseAllWords(lower.replace("_", " ")).replace(" ", "");
                return new Methods.Tuple<>(lower, upper);
            }
        }
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ENTITY;
    }

    @Override
    public int hashCode() {
        return 31 * (types != null ? types.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionEntity_1_12)) return false;

        SpawnOptionEntity_1_12 other = (SpawnOptionEntity_1_12) object;
        return Arrays.equals(types, other.types);
    }

}