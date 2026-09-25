package com.npcspawner.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * A client-only, never-added-to-the-world fake player. The block entity renderer feeds it to the
 * vanilla player renderer, which gives us the Alex model, worn armor, held items and the shield
 * for free (and makes mod armor go through the normal armor-layer pipeline).
 */
public class NpcPlayerEntity extends OtherClientPlayerEntity {
    private final SkinTextures skinTextures;
    public final int skinGeneration;

    public NpcPlayerEntity(ClientWorld world, Identifier skin, NpcGear gear, int skinGeneration) {
        super(world, new GameProfile(UUID.randomUUID(), "npc"));
        // Alex (slim) model, no cape/elytra texture
        this.skinTextures = new SkinTextures(skin, null, null, null, SkinTextures.Model.SLIM, true);
        this.skinGeneration = skinGeneration;

        // show hat/jacket/sleeve/pants overlay layers (0x7E = all bits except the cape bit)
        this.getDataTracker().set(PLAYER_MODEL_PARTS, (byte) 0x7E);

        // main hand = hotbar slot 0 (selectedSlot defaults to 0)
        getInventory().main.set(0, gear.mainHand);
        getInventory().offHand.set(0, gear.offHand);
        for (int slot = 0; slot < 4; slot++) {
            getInventory().armor.set(slot, gear.armor[slot]);
        }
    }

    @Override
    public SkinTextures getSkinTextures() {
        return skinTextures;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    /**
     * Vanilla only draws the name tag when the entity is not "invisible to" the local player.
     * The entity is NOT actually invisible (isInvisible() stays false), so the model still renders.
     */
    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return true;
    }
}
