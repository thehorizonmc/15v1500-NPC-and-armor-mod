package com.example.npccrowd.entity;

import com.example.npccrowd.util.RandomGear;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Arm;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * A completely stationary, non-AI humanoid that renders with the vanilla
 * player model. It exists purely to be looked at from a distance, so almost
 * every behaviour a normal mob has is switched off.
 * <p>
 * Synced to the client via the data tracker:
 * <ul>
 *   <li><b>skin seed</b> - an arbitrary int; the client turns it into an index
 *       into whatever skins it found in the {@code random-skins} folder. The
 *       server never needs to know how many skins exist.</li>
 *   <li><b>look target</b> - an absolute world position. This is the key to
 *       the "locked in" behaviour: {@code /facenpcshere} stores the point
 *       <em>once</em>, and the renderer recomputes the angle toward that fixed
 *       point every frame. The stored value never changes as the player walks
 *       around, so the heads stay put.</li>
 * </ul>
 */
public class NpcEntity extends LivingEntity {
    private static final TrackedData<Integer> SKIN_SEED =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> HAS_LOOK_TARGET =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> LOOK_X =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> LOOK_Y =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> LOOK_Z =
            DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // Armor order matches vanilla's entity slot ids: feet, legs, chest, head.
    private final DefaultedList<ItemStack> armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> hands = DefaultedList.ofSize(2, ItemStack.EMPTY);

    /** The spawner block that created this NPC, so breaking it can clean up. */
    @Nullable
    private BlockPos anchorPos;

    public NpcEntity(EntityType<? extends NpcEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createNpcAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SKIN_SEED, 0);
        this.dataTracker.startTracking(HAS_LOOK_TARGET, false);
        this.dataTracker.startTracking(LOOK_X, 0.0f);
        this.dataTracker.startTracking(LOOK_Y, 0.0f);
        this.dataTracker.startTracking(LOOK_Z, 0.0f);
    }

    // ------------------------------------------------------------ randomize

    /** Called once by the spawner block: pick a skin and roll all the gear. */
    public void randomize(Random random) {
        this.setSkinSeed(random.nextInt(Integer.MAX_VALUE));
        RandomGear.applyAll(this);
    }

    // ----------------------------------------------------------- tracked data

    public int getSkinSeed() {
        return this.dataTracker.get(SKIN_SEED);
    }

    public void setSkinSeed(int seed) {
        this.dataTracker.set(SKIN_SEED, seed);
    }

    public void setLookTarget(Vec3d target) {
        this.dataTracker.set(LOOK_X, (float) target.x);
        this.dataTracker.set(LOOK_Y, (float) target.y);
        this.dataTracker.set(LOOK_Z, (float) target.z);
        this.dataTracker.set(HAS_LOOK_TARGET, true);
    }

    public void clearLookTarget() {
        this.dataTracker.set(HAS_LOOK_TARGET, false);
    }

    /** The fixed world point this NPC should face, or null to use its spawn yaw. */
    @Nullable
    public Vec3d getLookTarget() {
        if (!this.dataTracker.get(HAS_LOOK_TARGET)) {
            return null;
        }
        return new Vec3d(
                this.dataTracker.get(LOOK_X),
                this.dataTracker.get(LOOK_Y),
                this.dataTracker.get(LOOK_Z)
        );
    }

    @Nullable
    public BlockPos getAnchorPos() {
        return this.anchorPos;
    }

    public void setAnchorPos(@Nullable BlockPos pos) {
        this.anchorPos = pos == null ? null : pos.toImmutable();
    }

    // ------------------------------------------------------------- equipment

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armor;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot.getType() == EquipmentSlot.Type.HAND) {
            return this.hands.get(slot.getEntitySlotId());
        }
        return this.armor.get(slot.getEntitySlotId());
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        if (slot.getType() == EquipmentSlot.Type.HAND) {
            this.hands.set(slot.getEntitySlotId(), stack);
        } else {
            this.armor.set(slot.getEntitySlotId(), stack);
        }
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    // -------------------------------------------------------- stay perfectly still

    /** No-op: this is what actually stops the NPC from ever moving. */
    @Override
    public void travel(Vec3d movementInput) {
    }

    @Override
    public void tick() {
        super.tick();
        // Belt and braces: kill any velocity something else tried to apply.
        this.setVelocity(Vec3d.ZERO);
        this.velocityDirty = false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void pushAwayFrom(net.minecraft.entity.Entity entity) {
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
    }

    /** Never render a nametag, even if something managed to name it. */
    @Override
    public boolean shouldRenderName() {
        return false;
    }

    // ------------------------------------------------------------------- nbt

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SkinSeed", this.getSkinSeed());

        nbt.putBoolean("HasLookTarget", this.dataTracker.get(HAS_LOOK_TARGET));
        nbt.putFloat("LookX", this.dataTracker.get(LOOK_X));
        nbt.putFloat("LookY", this.dataTracker.get(LOOK_Y));
        nbt.putFloat("LookZ", this.dataTracker.get(LOOK_Z));

        if (this.anchorPos != null) {
            nbt.putLong("AnchorPos", this.anchorPos.asLong());
        }

        NbtList armorList = new NbtList();
        for (ItemStack stack : this.armor) {
            armorList.add(stack.isEmpty() ? new NbtCompound() : stack.writeNbt(new NbtCompound()));
        }
        nbt.put("ArmorItems", armorList);

        NbtList handList = new NbtList();
        for (ItemStack stack : this.hands) {
            handList.add(stack.isEmpty() ? new NbtCompound() : stack.writeNbt(new NbtCompound()));
        }
        nbt.put("HandItems", handList);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setSkinSeed(nbt.getInt("SkinSeed"));

        this.dataTracker.set(HAS_LOOK_TARGET, nbt.getBoolean("HasLookTarget"));
        this.dataTracker.set(LOOK_X, nbt.getFloat("LookX"));
        this.dataTracker.set(LOOK_Y, nbt.getFloat("LookY"));
        this.dataTracker.set(LOOK_Z, nbt.getFloat("LookZ"));

        this.anchorPos = nbt.contains("AnchorPos") ? BlockPos.fromLong(nbt.getLong("AnchorPos")) : null;

        if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
            NbtList armorList = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < this.armor.size() && i < armorList.size(); i++) {
                this.armor.set(i, ItemStack.fromNbt(armorList.getCompound(i)));
            }
        }
        if (nbt.contains("HandItems", NbtElement.LIST_TYPE)) {
            NbtList handList = nbt.getList("HandItems", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < this.hands.size() && i < handList.size(); i++) {
                this.hands.set(i, ItemStack.fromNbt(handList.getCompound(i)));
            }
        }
    }
}
