package com.npcspawner;

import com.npcspawner.client.NpcGear;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Random;

/**
 * Handles the up-arrow "randomize my gear" keybind.
 * <p>
 * The client can't equip the player itself - the server owns inventory state,
 * so anything set client-side would just get corrected away on the next sync.
 * Instead the key press sends an empty "please re-roll me" packet, and this
 * class does the actual rolling and equipping here, server side, using the
 * exact same {@link NpcGear#roll} the NPCs use, so the player and the NPCs
 * can never end up on different odds.
 */
public final class PlayerGearNetworking {
    /** Empty payload - the message itself is the whole request. */
    public static final Identifier RANDOMIZE_SELF =
            new Identifier(NpcSpawnerMod.MOD_ID, "randomize_self");

    private PlayerGearNetworking() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(RANDOMIZE_SELF,
                (server, player, handler, buf, responseSender) -> {
                    // Network callbacks arrive off the main thread; touching
                    // inventory/equipment from there would race the game loop.
                    server.execute(() -> apply(player));
                });
    }

    private static void apply(ServerPlayerEntity player) {
        NpcGear gear = NpcGear.roll(new Random());

        player.equipStack(EquipmentSlot.MAINHAND, gear.mainHand);
        player.equipStack(EquipmentSlot.OFFHAND, gear.offHand);
        // NpcGear.armor index order: 0 boots, 1 leggings, 2 chestplate, 3 helmet.
        player.equipStack(EquipmentSlot.FEET, gear.armor[0]);
        player.equipStack(EquipmentSlot.LEGS, gear.armor[1]);
        player.equipStack(EquipmentSlot.CHEST, gear.armor[2]);
        player.equipStack(EquipmentSlot.HEAD, gear.armor[3]);

        // Push the change to the player's own screen so the hotbar and armor
        // slots visibly update immediately rather than waiting on the next
        // routine sync.
        player.playerScreenHandler.sendContentUpdates();
    }
}
