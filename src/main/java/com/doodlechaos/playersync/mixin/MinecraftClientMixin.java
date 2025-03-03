package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerRecorderV2;
import com.doodlechaos.playersync.VideoRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    //When recording or playing back, render is called once every video frame
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onClientRenderStart(CallbackInfo ci) {

        if(PlayerSync.TickServerFlag){
            ci.cancel();
            return;
        }

        if(PlayerSync.PlayingBack){
            PlayerSync.SimulateInputsFromKeyframe();
/*            PlayerSync.SimulateKeystrokes();
            PlayerSync.SimulateMouse();*/
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    private void onClientRenderFinish(CallbackInfo ci) {

        if(PlayerSync.Recording){
            PlayerRecorderV2.RecordKeyframe();
        }
        if(PlayerSync.PlayingBack){
            PlayerSync.setPlayerFromKeyframe();
            PlayerSync.playbackIndex++;
        }
        if(VideoRenderer.isRendering()){
            VideoRenderer.CaptureFrame();
        }
    }


    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {
        if (PlayerSync.Recording || PlayerSync.PlayingBack) {
            PlayerSync.TickServerFlag = true;
        }
    }


}
