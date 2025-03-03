/*
package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.utils.PlayerSyncFolderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class PlayerRecorder {

    // Store merged keyframes in memory.
    private static final List<PlayerKeyframe> recordedKeyframes = new ArrayList<>();

    // Variables to accumulate scroll events.
    private static double scrollOffsetX = 0;
    private static double scrollOffsetY = 0;

    */
/**
     * Initializes the GLFW scroll callback to capture scroll events.
     * Call this once during initialization.
     *//*

    private static GLFWScrollCallback originalScrollCallback = null;

    public static void initMouseScrollCallback() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        // Capture the existing scroll callback (if any) and install our own.
        originalScrollCallback = GLFW.glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            // Record your scroll offsets.
            scrollOffsetX += xoffset;
            scrollOffsetY += yoffset;
            // Forward the event to the original callback if it exists.
            if (originalScrollCallback != null) {
                originalScrollCallback.invoke(window, xoffset, yoffset);
            }
        });

        LOGGER.info("Done initializing mouse scroll detection callback");
    }

    */
/**
     * Records a new keyframe that captures both player and mouse data.
     *//*

    public static void RecordKeyframe(){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player == null) {
            LOGGER.error("No player to record keyframe");
            return;
        }

        float partialTick = client.getTickDelta();
        Vec3d playerPos = client.player.getLerpedPos(partialTick);
        List<Integer> keyEvents = getHeldKeyboardKeyCodes();

        // Retrieve current mouse data.
        MouseInputData mouseInput = getMouseInput();

        // Create a merged keyframe with both keyboard and mouse inputs.
        PlayerKeyframe keyframe = new PlayerKeyframe(
                PlayerSync.GetRecFrame(),
                playerPos,
                client.player.getYaw(),
                client.player.getPitch(),
                keyEvents,
                mouseInput.mouseX,
                mouseInput.mouseY,
                mouseInput.heldMouseButtons,
                mouseInput.scrollX,
                mouseInput.scrollY
        );

        // Add the new keyframe to our in-memory list.
        recordedKeyframes.add(keyframe);
        LOGGER.info(keyframe.toString());
    }

    */
/**
     * Iterates over possible key codes and returns a list of currently held keyboard keys.
     *//*

    private static List<Integer> getHeldKeyboardKeyCodes() {
        long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        List<Integer> heldKeys = new ArrayList<>();
        for (int keyCode = 32; keyCode < GLFW.GLFW_KEY_LAST; keyCode++) {
            if (GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS) {
                heldKeys.add(keyCode);
            }
        }
        return heldKeys;
    }

    */
/**
     * Captures current mouse input, including position, held buttons, and scroll offsets.
     *
     * @return A MouseInput object containing the current mouse state.
     *//*

    public static MouseInputData getMouseInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        long windowHandle = client.getWindow().getHandle();
        double mouseX, mouseY;

        // Get mouse cursor position.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer xPos = stack.mallocDouble(1);
            DoubleBuffer yPos = stack.mallocDouble(1);
            GLFW.glfwGetCursorPos(windowHandle, xPos, yPos);
            mouseX = xPos.get(0);
            mouseY = yPos.get(0);
        }

        // Get held mouse buttons.
        List<Integer> heldButtons = new ArrayList<>();
        for (int button = GLFW.GLFW_MOUSE_BUTTON_1; button <= GLFW.GLFW_MOUSE_BUTTON_LAST; button++) {
            if (GLFW.glfwGetMouseButton(windowHandle, button) == GLFW.GLFW_PRESS) {
                heldButtons.add(button);
            }
        }

        // TODO: Capture current scroll offsets and reset them for the next frame.
        double currentScrollX = scrollOffsetX;
        double currentScrollY = scrollOffsetY;
        scrollOffsetX = 0;
        scrollOffsetY = 0;

        return new MouseInputData(mouseX, mouseY, heldButtons, currentScrollX, currentScrollY);
    }

    public static List<PlayerKeyframe> getRecordedKeyframes() {
        return recordedKeyframes;
    }

    public static void clearRecordedKeyframes() {
        recordedKeyframes.clear();
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
*/
