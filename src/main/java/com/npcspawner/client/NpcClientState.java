package com.npcspawner.client;

import net.minecraft.util.math.Vec3d;

/** Purely client-side state driven by the commands. The renderer reads it every frame. */
public final class NpcClientState {
    /** /hidenpcs toggles this. */
    public static boolean hidden = false;

    /** Set by /facenpcshere (a snapshot of the player's eye position). null = look straight ahead. */
    public static Vec3d lookTarget = null;

    private NpcClientState() {}
}
