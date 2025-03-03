package com.doodlechaos.playersync.Sync.InputEventContainers;

public class KeyboardEvent {
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
    public String toString() {
        return "KeyboardEvent{" +
                "key=" + key +
                ", scancode=" + scancode +
                ", action=" + action +
                ", modifiers=" + modifiers +
                '}';
    }
}
