package dev.cattyn.doublehandcrystal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DoubleHandCrystalClient implements ClientModInitializer {
    private static final String DEFAULT_PREFIX = "!";
    private static final Identifier DEFAULT_ITEM_ID = Identifier.withDefaultNamespace("end_crystal");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("doublehandcrystal.properties");

    private static String commandPrefix = DEFAULT_PREFIX;
    private static Item appearanceItem = Items.END_CRYSTAL;

    @Override
    public void onInitializeClient() {
        loadConfig();
    }

    public static String commandPrefix() {
        return commandPrefix;
    }

    public static Item appearanceItem() {
        return appearanceItem;
    }

    public static ItemStack appearanceItemStack() {
        return new ItemStack(appearanceItem);
    }

    public static void setAppearanceItem(Item item) {
        saveConfig(commandPrefix, item);
        appearanceItem = item;
    }

    public static void setCommandPrefix(String prefix) {
        if (!isValidPrefix(prefix)) {
            throw new IllegalArgumentException("Prefix must be 1-4 non-whitespace characters and cannot be '/'.");
        }

        saveConfig(prefix, appearanceItem);
        commandPrefix = prefix;
    }

    public static boolean isValidPrefix(String prefix) {
        return prefix != null
                && !prefix.isBlank()
                && prefix.length() <= 4
                && prefix.chars().noneMatch(Character::isWhitespace)
                && !prefix.contains("/");
    }

    private static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load DoubleHandCrystal configuration", e);
        }

        String prefix = properties.getProperty("prefix", DEFAULT_PREFIX);
        if (!isValidPrefix(prefix)) {
            throw new IllegalStateException("Invalid command prefix in " + CONFIG_PATH + ": " + prefix);
        }

        Identifier itemId = Identifier.tryParse(properties.getProperty("item", DEFAULT_ITEM_ID.toString()));
        if (itemId == null || !BuiltInRegistries.ITEM.keySet().contains(itemId)) {
            throw new IllegalStateException("Invalid item ID in " + CONFIG_PATH);
        }

        commandPrefix = prefix;
        appearanceItem = BuiltInRegistries.ITEM.get(itemId).orElseThrow().value();
    }

    private static void saveConfig(String prefix, Item item) {
        Properties properties = new Properties();
        properties.setProperty("prefix", prefix);
        properties.setProperty("item", BuiltInRegistries.ITEM.getKey(item).toString());

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "DoubleHandCrystal client settings");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save DoubleHandCrystal configuration", e);
        }
    }
}
