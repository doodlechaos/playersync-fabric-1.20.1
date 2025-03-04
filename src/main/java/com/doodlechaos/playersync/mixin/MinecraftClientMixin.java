package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.AudioSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.VideoRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    //When recording or playing back, render is called once every video frame
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onClientRenderStart(CallbackInfo ci) {

        if(PlayerSync.TickServerFlag){
            ci.cancel();
            return;
        }

        if(PlayerTimeline.isPlayingBack() || PlayerTimeline.isRecording()){
            AudioSync.syncAudio();
        }

        if(PlayerTimeline.isPlayingBack()){
            PlayerTimeline.SimulateInputsFromKeyframe();
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    private void onClientRenderFinish(CallbackInfo ci) {

        if(PlayerTimeline.isRecording()){
            PlayerTimeline.CreateKeyframe();
        }
        if(PlayerTimeline.isPlayingBack()){
            PlayerTimeline.setPlayerFromKeyframe();
            PlayerTimeline.playheadIndex++;
        }
        if(VideoRenderer.isRendering()){
            VideoRenderer.CaptureFrame();
        }
    }


    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {

        if (PlayerTimeline.isRecording() || PlayerTimeline.isPlayingBack()) {
            PlayerSync.TickServerFlag = true;
        }
    }

    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void onEndClientTick(CallbackInfo ci) {
        if(PlayerTimeline.startRecNextTick){
            PlayerTimeline.setRecording(true);
            PlayerTimeline.startRecNextTick = false;
        }
        if(PlayerTimeline.startPlaybackNextTick){
            PlayerTimeline.setPlayingBack(!PlayerTimeline.isPlayingBack(), 0);
            PlayerTimeline.startPlaybackNextTick = false;
        }

    }

    /**
     * Overrides getTickDelta to return a custom value based on the 60 FPS playheadIndex,
     * so that 3 rendered frames correspond to one Minecraft tick.
     */
    @Inject(method = "getTickDelta", at = @At("HEAD"), cancellable = true)
    private void overrideGetTickDelta(CallbackInfoReturnable<Float> cir) {
        if (PlayerTimeline.isRecording() || PlayerTimeline.isPlayingBack()) {
            int playheadIndex = PlayerTimeline.playheadIndex;
            // Calculate the fractional tick: each tick equals 3 frames (60fps / 20tps)
            float tickDelta = (playheadIndex % 3) / 3.0f;
            cir.setReturnValue(tickDelta);
        }
    }


}
