package com.doodlechaos.playersync.Sync.InputEventContainers;

public class MouseScrollEvent {
    public final double horizontal;
    public final double vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public String toString() {
        return "MouseScrollEvent{" +
                "horizontal=" + horizontal +
                ", vertical=" + vertical +
                '}';
    }
}