package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.InputsManager;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.Sync.InputEventContainers.KeyboardEvent;
import com.doodlechaos.playersync.mixin.accessor.KeyboardAccessor;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(Keyboard.class)
public class KeyboardMixin {

//    @Inject(method = "onKey", at = @At("HEAD"))
//    public void recordKeyboardInput(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
//
//        if(!PlayerTimeline.isRecording())
//            return;
//
//        KeyboardEvent event = new KeyboardEvent(key, scancode, action, modifiers);
//        InputsManager.recordKeyboardEvent(event);
//    }

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void onSetup(long window, CallbackInfo ci) {
        Keyboard self = (Keyboard)(Object)this;

        MinecraftClient client = MinecraftClient.getInstance();
        // Wrap the key callback.
        GLFWKeyCallbackI keyCallback = (windowx, key, scancode, action, modifiers) -> {
            // Record the raw GLFW key event.
            if (PlayerTimeline.isRecording()) {
                KeyboardEvent event = new KeyboardEvent(key, scancode, action, modifiers);
                InputsManager.recordKeyboardEvent(event);
            }
            // Delegate to Minecraft’s processing via onKey.
            client.execute(() -> self.onKey(windowx, key, scancode, action, modifiers));
        };

        // Wrap the char callback using GLFWCharModsCallbackI.
        GLFWCharModsCallbackI charCallback = (windowx, codePoint, mods) -> {
            // Delegate to onChar via our accessor.
            ((KeyboardAccessor)self).callOnChar(windowx, codePoint, mods);
        };

        // Set your wrapped callbacks.
        InputUtil.setKeyboardCallbacks(window, keyCallback, charCallback);
        // Cancel the original setup to prevent the original lambdas from being set.
        ci.cancel();

        LOGGER.info("Done setting up keyboard input recording callbacks");
    }

}
