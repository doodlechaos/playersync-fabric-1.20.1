package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci){
        // If we are in manual-time mode and no manual-tick has been requested,
        // cancel the server’s normal tick.
        if (PlayerSync.ManualTime && !PlayerSync.AllowServerTickOnce) {
            ci.cancel();
            return;
        }

        // If we get here and manual mode is true, it means PlayerSync.AllowServerTickOnce
        // was set (manual tick requested). We let the tick happen, but clear the flag
        // so it doesn’t keep ticking every frame.
        PlayerSync.AllowServerTickOnce = false;
    }
}
