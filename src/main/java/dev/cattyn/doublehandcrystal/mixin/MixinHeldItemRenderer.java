package dev.cattyn.doublehandcrystal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.cattyn.doublehandcrystal.DoubleHandCrystalClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemInHandRenderer.class)
public class MixinHeldItemRenderer {
    @ModifyArgs(
            method = "submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;submitArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"
            )
    )
    private void renderItemHook(Args args, @Local(argsOnly = true) LocalPlayer player) {
        InteractionHand hand = args.get(3);
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }
        if (player.getMainHandItem().is(ItemTags.SWORDS)
                && player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            args.set(5, DoubleHandCrystalClient.appearanceItemStack());
        }
    }
}
