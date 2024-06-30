package xyz.fancyteam.ajimaji.item;

import java.util.List;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import static xyz.fancyteam.ajimaji.AjiMaji.tt;

public class BunnyEarsItem extends ArmorItem {
    public BunnyEarsItem(RegistryEntry<ArmorMaterial> material,
                         Settings settings) {
        super(material, Type.HELMET, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(tt("tooltip", "bunny_ears"));
    }
}
