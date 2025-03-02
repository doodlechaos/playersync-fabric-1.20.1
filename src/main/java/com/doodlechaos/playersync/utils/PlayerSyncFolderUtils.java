package com.doodlechaos.playersync.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

import java.io.File;

public class PlayerSyncFolderUtils {

    public static File getPlayerSyncFolder() {
        MinecraftClient client = MinecraftClient.getInstance();
        File baseFolder;
        if (client.getServer() != null) {
            baseFolder = client.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        } else {
            baseFolder = client.runDirectory;
        }
        File folder = new File(baseFolder, "playersync");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}
