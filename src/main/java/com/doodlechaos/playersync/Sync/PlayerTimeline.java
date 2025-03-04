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

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerTimeline {

    private static boolean recording = false;
    private static boolean playingBack = false;

    public static int GetRecFrame(){
        return getRecordedKeyframes().size();
    };

    public static int playheadIndex = 0;

    public static boolean isRecording(){return recording;}
    public static boolean isPlayingBack(){return playingBack;}

    public static void setRecording(boolean value)
    {
        //TODO: I think we need to wait for the next game tick before we actually start recording for determinism
        recording = value;
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
    public static void RecordKeyframe(){
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if(player == null) {
            LOGGER.error("No player to record keyframe");
            return;
        }

        float partialTick = client.getTickDelta();
        Vec3d lerpedPlayerPos = player.getLerpedPos(partialTick);

        // Use the number of keyframes as the frame number (or use your own frame counter if available)
        long frameNumber = recordedKeyframes.size();

        // Create a merged keyframe with both keyboard and mouse inputs.
        PlayerKeyframe keyframe = new PlayerKeyframe(
                frameNumber,
                lerpedPlayerPos,
                player.getYaw(partialTick),//lerpedYaw,//client.player.getYaw(),
                player.getPitch(partialTick),//lerpedPitch,//client.player.getPitch(),
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

        if(client.player == null)
            return;

        if(playheadIndex >= PlayerTimeline.getRecordedKeyframes().size()){
            setPlayingBack(false, playheadIndex);
            playheadIndex = 0;
            client.player.sendMessage(Text.literal("Playback complete"), false);

            if(VideoRenderer.isRendering()){
                VideoRenderer.FinishRendering();
            }

            return;
        }

        PlayerKeyframe keyframe = GetCurKeyframe();

        if(keyframe == null)
            return;

        if(playheadIndex == 0){
            client.player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
            client.player.setYaw(keyframe.playerYaw);
            client.player.setPitch(keyframe.playerPitch);
        }

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
