package com.doodlechaos.playersync.Sync.InputEventContainers;

import net.minecraft.client.MinecraftClient;
import com.doodlechaos.playersync.mixin.accessor.MouseAccessor;

public class MouseButtonEvent extends InputEvent {
    public final int button;
    public final int action;
    public final int mods;

    public MouseButtonEvent(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }

    @Override
    public String toLine() {
        return "MouseButtonEvent;button=" + button + ";action=" + action + ";mods=" + mods;
    }

    /**
     * Deserializes a MouseButtonEvent from its string representation.
     * Expected format: "MouseButtonEvent;button=0;action=1;mods=0"
     */
    public static MouseButtonEvent fromLine(String line) {
        String[] parts = line.split(";");
        int button = 0, action = 0, mods = 0;
        for (String part : parts) {
            if (part.startsWith("button=")) {
                button = Integer.parseInt(part.substring("button=".length()));
            } else if (part.startsWith("action=")) {
                action = Integer.parseInt(part.substring("action=".length()));
            } else if (part.startsWith("mods=")) {
                mods = Integer.parseInt(part.substring("mods=".length()));
            }
        }
        return new MouseButtonEvent(button, action, mods);
    }

    @Override
    public void simulate(long window, MinecraftClient client) {
        ((MouseAccessor) client.mouse).callOnMouseButton(window, button, action, mods);
    }
}
