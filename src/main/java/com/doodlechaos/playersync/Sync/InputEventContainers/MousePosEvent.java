package com.doodlechaos.playersync.Sync.InputEventContainers;

public class MousePosEvent {
    public final double x;
    public final double y;

    public MousePosEvent(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "MousePosEvent{" +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}