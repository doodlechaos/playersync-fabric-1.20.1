package com.doodlechaos.playersync.Sync.InputEventContainers;

import com.doodlechaos.playersync.mixin.accessor.MouseAccessor;
import net.minecraft.client.MinecraftClient;

public class MouseScrollEvent extends InputEvent {
    public final double horizontal;
    public final double vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public String toLine() {
        return "MouseScrollEvent;horizontal=" + horizontal + ";vertical=" + vertical;
    }

    /**
     * Deserializes a MouseScrollEvent from its string representation.
     * Expected format: "MouseScrollEvent;horizontal=0.0;vertical=-1.0"
     */
    public static MouseScrollEvent fromLine(String line) {
        String[] parts = line.split(";");
        double horizontal = 0, vertical = 0;
        for (String part : parts) {
            if (part.startsWith("horizontal=")) {
                horizontal = Double.parseDouble(part.substring("horizontal=".length()));
            } else if (part.startsWith("vertical=")) {
                vertical = Double.parseDouble(part.substring("vertical=".length()));
            }
        }
        return new MouseScrollEvent(horizontal, vertical);
    }

    @Override
    public void simulate(long window, MinecraftClient client) {
        ((MouseAccessor) client.mouse).callOnMouseScroll(window, horizontal, vertical);
    }
}
