package xyz.fancyteam.ajimaji.component;

import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Uuids;

public record TopHatIdComponent(UUID topHatId) {
    public static final Codec<TopHatIdComponent> CODEC =
        Uuids.CODEC.xmap(TopHatIdComponent::new, TopHatIdComponent::topHatId);

    public static TopHatIdComponent getOrCreate(ItemStack stack) {
        TopHatIdComponent component = stack.get(AMDataComponents.TOP_HAT_ID);
        if (component == null) {
            component = new TopHatIdComponent(UUID.randomUUID());
            stack.set(AMDataComponents.TOP_HAT_ID, component);
        }

        return component;
    }
}
