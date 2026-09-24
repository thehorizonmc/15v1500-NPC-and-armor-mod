package com.example.npccrowd.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Random;

/**
 * Every "what gear does this NPC get" decision lives here, so all the
 * probabilities are in one file and easy to tweak.
 *
 * <h2>Tool (main hand)</h2>
 * 8 options: {diamond, iron, wooden, golden} x {sword, axe}. Iron is weighted
 * twice as heavily as the other three materials, giving:
 * iron sword 20%, iron axe 20%, and 10% for each of the other six.
 *
 * <h2>Armor</h2>
 * Vanilla has no wooden armor, so the low tier is leather. Same weighting as
 * tools: iron 40%, diamond 20%, gold 20%, leather 20%.
 * <p>
 * Three steps, run fresh every time:
 * <ol>
 *   <li>Roll each slot present/absent independently. Chance the slot is
 *       <em>empty</em>: helmet 30%, chestplate 20%, leggings 25%, boots 15%.</li>
 *   <li>If that left 2+ pieces present, 50% chance to upgrade to a complete
 *       4-piece set.</li>
 *   <li>If the final set is complete (either naturally or via step 2), 70%
 *       chance all four pieces share one rolled material; otherwise each
 *       present piece rolls its material independently.</li>
 * </ol>
 *
 * <h2>Shield (off hand)</h2>
 * Flat 50% chance of a shield, otherwise the off hand is left empty.
 */
public final class RandomGear {
    private static final Random RANDOM = new Random();

    // --- Armor slot "missing" chances -------------------------------------
    private static final double HELMET_MISSING_CHANCE = 0.30;
    private static final double CHESTPLATE_MISSING_CHANCE = 0.20;
    private static final double LEGGINGS_MISSING_CHANCE = 0.25;
    private static final double BOOTS_MISSING_CHANCE = 0.15;

    // --- Set biases --------------------------------------------------------
    private static final double COMPLETE_SET_BIAS_CHANCE = 0.50;
    private static final double MATCHING_MATERIAL_CHANCE = 0.70;

    // --- Shield ------------------------------------------------------------
    private static final double SHIELD_CHANCE = 0.50;

    public static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public enum ArmorMaterial {
        LEATHER, IRON, GOLD, DIAMOND
    }

    private RandomGear() {
    }

    /**
     * Rolls and applies a tool, a full armor outfit, and a possible shield.
     * Used by both the NPC spawner block and the player randomize keybind, so
     * an NPC and a keybound player roll from exactly the same tables.
     */
    public static void applyAll(LivingEntity target) {
        applyTool(target);
        applyArmor(target);
        applyShield(target);
    }

    // ---------------------------------------------------------------- tool

    public static ItemStack rollTool() {
        WeightedPool<Item> pool = new WeightedPool<Item>()
                .add(Items.DIAMOND_SWORD, 1)
                .add(Items.DIAMOND_AXE, 1)
                .add(Items.IRON_SWORD, 2)
                .add(Items.IRON_AXE, 2)
                .add(Items.WOODEN_SWORD, 1)
                .add(Items.WOODEN_AXE, 1)
                .add(Items.GOLDEN_SWORD, 1)
                .add(Items.GOLDEN_AXE, 1);
        return new ItemStack(pool.pick(RANDOM));
    }

    public static void applyTool(LivingEntity target) {
        target.equipStack(EquipmentSlot.MAINHAND, rollTool());
    }

    // --------------------------------------------------------------- shield

    public static void applyShield(LivingEntity target) {
        if (RANDOM.nextDouble() < SHIELD_CHANCE) {
            target.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        } else {
            target.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    // ---------------------------------------------------------------- armor

    public static ArmorMaterial rollMaterial() {
        WeightedPool<ArmorMaterial> pool = new WeightedPool<ArmorMaterial>()
                .add(ArmorMaterial.LEATHER, 1)
                .add(ArmorMaterial.IRON, 2)
                .add(ArmorMaterial.GOLD, 1)
                .add(ArmorMaterial.DIAMOND, 1);
        return pool.pick(RANDOM);
    }

    public static Item itemFor(EquipmentSlot slot, ArmorMaterial material) {
        if (slot == EquipmentSlot.HEAD) {
            switch (material) {
                case LEATHER: return Items.LEATHER_HELMET;
                case IRON:    return Items.IRON_HELMET;
                case GOLD:    return Items.GOLDEN_HELMET;
                default:      return Items.DIAMOND_HELMET;
            }
        } else if (slot == EquipmentSlot.CHEST) {
            switch (material) {
                case LEATHER: return Items.LEATHER_CHESTPLATE;
                case IRON:    return Items.IRON_CHESTPLATE;
                case GOLD:    return Items.GOLDEN_CHESTPLATE;
                default:      return Items.DIAMOND_CHESTPLATE;
            }
        } else if (slot == EquipmentSlot.LEGS) {
            switch (material) {
                case LEATHER: return Items.LEATHER_LEGGINGS;
                case IRON:    return Items.IRON_LEGGINGS;
                case GOLD:    return Items.GOLDEN_LEGGINGS;
                default:      return Items.DIAMOND_LEGGINGS;
            }
        } else if (slot == EquipmentSlot.FEET) {
            switch (material) {
                case LEATHER: return Items.LEATHER_BOOTS;
                case IRON:    return Items.IRON_BOOTS;
                case GOLD:    return Items.GOLDEN_BOOTS;
                default:      return Items.DIAMOND_BOOTS;
            }
        }
        throw new IllegalArgumentException("Not an armor slot: " + slot);
    }

    /**
     * Rolls which armor slots are filled and with what material, WITHOUT
     * touching any entity. Returns a 4-element array lined up with
     * {@link #ARMOR_SLOTS} (head, chest, legs, feet); a null entry means that
     * slot stays empty.
     * <p>
     * Both the vanilla and the cosmetic armor paths call this, so the odds can
     * never drift apart between them.
     */
    public static ArmorMaterial[] rollArmorPlan() {
        boolean[] present = new boolean[4];
        present[0] = RANDOM.nextDouble() >= HELMET_MISSING_CHANCE;
        present[1] = RANDOM.nextDouble() >= CHESTPLATE_MISSING_CHANCE;
        present[2] = RANDOM.nextDouble() >= LEGGINGS_MISSING_CHANCE;
        present[3] = RANDOM.nextDouble() >= BOOTS_MISSING_CHANCE;

        int presentCount = 0;
        for (boolean p : present) {
            if (p) presentCount++;
        }

        // Step 2: bias toward complete sets once there's already some armor.
        if (presentCount >= 2 && RANDOM.nextDouble() < COMPLETE_SET_BIAS_CHANCE) {
            present = new boolean[] {true, true, true, true};
        }

        boolean completeSet = present[0] && present[1] && present[2] && present[3];

        // Step 3: complete sets are usually (not always) uniform.
        ArmorMaterial uniformMaterial = null;
        if (completeSet && RANDOM.nextDouble() < MATCHING_MATERIAL_CHANCE) {
            uniformMaterial = rollMaterial();
        }

        ArmorMaterial[] plan = new ArmorMaterial[4];
        for (int i = 0; i < 4; i++) {
            if (!present[i]) {
                plan[i] = null;
            } else {
                plan[i] = uniformMaterial != null ? uniformMaterial : rollMaterial();
            }
        }
        return plan;
    }

    /** Equips a rolled outfit using real vanilla armor items. */
    public static void applyArmor(LivingEntity target) {
        ArmorMaterial[] plan = rollArmorPlan();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            EquipmentSlot slot = ARMOR_SLOTS[i];
            if (plan[i] == null) {
                target.equipStack(slot, ItemStack.EMPTY);
            } else {
                target.equipStack(slot, new ItemStack(itemFor(slot, plan[i])));
            }
        }
    }

}
