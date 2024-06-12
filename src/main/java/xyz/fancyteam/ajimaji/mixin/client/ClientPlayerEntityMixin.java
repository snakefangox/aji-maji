package xyz.fancyteam.ajimaji.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @ModifyConstant(method = "tickMovement", constant = @Constant(floatValue = 0.2f))
    private float ajimaji$fastCardWielding(float constant) {
        return ((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK) ? 1.0f : constant;
    }
}
