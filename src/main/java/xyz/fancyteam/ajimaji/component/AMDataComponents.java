package xyz.fancyteam.ajimaji.component;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMDataComponents {
    public static final ComponentType<MagicCarpetComponent> MAGIC_CARPET_DATA =
        ComponentType.<MagicCarpetComponent>builder().codec(MagicCarpetComponent.CODEC).build();
    public static final ComponentType<TopHatIdComponent> TOP_HAT_ID =
        ComponentType.<TopHatIdComponent>builder().codec(TopHatIdComponent.CODEC).build();

    public static void register() {
        register("magic_carpet_data", MAGIC_CARPET_DATA);
        register("top_hat_id", TOP_HAT_ID);
    }

    private static void register(String path, ComponentType<?> type) {
        Registry.register(Registries.DATA_COMPONENT_TYPE, id(path), type);
    }
}
