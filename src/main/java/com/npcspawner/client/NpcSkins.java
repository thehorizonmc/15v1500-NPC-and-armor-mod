package com.npcspawner.client;

import com.npcspawner.NpcSpawnerMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Loads every PNG in .minecraft/random-skins as a texture. Falls back to a solid green skin. */
public final class NpcSkins {
    private static final List<Identifier> skins = new ArrayList<>();
    private static final List<Identifier> registered = new ArrayList<>();
    private static Identifier fallback;
    private static boolean loaded = false;
    private static int generation = 0;

    private NpcSkins() {}

    /** Bumped on every reload so cached NPCs know to pick a new skin. */
    public static int generation() {
        ensureLoaded();
        return generation;
    }

    public static synchronized void ensureLoaded() {
        if (!loaded) reload();
    }

    public static Identifier pick(Random rng) {
        ensureLoaded();
        return skins.isEmpty() ? fallback : skins.get(rng.nextInt(skins.size()));
    }

    /** @return number of skins found in the folder */
    public static synchronized int reload() {
        TextureManager tm = MinecraftClient.getInstance().getTextureManager();
        for (Identifier id : registered) tm.destroyTexture(id);
        registered.clear();
        skins.clear();
        generation++;
        loaded = true;

        // solid green fallback (ABGR: alpha, blue, green, red)
        NativeImage green = new NativeImage(64, 64, true);
        for (int x = 0; x < 64; x++) for (int y = 0; y < 64; y++) green.setColor(x, y, 0xFF00C800);
        fallback = new Identifier(NpcSpawnerMod.MOD_ID, "skins/g" + generation + "_fallback");
        tm.registerTexture(fallback, new NativeImageBackedTexture(green));
        registered.add(fallback);

        Path dir = FabricLoader.getInstance().getGameDir().resolve("random-skins");
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            NpcSpawnerMod.LOGGER.warn("Could not create {}", dir, e);
        }

        File[] files = dir.toFile().listFiles((d, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));
        if (files != null) {
            Arrays.sort(files);
            int index = 0;
            for (File f : files) {
                try (InputStream in = Files.newInputStream(f.toPath())) {
                    NativeImage image = normalize(NativeImage.read(in));
                    if (image == null) {
                        NpcSpawnerMod.LOGGER.warn("Skipping {} (skins must be 64x64 or 64x32)", f.getName());
                        continue;
                    }
                    Identifier id = new Identifier(NpcSpawnerMod.MOD_ID, "skins/g" + generation + "_" + index++);
                    tm.registerTexture(id, new NativeImageBackedTexture(image));
                    registered.add(id);
                    skins.add(id);
                } catch (Exception e) {
                    NpcSpawnerMod.LOGGER.warn("Could not load skin {}", f.getName(), e);
                }
            }
        }
        NpcSpawnerMod.LOGGER.info("NPC skins: loaded {} from {}", skins.size(), dir);
        return skins.size();
    }

    /** Same fix-ups vanilla applies to downloaded skins (legacy 64x32 -> 64x64, force opaque body). */
    private static NativeImage normalize(NativeImage src) {
        int w = src.getWidth(), h = src.getHeight();
        if (w != 64 || (h != 64 && h != 32)) {
            src.close();
            return null;
        }
        NativeImage img = src;
        boolean legacy = h == 32;
        if (legacy) {
            img = new NativeImage(64, 64, true);
            img.copyFrom(src);
            src.close();
            img.fillRect(0, 32, 64, 32, 0);
            img.copyRect(4, 16, 16, 32, 4, 4, true, false);
            img.copyRect(8, 16, 16, 32, 4, 4, true, false);
            img.copyRect(0, 20, 24, 32, 4, 12, true, false);
            img.copyRect(4, 20, 16, 32, 4, 12, true, false);
            img.copyRect(8, 20, 8, 32, 4, 12, true, false);
            img.copyRect(12, 20, 16, 32, 4, 12, true, false);
            img.copyRect(44, 16, -8, 32, 4, 4, true, false);
            img.copyRect(48, 16, -8, 32, 4, 4, true, false);
            img.copyRect(40, 20, 0, 32, 4, 12, true, false);
            img.copyRect(44, 20, -8, 32, 4, 12, true, false);
            img.copyRect(48, 20, -16, 32, 4, 12, true, false);
            img.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }
        forceOpaque(img, 0, 0, 32, 16);
        if (legacy) clearIfSolid(img, 32, 0, 64, 32);
        forceOpaque(img, 0, 16, 64, 32);
        forceOpaque(img, 16, 48, 48, 64);
        return img;
    }

    private static void forceOpaque(NativeImage img, int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2; x++)
            for (int y = y1; y < y2; y++)
                img.setColor(x, y, img.getColor(x, y) | 0xFF000000);
    }

    /** Old skins often have a solid-colored hat layer; if the region has no transparency, wipe it. */
    private static void clearIfSolid(NativeImage img, int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2; x++)
            for (int y = y1; y < y2; y++)
                if (((img.getColor(x, y) >> 24) & 0xFF) < 128) return;
        for (int x = x1; x < x2; x++)
            for (int y = y1; y < y2; y++)
                img.setColor(x, y, img.getColor(x, y) & 0x00FFFFFF);
    }
}
