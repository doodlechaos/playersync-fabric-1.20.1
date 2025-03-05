package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.Sync.InputEventContainers.*;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

public class InputsManager {

    public static String mostRecentCommand;

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
        //recordedInputsBuffer.add(event);
    }

    // Called from your mixin to record a keyboard event.
    public static void recordKeyboardEvent(KeyboardEvent event) {
        LOGGER.info("recorded keyboard event on frame: " + PlayerTimeline.playheadFrame);
        recordedInputsBuffer.add(event);
    }

    public static List<InputEvent> getRecordedInputsBuffer(){
        return recordedInputsBuffer;
    }

    public static void clearRecordedInputsBuffer(){
        // Clear the recorded event lists so they don't accumulate events across frames.
        recordedInputsBuffer.clear();
    }

    public static void SimulateInputsFromKeyframe(PlayerKeyframe keyframe){

        if(keyframe == null)
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        long window = client.getWindow().getHandle();

        for (InputEvent ie : keyframe.recordedInputEvents) {
            ie.simulate(window, client);
        }
    }



}
