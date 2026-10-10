package dev.cattyn.doublehandcrystal.mixin;

import dev.cattyn.doublehandcrystal.DoubleHandCrystalClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {
    @Shadow
    protected EditBox input;

    @Unique
    private String doubleHandCrystal$completionBase;
    @Unique
    private String doubleHandCrystal$completionOutput;
    @Unique
    private List<String> doubleHandCrystal$completions = List.of();
    @Unique
    private int doubleHandCrystal$completionIndex;

    @Inject(method = "handleChatInput(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void doubleHandCrystal$handleCommand(String message, boolean addToRecent, CallbackInfo ci) {
        String command = message.trim();
        String commandPrefix = DoubleHandCrystalClient.commandPrefix();
        if (!command.startsWith(commandPrefix)) {
            return;
        }

        String commandBody = command.substring(commandPrefix.length());
        if (!commandBody.equals("item") && !commandBody.startsWith("item ")) {
            return;
        }

        ci.cancel();
        if (addToRecent) {
            Minecraft.getInstance().gui.hud.getChat().addRecentChat(command);
        }

        String[] parts = commandBody.split("\\s+", 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            this.doubleHandCrystal$sendFeedback("Usage: " + commandPrefix + "item <item_id>");
            return;
        }

        Identifier itemId = Identifier.tryParse(parts[1]);
        if (itemId == null || !BuiltInRegistries.ITEM.keySet().contains(itemId)) {
            this.doubleHandCrystal$sendFeedback("Unknown item: " + parts[1]);
            return;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId).orElseThrow().value();
        DoubleHandCrystalClient.setAppearanceItem(item);
        this.doubleHandCrystal$sendFeedback("Main-hand appearance set to " + BuiltInRegistries.ITEM.getKey(item));
    }

    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void doubleHandCrystal$completeItem(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() != 258) {
            this.doubleHandCrystal$clearCompletion();
            return;
        }

        String commandPrefix = DoubleHandCrystalClient.commandPrefix() + "item ";
        String inputValue = this.input.getValue();
        if (!inputValue.startsWith(commandPrefix)) {
            this.doubleHandCrystal$clearCompletion();
            return;
        }

        String itemPrefix = inputValue.substring(commandPrefix.length());
        if (itemPrefix.chars().anyMatch(Character::isWhitespace)) {
            this.doubleHandCrystal$clearCompletion();
            return;
        }

        if (inputValue.equals(this.doubleHandCrystal$completionOutput)
                && this.doubleHandCrystal$completionBase != null) {
            this.doubleHandCrystal$completionIndex =
                    (this.doubleHandCrystal$completionIndex + 1) % this.doubleHandCrystal$completions.size();
        } else {
            this.doubleHandCrystal$completionBase = inputValue.substring(0, commandPrefix.length());
            this.doubleHandCrystal$completionCompletions(itemPrefix);
            if (this.doubleHandCrystal$completions.isEmpty()) {
                this.doubleHandCrystal$clearCompletion();
                return;
            }
            this.doubleHandCrystal$completionIndex = 0;
        }

        this.doubleHandCrystal$completionOutput =
                this.doubleHandCrystal$completionBase + this.doubleHandCrystal$completions.get(this.doubleHandCrystal$completionIndex);
        this.input.setValue(this.doubleHandCrystal$completionOutput);
        cir.setReturnValue(true);
    }

    @Unique
    private void doubleHandCrystal$completionCompletions(String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        boolean includeNamespace = prefix.indexOf(':') >= 0;
        List<String> matches = new ArrayList<>();
        for (Identifier itemId : BuiltInRegistries.ITEM.keySet()) {
            String suggestion = includeNamespace || !itemId.getNamespace().equals("minecraft")
                    ? itemId.toString()
                    : itemId.getPath();
            boolean matchesPrefix = includeNamespace
                    ? itemId.toString().startsWith(normalizedPrefix)
                    : itemId.getPath().startsWith(normalizedPrefix)
                            || itemId.getNamespace().startsWith(normalizedPrefix);
            if (matchesPrefix) {
                matches.add(suggestion);
            }
        }
        matches.sort(String::compareTo);
        this.doubleHandCrystal$completions = matches;
    }

    @Unique
    private void doubleHandCrystal$sendFeedback(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.literal(message));
        }
    }

    @Unique
    private void doubleHandCrystal$clearCompletion() {
        this.doubleHandCrystal$completionBase = null;
        this.doubleHandCrystal$completionOutput = null;
        this.doubleHandCrystal$completions = List.of();
        this.doubleHandCrystal$completionIndex = 0;
    }
}
