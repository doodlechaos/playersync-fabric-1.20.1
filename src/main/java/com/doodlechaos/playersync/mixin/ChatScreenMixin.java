package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.InputsManager;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    // Note: The method signature may differ based on your Minecraft version.
    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        if (chatText.startsWith("/")) {
            InputsManager.mostRecentCommand = chatText;
            LOGGER.info("Captured command from ChatScreen: " + chatText);
        }
        LOGGER.info("Detected on send message in chat screen");
    }
}
