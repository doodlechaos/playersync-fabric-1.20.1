package com.doodlechaos.playersync;

import com.doodlechaos.playersync.Sync.PlayerRecorder;
import net.fabricmc.api.ClientModInitializer;

public class PlayerSyncClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize the scroll callback after the client window is ready
        PlayerRecorder.initMouseScrollCallback();
    }
}