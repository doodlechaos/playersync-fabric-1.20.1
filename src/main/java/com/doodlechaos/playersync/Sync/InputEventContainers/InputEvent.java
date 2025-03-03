package com.doodlechaos.playersync.Sync.InputEventContainers;

import net.minecraft.client.MinecraftClient;

public abstract class InputEvent {

    public abstract String toLine();

    // Each event will know how to simulate itself.
    public abstract void simulate(long window, MinecraftClient client);

    /**
     * Factory method to deserialize an input event from a single-line string.
     * Delegates to the correct subclass based on the type prefix.
     *
     * @param line The serialized input event.
     * @return The deserialized InputEvent.
     */
    public static InputEvent fromLine(String line) {
        if (line.startsWith("KeyboardEvent")) {
            return KeyboardEvent.fromLine(line);
        } else if (line.startsWith("MouseButtonEvent")) {
            return MouseButtonEvent.fromLine(line);
        } else if (line.startsWith("MousePosEvent")) {
            return MousePosEvent.fromLine(line);
        } else if (line.startsWith("MouseScrollEvent")) {
            return MouseScrollEvent.fromLine(line);
        } else {
            throw new IllegalArgumentException("Unknown event type: " + line);
        }
    }
}
