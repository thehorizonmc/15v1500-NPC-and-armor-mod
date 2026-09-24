package com.example.npccrowd.client;

import com.example.npccrowd.network.ModNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Press the up arrow key to re-roll your own gear.
 * <p>
 * Bound to the arrow key rather than a letter so it doesn't collide with WASD
 * or any of the usual mod hotkeys. It's rebindable like any other control, in
 * Options → Controls → Key Binds under the "NPC Crowd" category.
 * <p>
 * Each press fires one message to the server, which does the actual rolling.
 * Holding the key down does nothing extra - {@code wasPressed()} drains
 * discrete press events rather than reporting "is held", so you get one outfit
 * per tap instead of a new one every tick.
 */
public final class NpcCrowdKeybinds {
    private static KeyBinding randomizeSelfKey;

    private NpcCrowdKeybinds() {
    }

    public static void register() {
        randomizeSelfKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npccrowd.randomize_self",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "category.npccrowd.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (randomizeSelfKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.RANDOMIZE_SELF, PacketByteBufs.empty());
            }
        });
    }
}
