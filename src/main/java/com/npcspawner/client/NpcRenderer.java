package com.npcspawner.client;

import com.npcspawner.NpcSpawnBlock;
import com.npcspawner.NpcSpawnBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * Draws the NPC. Skin/gear are derived from the block position, so they are stable across
 * reloads and need no networking. Body/head orientation is computed here, every frame, from
 * NpcClientState.lookTarget - it is not stored anywhere else.
 */
public class NpcRenderer implements BlockEntityRenderer<NpcSpawnBlockEntity> {
    private static final double EYE_HEIGHT = 1.62;
    private final Map<NpcSpawnBlockEntity, NpcPlayerEntity> cache = new WeakHashMap<>();

    public NpcRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(NpcSpawnBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (NpcClientState.hidden) return;
        World world = be.getWorld();
        if (!(world instanceof ClientWorld clientWorld)) return;

        NpcPlayerEntity npc = cache.get(be);
        if (npc == null || npc.skinGeneration != NpcSkins.generation()) {
            npc = create(be, clientWorld);
            cache.put(be, npc);
        }

        BlockPos pos = be.getPos();
        double cx = pos.getX() + 0.5, cz = pos.getZ() + 0.5;
        npc.setPosition(cx, pos.getY(), cz);
        npc.lastRenderX = cx;
        npc.lastRenderY = pos.getY();
        npc.lastRenderZ = cz;

        // ---- body: faces the way the block was placed, unless /facenpcshere is active ----
        float bodyYaw = be.getCachedState().get(NpcSpawnBlock.FACING).asRotation();
        float headYaw;
        float pitch = 0f;
        // ---- recomputed every frame from the locked target (nothing is stored per-NPC) ----
        Vec3d target = NpcClientState.lookTarget;
        if (target != null) {
            double dx = target.x - cx;
            double dy = target.y - (pos.getY() + EYE_HEIGHT);
            double dz = target.z - cz;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            // whole body turns toward the target...
            bodyYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
            // ...and the head tilts up/down to meet it
            pitch = MathHelper.clamp((float) (-(MathHelper.atan2(dy, horizontal) * (180.0 / Math.PI))), -90f, 90f);
        }
        headYaw = bodyYaw;

        npc.setYaw(bodyYaw);
        npc.prevYaw = bodyYaw;
        npc.bodyYaw = bodyYaw;
        npc.prevBodyYaw = bodyYaw;
        npc.headYaw = headYaw;
        npc.prevHeadYaw = headYaw;
        npc.setPitch(pitch);
        npc.prevPitch = pitch;

        // The block itself is see-through, so sample light at its own position.
        int lightCoords = WorldRenderer.getLightmapCoordinates(world, pos);

        // tickDelta = 0 and a frozen age keep the pose completely static (no idle arm sway).
        MinecraftClient.getInstance().getEntityRenderDispatcher()
                .render(npc, 0.5, 0.0, 0.5, 0.0f, 0.0f, matrices, vertexConsumers, lightCoords);
    }

    private static NpcPlayerEntity create(NpcSpawnBlockEntity be, ClientWorld world) {
        Random rng = new Random(mix(be.getPos().asLong()));
        Identifier skin = NpcSkins.pick(rng);
        NpcGear gear = NpcGear.roll(rng);
        return new NpcPlayerEntity(world, skin, gear, NpcSkins.generation());
    }

    /** splitmix64 finalizer: neighbouring block positions must not give correlated random rolls. */
    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    @Override
    public boolean rendersOutsideBoundingBox(NpcSpawnBlockEntity be) {
        return true; // the model is taller than the 1x1x1 block
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }
}
