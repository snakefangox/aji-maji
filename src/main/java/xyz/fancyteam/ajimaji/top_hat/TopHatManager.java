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
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
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

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.misc.AMChunkTicketTypes;
import xyz.fancyteam.ajimaji.misc.AMDimensions;
import xyz.fancyteam.ajimaji.util.ServerTaskQueue;

public class TopHatManager extends PersistentState {
    private static final String ID = "aji-maji/top_hat";
    private static final Type<TopHatManager> TYPE = new Type<>(TopHatManager::new, TopHatManager::new, null);
    private static final List<Retrieval> PENDING_RETRIEVALS = new ArrayList<>();

    private record Retrieval(TopHatRetrieveListener listener, UUID entityId, ChunkPos pos) {
    }

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
                getInstanceUnchecked(serverWorld).putEntityChunk(entity);
            }
        });

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world.getRegistryKey() == AMDimensions.TOP_HAT_DIMENSION) {
                Iterator<Retrieval> iter = PENDING_RETRIEVALS.iterator();
                while (iter.hasNext()) {
                    Retrieval retrieval = iter.next();
                    if (chunk.getPos().equals(retrieval.pos)) {
                        iter.remove();
                        Entity entity = world.getEntity(retrieval.entityId);
                        if (entity != null) {
                            retrieval.listener.acceptRetrieved(entity);
                        } else {
                            ServerTaskQueue.submit(2, () -> {
                                Entity retrieved = world.getEntity(retrieval.entityId);
                                if (retrieved != null) {
                                    retrieval.listener.acceptRetrieved(retrieved);
                                } else {
                                    retrieval.listener.notifyMissing();
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public static void insertEntity(MinecraftServer server, UUID topHatId, Entity initialEntity) {
        ServerWorld topHatDim = getTopHatDim(server);
        if (topHatDim == null) return;
        Random random = topHatDim.random;

        BlockPos entryPoint = new BlockPos(random.nextInt(2000) - 1000, 256, random.nextInt(2000) - 1000);

        initialEntity.teleportTo(
            new TeleportTarget(topHatDim, Vec3d.ofBottomCenter(entryPoint), Vec3d.ZERO, initialEntity.getYaw(),
                initialEntity.getPitch(),
                newEntity -> getInstanceUnchecked(topHatDim).insertEntity(topHatId, newEntity)));
    }

    public static boolean retrieveEntity(MinecraftServer server, UUID topHatId, ServerWorld targetWorld,
                                         Vec3d targetPos, TopHatRetrieveListener listener) {
        ServerWorld topHatDim = getTopHatDim(server);
        if (topHatDim == null) return false;

        return getInstanceUnchecked(topHatDim).retrieveEntity(topHatDim, topHatId, new TopHatRetrieveListener() {
            @Override
            public void acceptRetrieved(Entity retrieved) {
                retrieved.teleportTo(
                    new TeleportTarget(targetWorld, targetPos, Vec3d.ZERO, retrieved.getYaw(), retrieved.getPitch(),
                        listener::acceptRetrieved));
            }

            @Override
            public void notifyMissing() {
                listener.notifyMissing();
            }
        });
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
    private final Map<UUID, ChunkPos> entityChunks = new LinkedHashMap<>();

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

        NbtCompound entityChunksNbt = new NbtCompound();
        for (var entry : entityChunks.entrySet()) {
            NbtCompound entityChunk = new NbtCompound();
            entityChunk.putInt("x", entry.getValue().x);
            entityChunk.putInt("z", entry.getValue().z);
            entityChunksNbt.put(entry.getKey().toString(), entityChunk);
        }

        nbt.put("entity_chunks", entityChunksNbt);

        return nbt;
    }

    private void insertEntity(UUID topHatId, Entity entityInTopHatDimension) {
        Deque<UUID> entityQueue = topHatQueues.computeIfAbsent(topHatId, _k -> new ArrayDeque<>());
        entityQueue.add(entityInTopHatDimension.getUuid());
        putEntityChunk(entityInTopHatDimension);
    }

    private boolean retrieveEntity(ServerWorld topHatDim, UUID topHatId, TopHatRetrieveListener onRetrieve) {
        Deque<UUID> entityQueue = topHatQueues.get(topHatId);
        if (entityQueue == null) return false;

        return retrieveEntity(topHatDim, entityQueue, onRetrieve);
    }

    private boolean retrieveEntity(ServerWorld topHatDim, Deque<UUID> entityQueue, TopHatRetrieveListener onRetrieve) {
        if (entityQueue.isEmpty()) return false;

        UUID entityId = entityQueue.poll();
        assert entityId != null;

        Entity retrieved = topHatDim.getEntity(entityId);
        if (retrieved == null) {
            ChunkPos entityChunk = entityChunks.remove(entityId);
            if (entityChunk != null) {
                topHatDim.getChunkManager()
                    .addTicket(AMChunkTicketTypes.TOP_HAT_PRE_TELEPORT, entityChunk, 0, entityId);
            }

            PENDING_RETRIEVALS.add(new Retrieval(onRetrieve, entityId, entityChunk));
        } else {
            entityChunks.remove(entityId);
            onRetrieve.acceptRetrieved(retrieved);
        }

        return true;
    }

    private void putEntityChunk(Entity entity) {
        entityChunks.put(entity.getUuid(), entity.getChunkPos());
    }
}
