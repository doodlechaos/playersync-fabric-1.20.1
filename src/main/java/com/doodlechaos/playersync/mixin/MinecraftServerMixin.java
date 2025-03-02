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

        //Never allow a server tick unless right after a client tick
        if ((PlayerSync.Recording || PlayerSync.PlayingBack) && !PlayerSync.TickServerFlag) {
            ci.cancel();
            return;
        }
    }
}
