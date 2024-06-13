package xyz.fancyteam.ajimaji.misc;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import xyz.fancyteam.ajimaji.AjiMaji;

public class AMSoundEvents {
    public static final SoundEvent CARD_THROW = SoundEvent.of(AjiMaji.id("item.card_deck.throw"));
    public static final SoundEvent CARD_HIT = SoundEvent.of(AjiMaji.id("entity.playing_card.hit"));

    public static void register() {
        Registry.register(Registries.SOUND_EVENT, CARD_THROW.getId(), CARD_THROW);
        Registry.register(Registries.SOUND_EVENT, CARD_HIT.getId(), CARD_HIT);
    }
}
