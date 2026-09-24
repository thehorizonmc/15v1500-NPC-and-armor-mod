package com.example.npccrowd.block;

import com.example.npccrowd.NpcCrowd;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block NPC_SPAWNER = Registry.register(
            Registries.BLOCK,
            new Identifier(NpcCrowd.MOD_ID, "npc_spawner"),
            new NpcSpawnerBlock(Block.Settings.copy(Blocks.STONE)
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.METAL)
                    // Non-opaque so neighbouring blocks still draw their faces
                    // once this one is hidden - otherwise you'd get a hole.
                    .nonOpaque())
    );

    public static final Item NPC_SPAWNER_ITEM = Registry.register(
            Registries.ITEM,
            new Identifier(NpcCrowd.MOD_ID, "npc_spawner"),
            new BlockItem(NPC_SPAWNER, new Item.Settings())
    );

    private ModBlocks() {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(NPC_SPAWNER_ITEM));
    }
}
