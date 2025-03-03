package com.doodlechaos.playersync;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class PlayerSyncClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //scheduleScrollCallbackRegistration();
    }


/*    private void scheduleScrollCallbackRegistration() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.getWindow() != null && client.getWindow().getHandle() != 0) {
                // Window is ready; initialize the scroll callback.
                PlayerRecorder.initMouseScrollCallback();
            } else {
                // Window not ready; reschedule for the next tick.
                scheduleScrollCallbackRegistration();
            }
        });
    }*/
}