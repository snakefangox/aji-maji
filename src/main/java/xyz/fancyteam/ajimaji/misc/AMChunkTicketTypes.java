package xyz.fancyteam.ajimaji.misc;

import java.util.UUID;

import net.minecraft.server.world.ChunkTicketType;

public class AMChunkTicketTypes {
    public static final ChunkTicketType<UUID> TOP_HAT_PRE_TELEPORT =
        ChunkTicketType.create("aji-maji:top_hat_pre_teleport", UUID::compareTo, 300);
}
