package com.example.npccrowd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tiny helper for weighted random selection.
 * <p>
 * Add entries with relative integer weights, then call {@link #pick(Random)}.
 * Weights are only compared against each other, so an entry with weight 2 is
 * twice as likely as one with weight 1.
 */
public class WeightedPool<T> {
    private final List<T> values = new ArrayList<>();
    private final List<Integer> weights = new ArrayList<>();
    private int totalWeight = 0;

    public WeightedPool<T> add(T value, int weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive, got " + weight);
        }
        values.add(value);
        weights.add(weight);
        totalWeight += weight;
        return this;
    }

    public T pick(Random random) {
        if (values.isEmpty()) {
            throw new IllegalStateException("Cannot pick from an empty WeightedPool");
        }
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < values.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return values.get(i);
            }
        }
        return values.get(values.size() - 1);
    }
}
