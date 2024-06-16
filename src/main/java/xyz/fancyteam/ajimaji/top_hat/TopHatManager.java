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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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
    private static final String ID = "aji-maji/top_hat";
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

    public static boolean retrieveEntity(MinecraftServer server, UUID topHatId, ServerWorld targetWorld,
                                         Vec3d targetPos, TopHatRetrieveListener listener) {
        ServerWorld topHatDim = getTopHatDim(server);
        if (topHatDim == null) return false;

        return getInstanceUnchecked(topHatDim).retrieveEntity(topHatDim, topHatId);
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

        storeEntity(initialEntity);
        topHatQueues.computeIfAbsent(topHatId, _k -> new ArrayDeque<>()).add(initialEntity.getUuid());

        initialEntity.teleportTo(
            new TeleportTarget(topHatDim, Vec3d.ofBottomCenter(entryPoint), Vec3d.ZERO, initialEntity.getYaw(),
                initialEntity.getPitch(), newEntity -> {}));
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
                topHatDim.getChunkManager()
                    .addTicket(AMChunkTicketTypes.TOP_HAT_PRE_TELEPORT, entityData.pos, 0, entityId);

                PENDING_REMOVALS.add(new Removal(entityId, entityData.pos));

                Entity deserialized = entityData.type.create(target.world());
                if (deserialized == null) return false;
                deserialized.readNbt(entityData.nbt);
                deserialized.setPos
            } else {
                AjiMaji.LOGGER.warn(
                    "Unable to find entity {} in top hat dimension or storage, maybe they've already been removed?",
                    entityId);
                return false;
            }
        } else {
            entityDatas.remove(entityId);
            retrieved.teleportTo(target);
        }

        return false;
    }

    private void storeEntity(Entity entity) {
        if (!(entity instanceof PlayerEntity) && entity.getType().isSaveable()) {
            NbtCompound nbt = new NbtCompound();
            if (entity.saveNbt(nbt)) {
                entityDatas.put(entity.getUuid(), new EntityData(entity.getType(), nbt, entity.getChunkPos()));
            }
        }
    }

    private void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        Iterator<Removal> iter = PENDING_REMOVALS.iterator();
        while (iter.hasNext()) {
            Removal removal = iter.next();
            if (chunk.getPos().equals(removal.pos)) {
                iter.remove();
                Entity entity = world.getEntity(removal.entityId);
                if (entity != null) {
                    ((EntityAccessor) entity).aji_maji_removeFromDimension();
                } else {
                    ServerTaskQueue.submit(2, () -> {
                        Entity retrieved = world.getEntity(removal.entityId);
                        if (retrieved != null) {
                            ((EntityAccessor) retrieved).aji_maji_removeFromDimension();
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
