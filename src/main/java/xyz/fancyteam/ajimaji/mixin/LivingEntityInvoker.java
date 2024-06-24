package xyz.fancyteam.ajimaji.mixin;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker("computeFallDamage")
    int aji_maji_computeFallDamage(float fallDistance, float damageMultiplier);
}
