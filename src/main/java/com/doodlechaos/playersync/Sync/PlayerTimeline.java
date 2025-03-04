package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.Sync.InputEventContainers.*;
import com.doodlechaos.playersync.VideoRenderer;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerTimeline {

    public static boolean startRecNextTick = false;
    private static boolean recording = false;

    public static boolean startPlaybackNextTick = false;
    private static boolean playingBack = false;

    public static int GetRecFrame(){
        return getRecordedKeyframes().size();
    };

    public static int playheadIndex = 0;

    public static boolean isRecording(){return recording;}
    public static boolean isPlayingBack(){return playingBack;}

    public static void setRecording(boolean value)
    {
        recording = value;
        playheadIndex = GetRecFrame();
        onStateUpdate();
    }

    public static void setPlayingBack(boolean value, int frame){
        playingBack = value;
        playheadIndex = frame;
        onStateUpdate();
    }

    private static void onStateUpdate(){
        AudioSync.setPlaying(playingBack || recording);
    }

    private static final List<PlayerKeyframe> recordedKeyframes = new ArrayList<>();
    private static final List<InputEvent> recordedInputsBuffer = new ArrayList<>();

    public static void registerDebugText(){
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            String debugText = "";
            if(isRecording()){
                debugText += "recFrame: " + GetRecFrame();
            }
            if(isPlayingBack()){
                debugText += " playingBack: " + playheadIndex;
            }
            // Draw the text at position (10, 10) with white color (0xFFFFFF)
            matrixStack.drawText(client.textRenderer, debugText, 10, 20, 0xFFFFFF, false);
        });
    }

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
        LOGGER.info("recorded keyboard event on frame: " + PlayerTimeline.playheadIndex);
        recordedInputsBuffer.add(event);
    }

    public static List<PlayerKeyframe> getRecordedKeyframes() {
        return recordedKeyframes;
    }

    public static void clearRecordedKeyframes() {
        recordedKeyframes.clear();
    }

    /**
     * Records a new keyframe that captures both player and input data.
     */
    public static void CreateKeyframe(){
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if(player == null) {
            LOGGER.error("No player to record keyframe");
            return;
        }

        float tickDelta = client.getTickDelta();
        if(playheadIndex == 0){
            LOGGER.error("playheadIndex: " + playheadIndex + " tickDelta: " + tickDelta);
        }
        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        // Use the number of keyframes as the frame number (or use your own frame counter if available)
        long frameNumber = recordedKeyframes.size();

        // Create a merged keyframe with both keyboard and mouse inputs.
        PlayerKeyframe keyframe = new PlayerKeyframe(
                frameNumber,
                tickDelta,
                lerpedPlayerPos,
                player.getYaw(tickDelta),//lerpedYaw,//client.player.getYaw(),
                player.getPitch(tickDelta),//lerpedPitch,//client.player.getPitch(),
                new ArrayList<>(recordedInputsBuffer)
        );

        // Add the new keyframe to our in-memory list.
        recordedKeyframes.add(keyframe);

        playheadIndex = getRecordedKeyframes().size();

        clearRecordedInputsBuffer();
    }

    public static void clearRecordedInputsBuffer(){
        // Clear the recorded event lists so they don't accumulate events across frames.
        recordedInputsBuffer.clear();
    }


    public static PlayerKeyframe GetCurKeyframe(){
        List<PlayerKeyframe> frames = PlayerTimeline.getRecordedKeyframes();

        if(playheadIndex < 0 || playheadIndex >= frames.size())
            return null;

        return frames.get(playheadIndex);
    }

    public static void setPlayerFromKeyframe(){

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        float tickDelta = client.getTickDelta();


        if(player == null)
            return;

        if(playheadIndex >= PlayerTimeline.getRecordedKeyframes().size()){
            setPlayingBack(false, 0);
            player.sendMessage(Text.literal("Playback complete"), false);

            if(VideoRenderer.isRendering()){
                VideoRenderer.FinishRendering();
            }

            return;
        }

        PlayerKeyframe keyframe = GetCurKeyframe();

        if(keyframe == null)
            return;

        //if(playheadIndex == 0){
            player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
            player.setYaw(keyframe.playerYaw);
            player.setPitch(keyframe.playerPitch);
        //}
/*
       Vec3d lerpedPos = player.getLerpedPos(tickDelta);
        boolean tickDeltaMismatch = tickDelta != keyframe.tickDelta;
        boolean positionMismatch = !lerpedPos.equals(keyframe.playerPos);
        boolean yawMismatch = player.getYaw(tickDelta) != keyframe.playerYaw;
        boolean pitchMismatch = player.getPitch(tickDelta) != keyframe.playerPitch;

        if (tickDeltaMismatch || positionMismatch || yawMismatch || pitchMismatch) {
            StringBuilder diffLog = new StringBuilder("DETECTED NON-DETERMINISM. PlayheadIndex: " + playheadIndex + ". ");

            if (tickDeltaMismatch) {
                diffLog.append(keyframe.tickDelta).append(" != ").append(tickDelta).append(". ");
            }

            double deltaX = 0, deltaY = 0, deltaZ = 0;
            if (positionMismatch) {
                deltaX = Math.abs(lerpedPos.x - keyframe.playerPos.x);
                deltaY = Math.abs(lerpedPos.y - keyframe.playerPos.y);
                deltaZ = Math.abs(lerpedPos.z - keyframe.playerPos.z);
                diffLog.append("Pos delta -> X: ").append(deltaX)
                        .append(", Y: ").append(deltaY)
                        .append(", Z: ").append(deltaZ).append(". ");
            }

            if (yawMismatch) {
                double yawDelta = Math.abs(player.getYaw(tickDelta) - keyframe.playerYaw);
                diffLog.append("Yaw delta: ").append(yawDelta).append(". ");
            }

            if (pitchMismatch) {
                double pitchDelta = Math.abs(player.getPitch(tickDelta) - keyframe.playerPitch);
                diffLog.append("Pitch delta: ").append(pitchDelta).append(". ");
            }

            LOGGER.info(diffLog.toString());

            // If the position difference exceeds 0.1 on any axis, force an update.
            if (deltaX > 0.1 || deltaY > 0.1 || deltaZ > 0.1) {
                LOGGER.error("Player position off by more than 0.1 on at least one axis. Forcing updatePosition to keyframe position: " + keyframe.playerPos);
                player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
            }
        }*/

    }

    public static void SimulateInputsFromKeyframe(){
        PlayerKeyframe keyframe = GetCurKeyframe();

        if(keyframe == null)
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();

        for (InputEvent ie : keyframe.recordedInputEvents) {
            ie.simulate(window, client);
        }
    }


    public static void SaveRecToFile(String recName){
        File recFile = new File(PlayerSyncFolderUtils.getPlayerSyncFolder(), recName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recFile))) {
            for (PlayerKeyframe keyframe : recordedKeyframes) {
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
                        PlayerKeyframe keyframe = new PlayerKeyframe(line);
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
