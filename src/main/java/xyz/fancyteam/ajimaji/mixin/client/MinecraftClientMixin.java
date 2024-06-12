package xyz.fancyteam.ajimaji.mixin.client;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.option.GameOptions;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Final
    public GameOptions options;

    @Shadow
    protected abstract void doItemUse();

    @Inject(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 10)
    )
    private void ajimaji$cardAttack(CallbackInfo ci) {
        while (options.attackKey.wasPressed()) {
            doItemUse();
        }
    }
}
