package com.doodlechaos.playersync.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface MouseAccessor {
    // This @Invoker annotation indicates that we're "exposing" a private method.
    @Invoker("onMouseButton")
    void callOnMouseButton(long window, int button, int action, int mods);

    @Invoker("onMouseScroll")
    void callOnMouseScroll(long window, double xoffset, double yoffset);

    @Invoker("onCursorPos")
    void callOnCursorPos(long window, double x, double y);
}