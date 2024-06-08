package xyz.fancyteam.ajimaji.component;

import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class AMDataComponents {
    public static final ComponentType<NbtComponent> MAGIC_CARPET_DATA = register("magic_carpet_data",
            builder -> builder.codec(NbtComponent.CODEC));

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, (builderOperator.apply(ComponentType.builder())).build());
    }

    public static void register() {
        // We need to load this class so the static fields are initialized,
        // but we don't actually have to do anything here :/
        // Silly, but if we don't do this it loads the static fields after
        // the registries have been synced and crashes.
    }
}
