package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.Sync.InputEventContainers.*;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerRecorderV2 {

    private static final List<PlayerKeyframeV2> recordedKeyframes = new ArrayList<>();

    // Lists to record events between frames
    private static final List<InputEvent> recordedInputsBuffer = new ArrayList<>();

    // Called from your mixin to record a mouse button event.
    public static void recordMouseButtonEvent(MouseButtonEvent event) {
        recordedInputsBuffer.add(event);
    }

    // Called from your mixin to record a mouse scroll event.
    public static void recordMouseScrollEvent(MouseScrollEvent event) {
        recordedInputsBuffer.add(event);
    }

    // Called from your mixin to record a mouse position event.
    public static void recordMousePosEvent(MousePosEvent event) {
        recordedInputsBuffer.add(event);
    }

    // Called from your mixin to record a keyboard event.
    public static void recordKeyboardEvent(KeyboardEvent event) {
        recordedInputsBuffer.add(event);
    }

    public static List<PlayerKeyframeV2> getRecordedKeyframes() {
        return recordedKeyframes;
    }

    public static void clearRecordedKeyframes() {
        recordedKeyframes.clear();
    }

    /**
     * Records a new keyframe that captures both player and input data.
     */
    public static void RecordKeyframe(){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player == null) {
            LOGGER.error("No player to record keyframe");
            return;
        }

        float partialTick = client.getTickDelta();
        Vec3d playerPos = client.player.getLerpedPos(partialTick);

        // Use the number of keyframes as the frame number (or use your own frame counter if available)
        long frameNumber = recordedKeyframes.size();

        // Create a merged keyframe with both keyboard and mouse inputs.
        PlayerKeyframeV2 keyframe = new PlayerKeyframeV2(
                frameNumber,
                playerPos,
                client.player.getYaw(),
                client.player.getPitch(),
                new ArrayList<>(recordedInputsBuffer)
        );

        // Add the new keyframe to our in-memory list.
        recordedKeyframes.add(keyframe);

        clearRecordedInputsBuffer();
    }

    public static void clearRecordedInputsBuffer(){
        // Clear the recorded event lists so they don't accumulate events across frames.
        recordedInputsBuffer.clear();
    }

    public static void SaveRecToFile(String recName){
        File recFile = new File(PlayerSyncFolderUtils.getPlayerSyncFolder(), recName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recFile))) {
            for (PlayerKeyframeV2 keyframe : recordedKeyframes) {
                writer.write(keyframe.ToLine());
                writer.newLine();
            }
            LOGGER.info("Recording saved to file: " + recFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error saving recording to file: " + recFile.getAbsolutePath(), e);
        }
    }

    public static void LoadRecFromFile(String recName){
        File recFile = new File(PlayerSyncFolderUtils.getPlayerSyncFolder(), recName);
        try (BufferedReader reader = new BufferedReader(new FileReader(recFile))) {
            recordedKeyframes.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        PlayerKeyframeV2 keyframe = new PlayerKeyframeV2(line);
                        recordedKeyframes.add(keyframe);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Error parsing keyframe: " + line, e);
                    }
                }
            }
            LOGGER.info("Recording loaded from file: " + recFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Error loading recording from file: " + recFile.getAbsolutePath(), e);
        }
    }

}
