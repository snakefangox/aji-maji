package xyz.fancyteam.ajimaji.net;

import com.kneelawk.knet.api.channel.NoContextPlayChannel;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.fabric.api.KNetFabric;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import xyz.fancyteam.ajimaji.item.TopHatBlockItem;

public class AMNet {
    private static final NoContextPlayChannel<UseTopHatPayload> USE_TOP_HAT =
        NoContextPlayChannel.ofNetCodec(UseTopHatPayload.ID, UseTopHatPayload.CODEC)
            .recvServer(AMNet::receiveUseTopHat);

    public static void register() {
        KNetFabric.registerPlay(USE_TOP_HAT);
    }

    public static void useTopHat(BlockHitResult hit, Hand hand) {
        USE_TOP_HAT.sendToServer(new UseTopHatPayload(hit, hand));
    }

    private static void receiveUseTopHat(UseTopHatPayload payload, PlayPayloadHandlingContext ctx) {
        PlayerEntity player = ctx.mustGetPlayer();
        Hand hand = payload.hand();
        BlockHitResult hit = payload.hit();

        ActionResult res =
            TopHatBlockItem.useOnBlock(player, ctx.mustGetLevel(), hit.getSide(), hit.getPos(), hit.getBlockPos(),
                player.getStackInHand(hand));

        if (res.shouldSwingHand()) {
            player.swingHand(hand, true);
        }
    }
}
