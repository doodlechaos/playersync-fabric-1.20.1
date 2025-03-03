package com.doodlechaos.playersync.Sync.InputEventContainers;

import com.doodlechaos.playersync.mixin.MouseAccessor;
import net.minecraft.client.MinecraftClient;

public class MousePosEvent extends InputEvent {
    public final double x;
    public final double y;

    public MousePosEvent(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toLine() {
        return "MousePosEvent;x=" + x + ";y=" + y;
    }

    /**
     * Deserializes a MousePosEvent from its string representation.
     * Expected format: "MousePosEvent;x=100.5;y=200.5"
     */
    public static MousePosEvent fromLine(String line) {
        String[] parts = line.split(";");
        double x = 0, y = 0;
        for (String part : parts) {
            if (part.startsWith("x=")) {
                x = Double.parseDouble(part.substring("x=".length()));
            } else if (part.startsWith("y=")) {
                y = Double.parseDouble(part.substring("y=".length()));
            }
        }
        return new MousePosEvent(x, y);
    }

    @Override
    public void simulate(long window, MinecraftClient client) {
        ((MouseAccessor) client.mouse).callOnCursorPos(window, x, y);
    }
}
