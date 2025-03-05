package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.VideoRenderer;
import com.doodlechaos.playersync.mixin.accessor.CameraAccessor;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

    private static boolean recording = false;

    public static boolean inPlaybackMode = false;
    public static boolean playbackPaused = false;

    public static int GetRecFrame(){
        return getRecordedKeyframes().size();
    };

    public static int playheadFrame = 0;

    public static boolean isRecording(){return recording;}
    public static boolean isInPlaybackMode(){return inPlaybackMode;}

    private static boolean wasRKeyDown = false;
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
    }

    public static void checkPlaybackKeyboardControls() {

        long window = MinecraftClient.getInstance().getWindow().getHandle();

        // Toggle playback mode (P key)
        boolean isPKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_P);
        if (isPKeyDown && !wasPKeyDown) {
            inPlaybackMode = !inPlaybackMode;
            LOGGER.info("Detected toggle playback mode key press");
        }
        wasPKeyDown = isPKeyDown;

        if (!isInPlaybackMode())
            return;

        // Toggle playback paused (R key)
        boolean isRKeyDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_R);
        if (isRKeyDown && !wasRKeyDown) {
            PlayerKeyframe keyframe = getCurKeyframe();

            if(keyframe != null)
                addCommandToKeyframe(InputsManager.mostRecentCommand, keyframe);


        }
        wasRKeyDown = isRKeyDown;

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

    private static void addCommandToKeyframe(String cmd, PlayerKeyframe keyframe){
        keyframe.cmds.add(cmd);

        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player != null)
            client.player.sendMessage(Text.literal("Added [" + cmd + "] to keyframe " + playheadFrame)); //TODO: This is just sending the command as a message. How can I make it so that it acts as if the player send the message in the text box so it actually executes the command as if it came from them?
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
                new ArrayList<>(InputsManager.getRecordedInputsBuffer()),
                new ArrayList<>()
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

        //Execute the commands stored in the keyframe
        for(String cmd : keyframe.cmds){
            if (cmd == null || cmd.isEmpty()) {
                continue; // Skip null or empty commands
            }
            ExecuteCommandAsPlayer(cmd);
        }
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
    public static void ExecuteCommandAsPlayer(String command) {
        IntegratedServer minecraftServer = MinecraftClient.getInstance().getServer();
        if (minecraftServer == null) {
            LOGGER.error("Minecraft server is not available.");
            return;
        }

        ServerPlayerEntity player = PlayerSync.GetServerPlayer();
        if (player == null) {
            LOGGER.error("No players are currently online.");
            return;
        }

        // Ensure the command does not start with a slash
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        // Capture the modified command in a final variable for use in the lambda
        final String commandToExecute = command;

        minecraftServer.execute(() -> {
            try {
                CommandDispatcher<ServerCommandSource> dispatcher = minecraftServer.getCommandManager().getDispatcher();
                var parsedCommand = dispatcher.parse(commandToExecute, player.getCommandSource());
                dispatcher.execute(parsedCommand);
                LOGGER.info("Command executed successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to execute command [" + commandToExecute + "]: " + e.toString());
            }
        });
    }


}
