package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.InputEventContainers.MouseButtonEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MousePosEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MouseScrollEvent;
import com.doodlechaos.playersync.Sync.PlayerRecorderV2;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButtonInject(long window, int button, int action, int mods, CallbackInfo ci) {
        // Record the button event before the normal processing.
        PlayerRecorderV2.recordMouseButtonEvent(new MouseButtonEvent(button, action, mods));
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollInject(long window, double horizontal, double vertical, CallbackInfo ci) {
        // Record the scroll event.
        PlayerRecorderV2.recordMouseScrollEvent(new MouseScrollEvent(horizontal, vertical));
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPosInject(long window, double x, double y, CallbackInfo ci) {
        // Record the cursor position event.
        PlayerRecorderV2.recordMousePosEvent(new MousePosEvent(x, y));
    }

}
