package com.doodlechaos.playersync.Sync.InputEventContainers;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;

public class KeyboardEvent extends InputEvent {
    public final int key;
    public final int scancode;
    public final int action;
    public final int modifiers;

    public KeyboardEvent(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }

    @Override
    public String toLine() {
        return "KeyboardEvent;key=" + key + ";scancode=" + scancode + ";action=" + action + ";modifiers=" + modifiers;
    }

    /**
     * Deserializes a KeyboardEvent from its string representation.
     * Expected format: "KeyboardEvent;key=65;scancode=30;action=1;modifiers=0"
     */
    public static KeyboardEvent fromLine(String line) {
        String[] parts = line.split(";");
        int key = 0, scancode = 0, action = 0, modifiers = 0;
        for (String part : parts) {
            if (part.startsWith("key=")) {
                key = Integer.parseInt(part.substring("key=".length()));
            } else if (part.startsWith("scancode=")) {
                scancode = Integer.parseInt(part.substring("scancode=".length()));
            } else if (part.startsWith("action=")) {
                action = Integer.parseInt(part.substring("action=".length()));
            } else if (part.startsWith("modifiers=")) {
                modifiers = Integer.parseInt(part.substring("modifiers=".length()));
            }
        }
        return new KeyboardEvent(key, scancode, action, modifiers);
    }

    @Override
    public void simulate(long window, net.minecraft.client.MinecraftClient client) {
        client.keyboard.onKey(window, key, scancode, action, modifiers);
        PlayerSync.LOGGER.info("Simulated keyboard event on frame: " + PlayerTimeline.playheadFrame + " key " + key + ", action " + action);
    }
}
