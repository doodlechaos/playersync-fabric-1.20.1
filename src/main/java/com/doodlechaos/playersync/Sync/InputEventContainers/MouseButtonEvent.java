package com.doodlechaos.playersync.Sync.InputEventContainers;

// Simple data classes to hold the event data.
public class MouseButtonEvent {
    public final int button;
    public final int action;
    public final int mods;

    public MouseButtonEvent(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }

    @Override
    public String toString() {
        return "MouseButtonEvent{" +
                "button=" + button +
                ", action=" + action +
                ", mods=" + mods +
                '}';
    }
}