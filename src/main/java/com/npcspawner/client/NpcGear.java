package com.npcspawner.client;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.Random;

/** Rolls the random equipment for one NPC. */
public final class NpcGear {
    // ---- tweakables ---------------------------------------------------------------
    /** Chance that each piece is MISSING. Index = slot id: boots, leggings, chestplate, helmet. */
    private static final double[] MISSING_CHANCE = {0.15, 0.25, 0.20, 0.30};
    /** If 2+ pieces were rolled, chance to upgrade to a full set. */
    private static final double FULL_SET_BIAS = 0.50;
    /** If the NPC has a full set, chance that all four pieces are the same material. */
    private static final double SAME_MATERIAL_CHANCE = 0.70;
    private static final double SHIELD_CHANCE = 0.50;
    // -------------------------------------------------------------------------------

    private enum Mat { DIAMOND, IRON, WOOD, GOLD }

    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;
    /** Index = slot id: 0 boots, 1 leggings, 2 chestplate, 3 helmet. */
    public final ItemStack[] armor = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    private NpcGear() {}

    public static NpcGear roll(Random rng) {
        NpcGear gear = new NpcGear();

        // Tool: 4 materials x {sword, axe} = 8 options, iron weighted double.
        gear.mainHand = new ItemStack(weapon(rollMaterial(rng), rng.nextBoolean()));

        // Shield
        if (rng.nextDouble() < SHIELD_CHANCE) gear.offHand = new ItemStack(Items.SHIELD);

        // Which armor pieces exist
        boolean[] has = new boolean[4];
        int count = 0;
        for (int slot = 0; slot < 4; slot++) {
            has[slot] = rng.nextDouble() >= MISSING_CHANCE[slot];
            if (has[slot]) count++;
        }
        if (count >= 2 && count < 4 && rng.nextDouble() < FULL_SET_BIAS) {
            Arrays.fill(has, true);
        }

        // Which material each piece is
        boolean complete = has[0] && has[1] && has[2] && has[3];
        boolean uniform = complete && rng.nextDouble() < SAME_MATERIAL_CHANCE;
        Mat shared = rollMaterial(rng);
        for (int slot = 0; slot < 4; slot++) {
            if (!has[slot]) continue;
            Mat mat = uniform ? shared : rollMaterial(rng);
            gear.armor[slot] = new ItemStack(armorItem(mat, slot));
        }
        return gear;
    }

    /** Diamond 1 : Iron 2 : Wood 1 : Gold 1. */
    private static Mat rollMaterial(Random rng) {
        int n = rng.nextInt(5);
        if (n == 0) return Mat.DIAMOND;
        if (n <= 2) return Mat.IRON;
        if (n == 3) return Mat.WOOD;
        return Mat.GOLD;
    }

    private static Item weapon(Mat mat, boolean axe) {
        switch (mat) {
            case DIAMOND: return axe ? Items.DIAMOND_AXE : Items.DIAMOND_SWORD;
            case IRON:    return axe ? Items.IRON_AXE : Items.IRON_SWORD;
            case WOOD:    return axe ? Items.WOODEN_AXE : Items.WOODEN_SWORD;
            default:      return axe ? Items.GOLDEN_AXE : Items.GOLDEN_SWORD;
        }
    }

    private static Item armorItem(Mat mat, int slot) {
        switch (mat) {
            case DIAMOND:
                return new Item[]{Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET}[slot];
            case IRON:
                return new Item[]{Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET}[slot];
            case WOOD:
                return NpcConfig.woodArmor(slot);
            default:
                return new Item[]{Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_HELMET}[slot];
        }
    }
}
