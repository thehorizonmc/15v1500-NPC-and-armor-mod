package com.example.npccrowd.client;

import com.example.npccrowd.NpcCrowd;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Loads every PNG in {@code .minecraft/random-skins} as a usable entity
 * texture, and hands them out by index.
 * <p>
 * This is entirely client side: each player sees whatever skins <em>they</em>
 * have in that folder. The server only ever sends a plain integer seed per
 * NPC, which the client reduces modulo the number of skins it found. That
 * means the same NPC can show a different skin on a different machine, which
 * is fine here (and means your recording machine is the only one that needs
 * the folder populated).
 * <p>
 * If the folder is missing or empty, everything falls back to a flat green
 * texture generated in memory, so you can immediately tell that skins aren't
 * being picked up.
 */
public final class NpcSkinManager {
    public static final String FOLDER_NAME = "random-skins";

    private static final List<Identifier> SKINS = new ArrayList<>();
    private static Identifier fallbackId;

    private NpcSkinManager() {
    }

    /** Texture for the given NPC seed, always non-null. */
    public static Identifier getSkin(int seed) {
        if (SKINS.isEmpty()) {
            return fallback();
        }
        return SKINS.get(Math.floorMod(seed, SKINS.size()));
    }

    public static int getSkinCount() {
        return SKINS.size();
    }

    /**
     * (Re)scan the folder. Safe to call repeatedly - it's wired to the resource
     * reload, so pressing F3+T in game picks up newly added skins without a
     * full restart.
     */
    public static void reload() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        TextureManager textureManager = client.getTextureManager();

        // Drop anything we registered last time so textures don't leak.
        for (Identifier old : SKINS) {
            textureManager.destroyTexture(old);
        }
        SKINS.clear();

        Path dir = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getGameDir().resolve(FOLDER_NAME);

        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            NpcCrowd.LOGGER.warn("Couldn't create the {} folder", FOLDER_NAME, e);
            return;
        }

        List<Path> pngs = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(pngs::add);
        } catch (Exception e) {
            NpcCrowd.LOGGER.warn("Couldn't read the {} folder", FOLDER_NAME, e);
            return;
        }

        int index = 0;
        for (Path png : pngs) {
            try (InputStream in = Files.newInputStream(png)) {
                NativeImage image = NativeImage.read(in);

                // The player model expects the modern 64x64 layout. Old 64x32
                // skins would render as garbage, so skip them loudly instead.
                if (image.getWidth() != 64 || image.getHeight() != 64) {
                    NpcCrowd.LOGGER.warn(
                            "Skipping {} - skins must be 64x64 (this one is {}x{}). Convert it to the modern skin format.",
                            png.getFileName(), image.getWidth(), image.getHeight());
                    image.close();
                    continue;
                }

                Identifier id = new Identifier(NpcCrowd.MOD_ID, "random_skin_" + index);
                MinecraftClient.getInstance().getTextureManager()
                        .registerTexture(id, new NativeImageBackedTexture(image));
                SKINS.add(id);
                index++;
            } catch (Exception e) {
                NpcCrowd.LOGGER.warn("Failed to load skin {}", png.getFileName(), e);
            }
        }

        if (SKINS.isEmpty()) {
            NpcCrowd.LOGGER.warn(
                    "No usable 64x64 skins found in {} - NPCs will use the green fallback skin.",
                    dir.toAbsolutePath());
        } else {
            NpcCrowd.LOGGER.info("Loaded {} NPC skin(s) from {}", SKINS.size(), dir.toAbsolutePath());
        }
    }

    /** Flat green 64x64 texture, built once and cached. */
    private static Identifier fallback() {
        if (fallbackId != null) {
            return fallbackId;
        }
        NativeImage image = new NativeImage(64, 64, false);
        // NativeImage packs colour as ABGR, so opaque green is 0xFF00FF00.
        final int green = 0xFF00FF00;
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                image.setColor(x, y, green);
            }
        }
        Identifier id = new Identifier(NpcCrowd.MOD_ID, "fallback_skin");
        MinecraftClient.getInstance().getTextureManager()
                .registerTexture(id, new NativeImageBackedTexture(image));
        fallbackId = id;
        return id;
    }
}
