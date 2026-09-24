package com.example.npccrowd.command;

import com.example.npccrowd.block.ModBlocks;
import com.example.npccrowd.block.NpcSpawnerBlock;
import com.example.npccrowd.entity.ModEntities;
import com.example.npccrowd.entity.NpcEntity;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class NpcCommands {
    private NpcCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("hidenpcs")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> setSpawnersHidden(ctx.getSource(), true)));

            dispatcher.register(CommandManager.literal("shownpcs")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> setSpawnersHidden(ctx.getSource(), false)));

            dispatcher.register(CommandManager.literal("facenpcshere")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> faceHere(ctx.getSource())));

            dispatcher.register(CommandManager.literal("clearnpcfacing")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(ctx -> clearFacing(ctx.getSource())));
        });
    }

    /**
     * Flips the HIDDEN blockstate on the spawner block under every NPC. We
     * find the blocks via the NPCs (each remembers its anchor) rather than
     * scanning the world, which keeps this cheap no matter how big the build is.
     */
    private static int setSpawnersHidden(ServerCommandSource source, boolean hidden) {
        ServerWorld world = source.getWorld();
        int changed = 0;

        for (NpcEntity npc : world.getEntitiesByType(ModEntities.NPC, npc -> true)) {
            BlockPos anchor = npc.getAnchorPos();
            if (anchor == null) {
                continue;
            }
            BlockState state = world.getBlockState(anchor);
            if (state.isOf(ModBlocks.NPC_SPAWNER) && state.get(NpcSpawnerBlock.HIDDEN) != hidden) {
                world.setBlockState(anchor, state.with(NpcSpawnerBlock.HIDDEN, hidden), Block.NOTIFY_ALL);
                changed++;
            }
        }

        final int count = changed;
        source.sendFeedback(
                () -> Text.literal((hidden ? "Hid " : "Revealed ") + count + " NPC spawner block(s)."),
                true
        );
        return count;
    }

    /**
     * Records the player's current eye position on every loaded NPC. Only the
     * point is stored - the actual head/body angle is recomputed from it each
     * frame on the render side, so the NPCs keep staring at this exact spot
     * even after the player walks away.
     */
    private static int faceHere(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ServerWorld world = source.getWorld();
        Vec3d eyePos = player.getEyePos();

        int count = 0;
        for (NpcEntity npc : world.getEntitiesByType(ModEntities.NPC, npc -> true)) {
            npc.setLookTarget(eyePos);
            count++;
        }

        final int finalCount = count;
        source.sendFeedback(
                () -> Text.literal(String.format(
                        "%d NPC(s) now facing (%.2f, %.2f, %.2f).",
                        finalCount, eyePos.x, eyePos.y, eyePos.z)),
                true
        );
        return count;
    }

    /** Undo {@code /facenpcshere}: go back to each NPC's original spawn yaw. */
    private static int clearFacing(ServerCommandSource source) {
        ServerWorld world = source.getWorld();
        int count = 0;
        for (NpcEntity npc : world.getEntitiesByType(ModEntities.NPC, npc -> true)) {
            npc.clearLookTarget();
            count++;
        }
        final int finalCount = count;
        source.sendFeedback(() -> Text.literal("Reset facing on " + finalCount + " NPC(s)."), true);
        return count;
    }
}
