package xyz.fancyteam.ajimaji.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("removeFromDimension")
    void aji_maji_removeFromDimension();
}
