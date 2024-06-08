package xyz.fancyteam.ajimaji.item;

import net.minecraft.block.Block;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.entry.RegistryEntry;

public class TopHatBlockItem extends ArmorBlockItem {
    public TopHatBlockItem(Block block, RegistryEntry<ArmorMaterial> material,
                           Settings settings) {
        super(block, material, Type.HELMET, settings);
    }
}
