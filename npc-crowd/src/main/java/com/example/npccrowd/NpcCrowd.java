package com.example.npccrowd;

import com.example.npccrowd.block.ModBlocks;
import com.example.npccrowd.command.NpcCommands;
import com.example.npccrowd.entity.ModEntities;
import com.example.npccrowd.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpcCrowd implements ModInitializer {
    public static final String MOD_ID = "npccrowd";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModEntities.initialize();
        ModNetworking.registerServerReceivers();
        NpcCommands.register();
        LOGGER.info("NPC Crowd initialized");
    }
}
