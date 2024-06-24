package xyz.fancyteam.ajimaji.misc;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

import net.minecraft.item.FilledMapItem;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.sound.SoundCategory;

import net.minecraft.text.Text;

import org.apache.commons.text.similarity.LevenshteinDistance;

import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;

import java.util.Comparator;
import java.util.Locale;

public class AMEvents {

    public static final String ACTIVATION_PHRASE = "four winds carry me";

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(AMEvents::checkFourWindsMessages);
    }

    static boolean checkFourWindsMessages(SignedMessage message, ServerPlayerEntity sender,
                                          MessageType.Parameters params) {
        if (sender.getVehicle() instanceof MagicCarpetEntity magicCarpet) {
            var msg = message.getContent().getString().trim().toLowerCase(Locale.ROOT);
            if (!msg.startsWith(ACTIVATION_PHRASE)) return true;

            msg = msg.replaceFirst(ACTIVATION_PHRASE, "");
            var map = magicCarpet.getMap();
            if (!map.isEmpty()) {
                var mapState = FilledMapItem.getMapState(map, sender.getWorld());
                if (mapState == null) return true;

                var d = LevenshteinDistance.getDefaultInstance();
                String finalMsg = msg;
                var target = mapState.getBanners().stream()
                    .filter(b -> b.name().isPresent())
                    .min(Comparator.comparing(a -> d.apply(a.name().map(Text::getString).get(), finalMsg)));
                if (target.isPresent()) {
                    var pos = target.get().pos();
                    magicCarpet.updateTrackedPosition(pos.getX(), pos.getY() + 2, pos.getZ());
                    sender.getWorld()
                        .playSound(null, pos, AMSoundEvents.CARPET_TELEPORT, SoundCategory.NEUTRAL, 2F, 1F);
                    return false;
                }
            }
        }

        return true;
    }
}
