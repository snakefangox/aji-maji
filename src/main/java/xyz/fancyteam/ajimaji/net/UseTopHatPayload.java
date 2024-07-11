package xyz.fancyteam.ajimaji.net;

import com.kneelawk.knet.api.util.NetByteBuf;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import xyz.fancyteam.ajimaji.AjiMaji;

public record UseTopHatPayload(BlockHitResult hit, Hand hand) implements CustomPayload {
    public static final Id<UseTopHatPayload> ID = new Id<>(AjiMaji.id("use_top_hat"));
    public static final PacketCodec<NetByteBuf, UseTopHatPayload> CODEC =
        PacketCodec.ofStatic((buf, payload) -> {
                buf.writeBlockHitResult(payload.hit());
                buf.writeEnum(payload.hand());
            },
            buf -> new UseTopHatPayload(buf.readBlockHitResult(), buf.readEnumConstant(Hand.class)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
