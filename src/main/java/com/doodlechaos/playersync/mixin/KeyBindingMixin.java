package com.doodlechaos.playersync.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

/*    @Inject(method = "setKeyPressed", at = @At("HEAD"))
    private static void setKeyPressedMixin(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        // Record a key press or release event depending on the 'pressed' parameter
        InputRecorder.recordKeyboardEventToBuffer(key.getCode(), pressed, PlayerSync.RecordingFrame));
    }*/

}
