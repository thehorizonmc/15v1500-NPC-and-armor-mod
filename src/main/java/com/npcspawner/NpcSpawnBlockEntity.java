package com.npcspawner;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

/** Holds no data: everything about the NPC is derived on the client from the block position. */
public class NpcSpawnBlockEntity extends BlockEntity {
    public NpcSpawnBlockEntity(BlockPos pos, BlockState state) {
        super(NpcSpawnerMod.NPC_BLOCK_ENTITY, pos, state);
    }
}
