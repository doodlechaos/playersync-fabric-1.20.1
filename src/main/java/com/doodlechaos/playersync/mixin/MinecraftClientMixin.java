package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerKeyframe;
import com.doodlechaos.playersync.Sync.PlayerRecorder;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    //When recording or playing back, render is called once every video frame
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onClientRenderStart(CallbackInfo ci) {

        if(PlayerSync.TickServerFlag){
            ci.cancel();
            return;
        }

/*        if(PlayerSync.PlayingBack){
        }*/

        if(PlayerSync.PlayingBack){
            PlayerSync.SimulateKeystrokes();
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    private void onClientRenderFinish(CallbackInfo ci) {

        if(PlayerSync.Recording){
            PlayerRecorder.RecordKeyframe();
        }
        if(PlayerSync.PlayingBack){
            PlayerSync.setPlayerFromKeyframe();
            PlayerSync.playbackIndex++;
        }
/*        if(PlayerSync.PlayingBack){
            PlayerSync.setPlayerFromKeyframe();
            PlayerSync.playbackIndex++;
        }*/

    }


    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {
        if (PlayerSync.Recording || PlayerSync.PlayingBack) {
            PlayerSync.TickServerFlag = true;
        }
    }


}
