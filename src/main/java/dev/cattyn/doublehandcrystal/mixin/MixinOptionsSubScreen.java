package dev.cattyn.doublehandcrystal.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.cattyn.doublehandcrystal.gui.PrefixSettingsScreen;

@Mixin(OptionsSubScreen.class)
public abstract class MixinOptionsSubScreen {
    @Shadow
    @Nullable
    protected OptionsList list;

    @Inject(method = "addContents", at = @At("TAIL"))
    private void doubleHandCrystal$addSettingsButton(CallbackInfo ci) {
        if ((Object) this instanceof ControlsScreen && this.list != null) {
            this.list.addSmall(Button.builder(Component.literal("DoubleHandCrystal Settings"),
                    button -> Minecraft.getInstance().gui.setScreen(
                            new PrefixSettingsScreen((OptionsSubScreen) (Object) this)))
                    .build(),
                    null);
        }
    }
}
