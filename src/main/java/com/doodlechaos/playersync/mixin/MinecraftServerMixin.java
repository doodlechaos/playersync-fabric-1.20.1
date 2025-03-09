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

        //Cancel the tick if we're in lockstep mode and not waiting for the server
        if (PlayerTimeline.isLockstepMode() && !PlayerSync.serverTickRequest) {
            //LOGGER.info("Blocking server tick");
            ci.cancel();
            return;
        }
        PlayerSync.serverTickRequest = false;
        //Allow the tick if we are not in lockstep mode, OR if we're not in lockstep mode and waiting for the server
        //LOGGER.info("ticking _SERVER on frame: " + PlayerTimeline.getFrame());
    }
}
