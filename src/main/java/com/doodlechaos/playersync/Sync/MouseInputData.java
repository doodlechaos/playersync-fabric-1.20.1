package com.doodlechaos.playersync.Sync;

import java.util.List;

/**
 * Helper class to encapsulate mouse input data.
 */
public class MouseInputData {
    public final double mouseX;
    public final double mouseY;
    public final List<Integer> heldMouseButtons;
    public final double scrollX;
    public final double scrollY;

    public MouseInputData(double mouseX, double mouseY, List<Integer> heldMouseButtons, double scrollX, double scrollY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.heldMouseButtons = heldMouseButtons;
        this.scrollX = scrollX;
        this.scrollY = scrollY;
    }
}