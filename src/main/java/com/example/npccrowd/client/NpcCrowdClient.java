package com.example.npccrowd.client;

import com.example.npccrowd.NpcCrowd;
import com.example.npccrowd.block.ModBlocks;
import com.example.npccrowd.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class NpcCrowdClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.NPC, NpcEntityRenderer::new);

        // Up arrow = re-roll your own gear.
        NpcCrowdKeybinds.register();

        // Cutout so the hidden (empty) model variant behaves itself.
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.NPC_SPAWNER, RenderLayer.getCutout());

        // Load skins once at startup...
        NpcSkinManager.reload();

        // ...and again on every resource reload, so F3+T picks up skins you
        // dropped into the folder without restarting the game.
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return new Identifier(NpcCrowd.MOD_ID, "npc_skins");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        NpcSkinManager.reload();
                    }
                }
        );
    }
}
