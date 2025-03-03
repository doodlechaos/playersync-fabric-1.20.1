package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerRecorderV2;
import com.doodlechaos.playersync.Sync.InputEventContainers.KeyboardEvent;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void recordKeyboardInput(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {

        KeyboardEvent event = new KeyboardEvent(key, scancode, action, modifiers);
        PlayerRecorderV2.recordKeyboardEvent(event);
    }
}
