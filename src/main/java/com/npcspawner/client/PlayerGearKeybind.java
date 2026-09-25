package com.npcspawner.client;

import com.npcspawner.PlayerGearNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Press the up arrow key to re-roll your own gear - a fresh random tool,
 * shield and armor set, using the exact same odds as the NPCs.
 * <p>
 * Bound to the arrow key rather than a letter so it doesn't collide with WASD
 * or other mods' hotkeys. Rebindable in Options -> Controls -> Key Binds,
 * under the "NPC Spawn Block" category.
 * <p>
 * {@code wasPressed()} drains discrete press events rather than reporting
 * "is held", so tapping the key once rolls once - holding it down does
 * nothing extra.
 */
public final class PlayerGearKeybind {
    private static KeyBinding randomizeSelfKey;

    private PlayerGearKeybind() {
    }

    public static void register() {
        randomizeSelfKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.npcspawner.randomize_self",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "category.npcspawner.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            while (randomizeSelfKey.wasPressed()) {
                ClientPlayNetworking.send(PlayerGearNetworking.RANDOMIZE_SELF, PacketByteBufs.empty());
            }
        });
    }
}
