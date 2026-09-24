package com.example.npccrowd.client;

import com.example.npccrowd.entity.NpcEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Renders NPCs with the vanilla slim ("Alex") player model, plus armor and
 * held items.
 * <p>
 * <b>Facing is resolved here, not on the server.</b> The entity only stores a
 * fixed world point; every frame this renderer works out the angle from the
 * NPC's eyes to that point and applies it to both the body and the head, so
 * they turn together. Because the stored point never moves, the NPCs keep
 * staring at the spot where the player stood when {@code /facenpcshere} ran,
 * regardless of where the player goes afterwards.
 */
public class NpcEntityRenderer extends LivingEntityRenderer<NpcEntity, PlayerEntityModel<NpcEntity>> {

    public NpcEntityRenderer(EntityRendererFactory.Context ctx) {
        // 'true' = slim arms, i.e. the Alex model.
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true), 0.5f);

        this.addFeature(new ArmorFeatureRenderer<>(
                this,
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()
        ));
        this.addFeature(new HeldItemFeatureRenderer<>(this, ctx.getHeldItemRenderer()));
    }

    @Override
    public Identifier getTexture(NpcEntity entity) {
        return NpcSkinManager.getSkin(entity.getSkinSeed());
    }

    /** No nametags, ever. */
    @Override
    protected boolean hasLabel(NpcEntity entity) {
        return false;
    }

    @Override
    public void render(NpcEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        Vec3d target = entity.getLookTarget();

        if (target != null) {
            double dx = target.x - entity.getX();
            double dy = target.y - entity.getEyeY();
            double dz = target.z - entity.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) (Math.toDegrees(MathHelper.atan2(dz, dx)) - 90.0);
            float targetPitch = (float) (-Math.toDegrees(MathHelper.atan2(dy, horizontal)));

            // Set both current and previous so nothing interpolates - the pose
            // should snap to the locked angle, not drift toward it.
            entity.setBodyYaw(targetYaw);
            entity.prevBodyYaw = targetYaw;
            entity.setHeadYaw(targetYaw);
            entity.prevHeadYaw = targetYaw;
            entity.setYaw(targetYaw);
            entity.prevYaw = targetYaw;
            entity.setPitch(targetPitch);
            entity.prevPitch = targetPitch;

            yaw = targetYaw;
        }

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
