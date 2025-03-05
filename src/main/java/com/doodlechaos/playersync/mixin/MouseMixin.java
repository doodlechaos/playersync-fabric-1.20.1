package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.InputEventContainers.MouseButtonEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MousePosEvent;
import com.doodlechaos.playersync.Sync.InputEventContainers.MouseScrollEvent;
import com.doodlechaos.playersync.Sync.InputsManager;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButtonInject(long window, int button, int action, int mods, CallbackInfo ci) {
        if(!PlayerTimeline.isRecording())
            return;

        // Record the button event before the normal processing.
        InputsManager.recordMouseButtonEvent(new MouseButtonEvent(button, action, mods));
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScrollInject(long window, double horizontal, double vertical, CallbackInfo ci) {
        if(!PlayerTimeline.isRecording())
            return;

        // Record the scroll event.
        InputsManager.recordMouseScrollEvent(new MouseScrollEvent(horizontal, vertical));
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void onCursorPosInject(long window, double x, double y, CallbackInfo ci) {
        if(!PlayerTimeline.isRecording())
            return;

        // Record the cursor position event.
        InputsManager.recordMousePosEvent(new MousePosEvent(x, y));
    }

}
