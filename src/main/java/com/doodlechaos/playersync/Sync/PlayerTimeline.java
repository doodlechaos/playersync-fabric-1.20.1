package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.VideoRenderer;
import com.doodlechaos.playersync.mixin.accessor.CameraAccessor;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerTimeline {

    private static boolean recording = false;

    private static boolean playbackEnabled = false;
    private static boolean playbackPaused = false;
    private static boolean playerDetatched = false;

    private static int frame = 0;

    //Getters
    public static boolean isRecording(){return recording;}
    public static boolean isPlaybackEnabled(){return playbackEnabled;}
    public static boolean isPlaybackPaused(){return playbackPaused; }
    public static boolean isPlayerDetatched() {return playerDetatched;}
    public static int GetRecFrame(){ return getRecordedKeyframes().size();};

    public static void registerDebugText(){
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            String debugText = "inPlaybackMode:" + playbackEnabled;
            if(isRecording()){
                debugText += "recFrame: " + GetRecFrame();
            }
            if(isPlaybackEnabled()){
                debugText += " playbackPaused:" + playbackPaused + " frame: " + frame;
            }
            // Draw the text at position (10, 10) with white color (0xFFFFFF)
            matrixStack.drawText(client.textRenderer, debugText, 10, 20, 0xFFFFFF, false);
        });
    }


    private static final List<PlayerKeyframe> recordedKeyframes = new ArrayList<>();

    public static void setRecording(boolean value)
    {
        recording = value;
        setFrame(GetRecFrame());
    }

    public static void setPlaybackEnabled(boolean value){
        if(playbackEnabled == value)
            return;

        playbackEnabled = value;

        if(!playbackEnabled)
            InputsManager.releaseAllKeys();
    }

    public static void setPlaybackPaused(boolean value){
        playbackPaused = value;
    }

    public static void setPlayerDetatched(boolean value){
        if(playerDetatched == value)
            return;
        playerDetatched = value;

        if(playerDetatched)
            InputsManager.releaseAllKeys();
    }

    public static void advanceFrames(int count){
        frame += count;
        if(frame >= getRecordedKeyframes().size())
            setFrame(getRecordedKeyframes().size());
        setPlayerDetatched(false);
    }

    public static void backupFrames(int amount){
        frame -= amount;
        if(frame <= 0)
            setFrame(0);
        setPlayerDetatched(false);
    }

    public static int getFrame(){ return frame; }

    public static void setFrame(int value){ frame = value; }

    public static List<PlayerKeyframe> getRecordedKeyframes() {
        return recordedKeyframes;
    }

    public static void clearRecordedKeyframes() {
        recordedKeyframes.clear();
    }

    public static void addCommandToKeyframe(String cmd, PlayerKeyframe keyframe){
        keyframe.cmds.add(cmd);

        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player != null)
            client.player.sendMessage(Text.literal("Added [" + cmd + "] to keyframe " + frame)); //TODO: This is just sending the command as a message. How can I make it so that it acts as if the player send the message in the text box so it actually executes the command as if it came from them?
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
        if(frame == 0){
            LOGGER.error("playheadIndex: " + frame + " tickDelta: " + tickDelta);
        }
        Vec3d lerpedPlayerPos = player.getLerpedPos(tickDelta);

        // Use the number of keyframes as the frame number (or use your own frame counter if available)
        long frameNumber = recordedKeyframes.size();

        Camera cam = client.gameRenderer.getCamera();
        Vec3d camPos = cam.getPos();
        Quaternionf camRot = cam.getRotation();

        camRot = new Quaternionf(camRot.x, camRot.y, camRot.z, camRot.w);

        LOGGER.info("cam rot recorded: " + camRot);

        // Create a merged keyframe with both keyboard and mouse inputs.
        PlayerKeyframe keyframe = new PlayerKeyframe(
                frameNumber,
                tickDelta,
                lerpedPlayerPos,
                player.getYaw(tickDelta),
                player.getPitch(tickDelta),
                camPos,
                camRot,
                new ArrayList<>(InputsManager.getRecordedInputsBuffer()),
                new ArrayList<>()
        );

        // Add the new keyframe to our in-memory list.
        recordedKeyframes.add(keyframe);

        setFrame(getRecordedKeyframes().size());

        InputsManager.clearRecordedInputsBuffer();
    }

    public static PlayerKeyframe getCurKeyframe(){
        List<PlayerKeyframe> frames = PlayerTimeline.getRecordedKeyframes();

        if(frame < 0 || frame >= frames.size())
            return null;

        return frames.get(frame);
    }

    public static void setPlayerFromKeyframe(PlayerKeyframe keyframe){

        if(playerDetatched)
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        Camera cam = client.gameRenderer.getCamera();

        if(player == null)
            return;

        if(frame >= PlayerTimeline.getRecordedKeyframes().size()){
            //setPlayingBack(false, 0);
            playbackPaused = true;

            if(VideoRenderer.isRendering()){
                VideoRenderer.FinishRendering();
                player.sendMessage(Text.literal("Rendering complete"), false);
            }

            return;
        }

        if(keyframe == null)
            return;

        player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
        player.setYaw(keyframe.playerYaw);
        player.setPitch(keyframe.playerPitch);

        Vector3f euler = new Vector3f();
        keyframe.camRot.getEulerAnglesYXZ(euler);

        manualSetCamera(cam, keyframe);
    }

    public static void manualSetCamera(Camera cam, PlayerKeyframe keyframe) {
        CameraAccessor accessor = (CameraAccessor) cam;

        accessor.setRawCamPos(keyframe.camPos);

        BlockPos.Mutable mutableBlockPos = accessor.myGetBlockPos();
        mutableBlockPos.set(
                (int) keyframe.camPos.x,
                (int) keyframe.camPos.y,
                (int) keyframe.camPos.z
        );

        // -- Clone the quaternion to avoid aliasing issues --
        Quaternionf newRot = new Quaternionf(keyframe.camRot);
        cam.getRotation().set(newRot);

        // -- Update the basis vectors --
        Vector3f horizontal = new Vector3f(0.0f, 0.0f, 1.0f).rotate(newRot);
        Vector3f vertical = new Vector3f(0.0f, 1.0f, 0.0f).rotate(newRot);
        Vector3f diagonal = new Vector3f(1.0f, 0.0f, 0.0f).rotate(newRot);

        cam.getHorizontalPlane().set(horizontal);
        cam.getVerticalPlane().set(vertical);
        cam.getDiagonalPlane().set(diagonal);

        // -- Convert the quaternion to Euler angles (Y-X-Z order) --
        Vector3f euler = new Vector3f();
        newRot.getEulerAnglesYXZ(euler);

        float yawDegrees = (float) Math.toDegrees(euler.y);
        float pitchDegrees = (float) Math.toDegrees(euler.x);

        accessor.setYaw(-yawDegrees);
        accessor.setPitch(pitchDegrees);
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
