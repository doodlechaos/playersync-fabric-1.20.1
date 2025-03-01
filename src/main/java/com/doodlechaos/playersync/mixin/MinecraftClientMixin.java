package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onClientRender(CallbackInfo ci) {

        if(PlayerSync.FramesToProcess <= 0 && PlayerSync.TickClient <= 0 && PlayerSync.TickServer <= 0)
        {
            PlayerSync.ManualTime = false;
            return;
        }

        MinecraftServer server = MinecraftClient.getInstance().getServer();
        PlayerSync.stepSyncFrame(server);

    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {
        if (PlayerSync.ManualTime) {
            PlayerSync.AllowServerTickOnce = true;
        }
    }


}
