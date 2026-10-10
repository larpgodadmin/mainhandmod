package dev.cattyn.doublehandcrystal.gui;

import dev.cattyn.doublehandcrystal.DoubleHandCrystalClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PrefixSettingsScreen extends Screen {
    private final Screen parent;
    private EditBox prefixInput;
    private Button saveButton;

    public PrefixSettingsScreen(Screen parent) {
        super(Component.literal("DoubleHandCrystal Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.prefixInput = new EditBox(this.font, centerX - 90, this.height / 2 - 12, 180, 20,
                Component.literal("Chat command prefix"));
        this.prefixInput.setMaxLength(4);
        this.prefixInput.setSuggestion("Command prefix (1-4 characters)");
        this.prefixInput.setValue(DoubleHandCrystalClient.commandPrefix());
        this.addRenderableWidget(this.prefixInput);

        int buttonY = this.height / 2 + 18;
        this.saveButton = this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            DoubleHandCrystalClient.setCommandPrefix(this.prefixInput.getValue());
            this.minecraft.gui.setScreen(this.parent);
        }).bounds(centerX - 90, buttonY, 85, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.minecraft.gui.setScreen(this.parent))
                .bounds(centerX + 5, buttonY, 85, 20).build());

        this.prefixInput.setResponder(value -> this.saveButton.active = DoubleHandCrystalClient.isValidPrefix(value));
    }
}
