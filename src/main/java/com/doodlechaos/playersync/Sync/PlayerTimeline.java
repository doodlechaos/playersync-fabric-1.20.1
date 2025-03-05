package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.VideoRenderer;
import com.doodlechaos.playersync.mixin.accessor.CameraAccessor;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerTimeline {

    public static boolean startRecNextTick = false;
    private static boolean recording = false;

    public static boolean startPlaybackNextTick = false;
    private static boolean inPlaybackMode = false;
    public static boolean playbackPaused = false;

    public static int GetRecFrame(){
        return getRecordedKeyframes().size();
    };

    public static int playheadFrame = 0;

    public static boolean isRecording(){return recording;}
    public static boolean isInPlaybackMode(){return inPlaybackMode;}

    private static boolean wasSpaceKeyDown = false;
    private static boolean wasPKeyDown = false;
    private static boolean wasPeriodKeyDown = false;
    private static boolean wasCommaKeyDown = false;

    public static void registerDebugText(){
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            String debugText = "inPlaybackMode:" + inPlaybackMode;
            if(isRecording()){
                debugText += "recFrame: " + GetRecFrame();
            }
            if(isInPlaybackMode()){
                debugText += " playbackPaused:" + playbackPaused + " frame: " + playheadFrame;
            }
            // Draw the text at position (10, 10) with white color (0xFFFFFF)
            matrixStack.drawText(client.textRenderer, debugText, 10, 20, 0xFFFFFF, false);
        });
    }

    private static final List<PlayerKeyframe> recordedKeyframes = new ArrayList<>();

    public static void setRecording(boolean value)
    {
        recording = value;
        playheadFrame = GetRecFrame();
    //    onStateUpdate();
    }

    public static void setPlayingBack(boolean value){
        inPlaybackMode = value;
  //      onStateUpdate();
    }

//    private static void onStateUpdate(){
//        AudioSync.setPlaying(inPlaybackMode || recording);
//    }


    public static void checkPlaybackKeyboardControls() {


        long window = MinecraftClient.getInstance().getWindow().getHandle();

        // Toggle playback mode (P key)
        boolean isPKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_P);
        if (isPKeyDown && !wasPKeyDown) {
            setPlayingBack(!inPlaybackMode);
            LOGGER.info("Detected toggle playback mode key press");
        }
        wasPKeyDown = isPKeyDown;

        if (!isInPlaybackMode())
            return;

        if(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT))
            advanceFrames(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 1);

        if(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT))
            backupFrames(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 1);

        if(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_DOWN))
            PlayerTimeline.playheadFrame = 0;

        if(InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_UP)){
            int recordedFrames = PlayerTimeline.getRecordedKeyframes().size();
            if(recordedFrames > 0)
                PlayerTimeline.playheadFrame = recordedFrames - 1;
        }

        // Toggle playback paused (Space key)
        boolean isSpaceKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_SPACE);
        if (isSpaceKeyDown && !wasSpaceKeyDown) {
            playbackPaused = !playbackPaused;
            LOGGER.info("Detected toggle playback paused key press");
        }
        wasSpaceKeyDown = isSpaceKeyDown;

        // Advance frame (Period key)
        boolean isPeriodKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_PERIOD);
        if (isPeriodKeyDown && !wasPeriodKeyDown) {
            advanceFrames(1);
            LOGGER.info("Detected advance frame key press");
        }
        wasPeriodKeyDown = isPeriodKeyDown;

        // Backup frame (Comma key)
        boolean isCommaKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_COMMA);
        if (isCommaKeyDown && !wasCommaKeyDown) {
            backupFrames(1);
            LOGGER.info("Detected backup frame key press");
        }
        wasCommaKeyDown = isCommaKeyDown;
    }

    public static void advanceFrames(int count){
        playheadFrame += count;
        if(playheadFrame >= getRecordedKeyframes().size())
            playheadFrame = getRecordedKeyframes().size();
    }

    public static void backupFrames(int amount){
        playheadFrame -= amount;
        if(playheadFrame <= 0)
            playheadFrame = 0;
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
        if(playheadFrame == 0){
            LOGGER.error("playheadIndex: " + playheadFrame + " tickDelta: " + tickDelta);
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
                new ArrayList<>(InputsManager.getRecordedInputsBuffer())
        );

        // Add the new keyframe to our in-memory list.
        recordedKeyframes.add(keyframe);

        playheadFrame = getRecordedKeyframes().size();

        InputsManager.clearRecordedInputsBuffer();
    }

    public static PlayerKeyframe getCurKeyframe(){
        List<PlayerKeyframe> frames = PlayerTimeline.getRecordedKeyframes();

        if(playheadFrame < 0 || playheadFrame >= frames.size())
            return null;

        return frames.get(playheadFrame);
    }

    public static void setPlayerFromKeyframe(PlayerKeyframe keyframe){

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        Camera cam = client.gameRenderer.getCamera();

        if(player == null)
            return;

        if(playheadFrame >= PlayerTimeline.getRecordedKeyframes().size()){
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
