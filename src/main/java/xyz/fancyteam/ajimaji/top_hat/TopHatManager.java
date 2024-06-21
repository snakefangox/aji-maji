package xyz.fancyteam.ajimaji.top_hat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.chunk.WorldChunk;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.misc.AMChunkTicketTypes;
import xyz.fancyteam.ajimaji.misc.AMDimensions;
import xyz.fancyteam.ajimaji.mixin.EntityAccessor;
import xyz.fancyteam.ajimaji.util.ServerTaskQueue;

public class TopHatManager extends PersistentState {
    private static final String ID = "aji-maji_top_hat";
    private static final Type<TopHatManager> TYPE = new Type<>(TopHatManager::new, TopHatManager::new, null);
    private static final List<Removal> PENDING_REMOVALS = new ArrayList<>();

    private record Removal(UUID entityId, ChunkPos pos) {}

    private record EntityData(EntityType<?> type, NbtCompound nbt, ChunkPos pos) {}

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (entity, source, amount) -> {
                RegistryEntry<DamageType> damageType = source.getTypeRegistryEntry();
                return damageType.matchesKey(DamageTypes.OUT_OF_WORLD) ||
                    damageType.matchesKey(DamageTypes.GENERIC_KILL) ||
                    entity.getWorld().getRegistryKey() != AMDimensions.TOP_HAT_DIMENSION;
            });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            Entity.RemovalReason reason = entity.getRemovalReason();
            if (world instanceof ServerWorld serverWorld && world.getRegistryKey() == AMDimensions.TOP_HAT_DIMENSION &&
                (reason == null || reason.shouldSave())) {
                getInstanceUnchecked(serverWorld).storeEntity(entity);
            }
        });

        // Wait for the entity's chunk to load, so we can remove the entity that has been moved to the other dimension
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world instanceof ServerWorld serverWorld && world.getRegistryKey() == AMDimensions.TOP_HAT_DIMENSION) {
                getInstanceUnchecked(serverWorld).onChunkLoad(serverWorld, chunk);
            }
        });
    }

    public static void insertEntity(MinecraftServer server, UUID topHatId, Entity initialEntity) {
        ServerWorld topHatDim = getTopHatDim(server);
        if (topHatDim == null) return;

        getInstanceUnchecked(topHatDim).insertEntity(topHatDim, topHatId, initialEntity);
    }

    public static boolean retrieveEntity(MinecraftServer server, UUID topHatId, TeleportTarget target) {
        ServerWorld topHatDim = getTopHatDim(server);
        if (topHatDim == null) return false;

        return getInstanceUnchecked(topHatDim).retrieveEntity(topHatDim, topHatId, target);
    }

    private static @Nullable ServerWorld getTopHatDim(MinecraftServer server) {
        ServerWorld topHatDim = server.getWorld(AMDimensions.TOP_HAT_DIMENSION);
        if (topHatDim == null) {
            AjiMaji.LOGGER.error("No dimension {}", AMDimensions.TOP_HAT_DIMENSION);
        }
        return topHatDim;
    }

    private static TopHatManager getInstanceUnchecked(ServerWorld topHatDim) {
        return topHatDim.getPersistentStateManager().getOrCreate(TYPE, ID);
    }

    private final Map<UUID, Deque<UUID>> topHatQueues = new LinkedHashMap<>();
    private final Map<UUID, EntityData> entityDatas = new LinkedHashMap<>();

    private TopHatManager() {

    }

    private TopHatManager(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound topHats = nbt.getCompound("top_hats");
        for (String key : topHats.getKeys()) {
            UUID topHatId = UUID.fromString(key);
            Deque<UUID> entities = new ArrayDeque<>();
            NbtList entitiesList = topHats.getList(key, NbtElement.INT_ARRAY_TYPE);
            for (NbtElement elem : entitiesList) {
                entities.offer(NbtHelper.toUuid(elem));
            }

            topHatQueues.put(topHatId, entities);
        }

        NbtCompound entityDatasNbt = nbt.getCompound("entity_datas");
        for (String key : entityDatasNbt.getKeys()) {
            NbtCompound entityNbt = entityDatasNbt.getCompound(key);
            ChunkPos pos = new ChunkPos(entityNbt.getInt("x"), entityNbt.getInt("z"));
            EntityType<?> type = registryLookup.getWrapperOrThrow(RegistryKeys.ENTITY_TYPE)
                .getOrThrow(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(entityNbt.getString("type"))))
                .value();
            NbtCompound entityData = entityNbt.getCompound("data");
            entityDatas.put(UUID.fromString(key), new EntityData(type, entityData, pos));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound topHats = new NbtCompound();
        for (var entry : topHatQueues.entrySet()) {
            NbtList entities = new NbtList();
            for (UUID entity : entry.getValue()) {
                entities.add(NbtHelper.fromUuid(entity));
            }
            topHats.put(entry.getKey().toString(), entities);
        }

        nbt.put("top_hats", topHats);

        NbtCompound entityDatasNbt = new NbtCompound();
        for (var entry : entityDatas.entrySet()) {
            EntityData data = entry.getValue();
            NbtCompound entityChunk = new NbtCompound();
            entityChunk.putInt("x", data.pos.x);
            entityChunk.putInt("z", data.pos.z);
            entityChunk.putString("type", EntityType.getId(data.type).toString());
            entityChunk.put("data", data.nbt);
            entityDatasNbt.put(entry.getKey().toString(), entityChunk);
        }

        nbt.put("entity_datas", entityDatasNbt);

        return nbt;
    }

    private void insertEntity(ServerWorld topHatDim, UUID topHatId, Entity initialEntity) {
        Random random = topHatDim.random;
        BlockPos entryPoint = new BlockPos(random.nextInt(2000) - 1000, 256, random.nextInt(2000) - 1000);

        topHatQueues.computeIfAbsent(topHatId, _k -> new ArrayDeque<>()).add(initialEntity.getUuid());

        initialEntity.teleportTo(
            new TeleportTarget(topHatDim, Vec3d.ofBottomCenter(entryPoint), Vec3d.ZERO, initialEntity.getYaw(),
                initialEntity.getPitch(), this::storeEntity));

        markDirty();
    }

    private boolean retrieveEntity(ServerWorld topHatDim, UUID topHatId, TeleportTarget target) {
        Deque<UUID> entityQueue = topHatQueues.get(topHatId);
        if (entityQueue == null) return false;

        while (!entityQueue.isEmpty()) {
            if (retrieveEntity(topHatDim, entityQueue, target)) return true;
        }

        return false;
    }

    private boolean retrieveEntity(ServerWorld topHatDim, Deque<UUID> entityQueue, TeleportTarget target) {
        if (entityQueue.isEmpty()) return false;

        UUID entityId = entityQueue.poll();
        assert entityId != null;

        Entity retrieved = topHatDim.getEntity(entityId);
        if (retrieved == null) {
            EntityData entityData = entityDatas.remove(entityId);
            if (entityData != null) {
                markDirty();

                PENDING_REMOVALS.add(new Removal(entityId, entityData.pos));

                topHatDim.getChunkManager()
                    .addTicket(AMChunkTicketTypes.TOP_HAT_PRE_TELEPORT, entityData.pos, 0, entityId);

                ServerWorld targetWorld = target.world();
                Entity deserialized = entityData.type.create(targetWorld);
                if (deserialized == null) return false;

                deserialized.readNbt(entityData.nbt);
                deserialized.refreshPositionAndAngles(target.pos().x, target.pos().y, target.pos().z, target.yaw(),
                    target.pitch());
                deserialized.setVelocity(target.velocity());
                targetWorld.onDimensionChanged(deserialized);
                targetWorld.resetIdleTimeout();
                target.postDimensionTransition().onTransition(deserialized);
                return true;
            } else {
                AjiMaji.LOGGER.warn(
                    "Unable to find entity {} in top hat dimension or storage, maybe they've already been removed?",
                    entityId);
                return false;
            }
        } else {
            markDirty();
            entityDatas.remove(entityId);
            retrieved.teleportTo(target);
            return true;
        }
    }

    private void storeEntity(Entity entity) {
        if (!(entity instanceof PlayerEntity) && entity.getType().isSaveable()) {
            NbtCompound nbt = new NbtCompound();
            if (entity.saveNbt(nbt)) {
                AjiMaji.LOGGER.debug("Storing entity: {} @ {}", entity, entity.getChunkPos());
                entityDatas.put(entity.getUuid(), new EntityData(entity.getType(), nbt, entity.getChunkPos()));
                markDirty();
            }
        }
    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        Iterator<Removal> iter = PENDING_REMOVALS.iterator();
        while (iter.hasNext()) {
            Removal removal = iter.next();
            if (chunk.getPos().equals(removal.pos)) {
                AjiMaji.LOGGER.debug("Found removal: {}", removal);
                iter.remove();
                Entity entity = world.getEntity(removal.entityId);
                if (entity != null) {
                    AjiMaji.LOGGER.debug("Removing entity {}", entity.getUuid());
                    ((EntityAccessor) entity).aji_maji_removeFromDimension();
                } else {
                    ServerTaskQueue.submit(2, () -> {
                        AjiMaji.LOGGER.debug("Waiting for removal: {}", removal);
                        Entity retrieved = world.getEntity(removal.entityId);
                        if (retrieved != null) {
                            AjiMaji.LOGGER.debug("Removing entity {}", retrieved.getUuid());
                            ((EntityAccessor) retrieved).aji_maji_removeFromDimension();
                            markDirty();
                        } else {
                            AjiMaji.LOGGER.warn(
                                "Unable to find entity {} in top hat dimension, they may have been duplicated",
                                removal.entityId);
                        }
                    });
                }
            }
        }
    }
}
