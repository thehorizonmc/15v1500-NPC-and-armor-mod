package com.npcspawner.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.npcspawner.NpcSpawnerMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Reads .minecraft/config/npcspawner.json (created with defaults on first launch). */
public final class NpcConfig {
    // Index = armor slot id: 0 boots, 1 leggings, 2 chestplate, 3 helmet
    private static final String[] SLOT_KEYS = {"feet", "legs", "chest", "head"};
    private static final String[] woodArmorIds = {
            "minecraft:leather_boots", "minecraft:leather_leggings",
            "minecraft:leather_chestplate", "minecraft:leather_helmet"};
    private static final Item[] LEATHER = {
            Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
    private static final boolean[] warned = new boolean[4];

    private NpcConfig() {}

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("npcspawner.json");
        try {
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (root.has("woodArmor")) {
                        JsonObject wood = root.getAsJsonObject("woodArmor");
                        for (int i = 0; i < 4; i++) {
                            if (wood.has(SLOT_KEYS[i])) woodArmorIds[i] = wood.get(SLOT_KEYS[i]).getAsString();
                        }
                    }
                }
            } else {
                JsonObject root = new JsonObject();
                JsonObject wood = new JsonObject();
                for (int i = 0; i < 4; i++) wood.addProperty(SLOT_KEYS[i], woodArmorIds[i]);
                root.add("woodArmor", wood);
                Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));
            }
        } catch (Exception e) {
            NpcSpawnerMod.LOGGER.warn("Could not read/write npcspawner.json, using defaults", e);
        }
    }

    /** The "wooden" armor piece for a slot. Vanilla has none, so this comes from the config (default: leather). */
    public static Item woodArmor(int slot) {
        Identifier id = Identifier.tryParse(woodArmorIds[slot]);
        if (id != null) {
            Optional<Item> item = Registries.ITEM.getOrEmpty(id);
            if (item.isPresent() && item.get() != Items.AIR) return item.get();
        }
        if (!warned[slot]) {
            warned[slot] = true;
            NpcSpawnerMod.LOGGER.warn("npcspawner.json: item '{}' for wood armor slot '{}' not found, using leather",
                    woodArmorIds[slot], SLOT_KEYS[slot]);
        }
        return LEATHER[slot];
    }
}
