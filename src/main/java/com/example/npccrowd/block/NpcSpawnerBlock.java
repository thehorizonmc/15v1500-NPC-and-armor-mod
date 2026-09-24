package com.example.npccrowd.block;

import com.example.npccrowd.entity.ModEntities;
import com.example.npccrowd.entity.NpcEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * Place this block and it spawns one stationary NPC standing on top of it.
 * Break it and that NPC is removed again.
 * <p>
 * The {@link #HIDDEN} blockstate is what {@code /hidenpcs} flips. When hidden,
 * the block maps to a model with no elements, so it renders as nothing at all
 * while still existing (and still being breakable, since the selection
 * outline and collision box remain). That's deliberately a blockstate rather
 * than a client-side toggle: it saves with the world and syncs to every
 * client automatically, including anyone recording from a second account.
 */
public class NpcSpawnerBlock extends Block {
    public static final BooleanProperty HIDDEN = BooleanProperty.of("hidden");

    private static final Random RANDOM = new Random();

    public NpcSpawnerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(HIDDEN, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HIDDEN);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient) {
            return;
        }

        NpcEntity npc = ModEntities.NPC.create(world);
        if (npc == null) {
            return;
        }

        // Face the same way the placer is facing, so a row of blocks placed
        // while walking gives you a crowd all roughly oriented together.
        float yaw = placer != null ? MathHelper.wrapDegrees(placer.getYaw()) : 0.0f;

        npc.refreshPositionAndAngles(
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                yaw, 0.0f
        );
        npc.setHeadYaw(yaw);
        npc.setBodyYaw(yaw);
        npc.setAnchorPos(pos);
        npc.randomize(RANDOM);

        world.spawnEntity(npc);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        // Only clean up when the block is really gone, not when HIDDEN flips.
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            Box searchBox = new Box(pos).expand(2.0);
            List<NpcEntity> anchored = world.getEntitiesByClass(
                    NpcEntity.class,
                    searchBox,
                    npc -> pos.equals(npc.getAnchorPos())
            );
            for (NpcEntity npc : anchored) {
                npc.discard();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
