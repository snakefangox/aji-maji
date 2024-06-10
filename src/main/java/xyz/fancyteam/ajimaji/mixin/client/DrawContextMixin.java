package xyz.fancyteam.ajimaji.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    @Unique
    private ItemStack ajimaji$drawingStack;

    @Inject(
        method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void ajimaji$captureItemStack(TextRenderer textRenderer, ItemStack stack, int x, int y,
                                          String countOverride, CallbackInfo ci) {
        ajimaji$drawingStack = stack;
    }

    @WrapWithCondition(
        method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I")
    )
    private boolean ajimaji$skipCardDeckCount(DrawContext instance, TextRenderer textRenderer, String text, int x,
                                              int y, int color, boolean shadow) {
        return ajimaji$drawingStack == null || !ajimaji$drawingStack.isOf(AMItems.CARD_DECK);
    }
}
