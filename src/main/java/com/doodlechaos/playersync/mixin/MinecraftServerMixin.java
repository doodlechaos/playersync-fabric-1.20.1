package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;


@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci){

        if(!PlayerTimeline.isRecording() && !PlayerTimeline.isPlaybackEnabled())
            return;

        if (!PlayerSync.TickServerFlag) {
            ci.cancel();
            return;
        }
    }
}
