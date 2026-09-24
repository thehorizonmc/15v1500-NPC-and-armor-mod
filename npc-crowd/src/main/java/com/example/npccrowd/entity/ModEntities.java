package com.example.npccrowd.entity;

import com.example.npccrowd.NpcCrowd;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<NpcEntity> NPC = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(NpcCrowd.MOD_ID, "npc"),
            EntityType.Builder.create(NpcEntity::new, SpawnGroup.MISC)
                    // Same footprint as a player.
                    .setDimensions(0.6f, 1.8f)
                    // Crowds are meant to be seen from far away, so track generously.
                    .maxTrackingRange(128)
                    .trackingTickInterval(3)
                    .build("npc")
    );

    private ModEntities() {
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(NPC, NpcEntity.createNpcAttributes());
    }
}
