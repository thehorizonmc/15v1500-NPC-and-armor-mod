package com.npcspawner.client;

import com.npcspawner.NpcSpawnerMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class NpcSpawnerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NpcConfig.load();
        BlockEntityRendererFactories.register(NpcSpawnerMod.NPC_BLOCK_ENTITY, NpcRenderer::new);

        // Up-arrow: re-roll your own gear.
        PlayerGearKeybind.register();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            // /hidenpcs : toggle rendering of every NPC
            dispatcher.register(ClientCommandManager.literal("hidenpcs").executes(ctx -> {
                NpcClientState.hidden = !NpcClientState.hidden;
                ctx.getSource().sendFeedback(Text.literal(NpcClientState.hidden
                        ? "NPC models hidden. Run /hidenpcs again to show them."
                        : "NPC models shown."));
                return 1;
            }));

            // /facenpcshere : lock every NPC's gaze onto where your head is RIGHT NOW
            // /facenpcshere reset : go back to looking straight ahead
            dispatcher.register(ClientCommandManager.literal("facenpcshere")
                    .executes(ctx -> {
                        Vec3d eye = ctx.getSource().getPlayer().getEyePos();
                        NpcClientState.lookTarget = eye;
                        ctx.getSource().sendFeedback(Text.literal(String.format(
                                "NPCs are now looking at %.1f, %.1f, %.1f", eye.x, eye.y, eye.z)));
                        return 1;
                    })
                    .then(ClientCommandManager.literal("reset").executes(ctx -> {
                        NpcClientState.lookTarget = null;
                        ctx.getSource().sendFeedback(Text.literal("NPCs look straight ahead again."));
                        return 1;
                    })));

            // /reloadnpcskins : re-scan .minecraft/random-skins without restarting
            dispatcher.register(ClientCommandManager.literal("reloadnpcskins").executes(ctx -> {
                int count = NpcSkins.reload();
                ctx.getSource().sendFeedback(Text.literal("Loaded " + count + " skin(s) from random-skins"
                        + (count == 0 ? " (using the green fallback)." : ".")));
                return 1;
            }));
        });
    }
}
