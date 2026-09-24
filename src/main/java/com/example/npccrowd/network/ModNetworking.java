package com.example.npccrowd.network;

import com.example.npccrowd.NpcCrowd;
import com.example.npccrowd.util.RandomGear;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * The client can't just equip the player itself - the server owns inventory
 * state, and anything the client set on its own would be corrected away on the
 * next sync. So the keybind sends an empty "please re-roll me" message and the
 * roll happens here, server side, where it sticks and gets broadcast to
 * everyone who can see the player.
 */
public final class ModNetworking {
    /** Empty payload; the message itself is the whole request. */
    public static final Identifier RANDOMIZE_SELF =
            new Identifier(NpcCrowd.MOD_ID, "randomize_self");

    private ModNetworking() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(RANDOMIZE_SELF,
                (server, player, handler, buf, responseSender) -> {
                    // Network callbacks arrive on the netty thread; touching
                    // the world or an inventory off the main thread is a race.
                    server.execute(() -> applyTo(player));
                });
    }

    private static void applyTo(ServerPlayerEntity player) {
        // Overwrites both hands and all four armor slots, every time.
        RandomGear.applyAll(player);

        // Push the new inventory contents to the player's own screen so the
        // hotbar and armor slots visibly update straight away.
        player.playerScreenHandler.sendContentUpdates();
    }
}
