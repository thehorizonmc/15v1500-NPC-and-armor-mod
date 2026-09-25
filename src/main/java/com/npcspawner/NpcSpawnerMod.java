package com.npcspawner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcSpawnerMod implements ModInitializer {
    public static final String MOD_ID = "npcspawner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static NpcSpawnBlock NPC_BLOCK;
    public static Item NPC_BLOCK_ITEM;
    public static BlockEntityType<NpcSpawnBlockEntity> NPC_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        Identifier id = new Identifier(MOD_ID, "npc_spawn_block");

        NPC_BLOCK = Registry.register(Registries.BLOCK, id,
                new NpcSpawnBlock(AbstractBlock.Settings.create()
                        .strength(1.0f)
                        .sounds(BlockSoundGroup.WOOD)
                        .nonOpaque()));

        NPC_BLOCK_ITEM = Registry.register(Registries.ITEM, id,
                new BlockItem(NPC_BLOCK, new Item.Settings()));

        NPC_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(NpcSpawnBlockEntity::new, NPC_BLOCK).build());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(NPC_BLOCK_ITEM));

        // Up-arrow "randomize my gear" keybind support.
        PlayerGearNetworking.register();
    }
}
