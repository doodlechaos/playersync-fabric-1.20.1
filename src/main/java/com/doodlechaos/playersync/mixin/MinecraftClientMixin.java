package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.AudioSync;
import com.doodlechaos.playersync.Sync.InputsManager;
import com.doodlechaos.playersync.Sync.PlayerKeyframe;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.VideoRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    //When recording or playing back, render is called once every video frame
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onClientRenderStart(CallbackInfo ci) {

        My60FPSUpdate();

        //If I use setblock to place a minecraft block between minecraft 20tps ticks, will it wait until the next tick to be placed?

        if(PlayerSync.TickServerFlag){
            ci.cancel();
            return;
        }

        if(PlayerTimeline.isInPlaybackMode()){
            PlayerKeyframe keyframe = PlayerTimeline.getCurKeyframe();

            if(!PlayerTimeline.playbackPaused)
                InputsManager.SimulateInputsFromKeyframe(keyframe);

            PlayerTimeline.setPlayerFromKeyframe(keyframe);

            if(!PlayerTimeline.playbackPaused)
                PlayerTimeline.playheadFrame++;
        }
    }

    @Unique
    private static long lastCheckTime = 0L;
    @Unique
    private static final long CHECK_INTERVAL = 16_666_667L;    // ~16.6 ms in nanoseconds:

    @Unique
    private void My60FPSUpdate(){
        long now = System.nanoTime();
        if (now - lastCheckTime < CHECK_INTERVAL) {
            return;
        }
        lastCheckTime = now;

        PlayerTimeline.checkPlaybackKeyboardControls();

        float playheadTime = PlayerTimeline.playheadFrame / 60.0f;
        AudioSync.updateAudio(playheadTime);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onClientRenderFinish(CallbackInfo ci) {

        if(PlayerTimeline.isRecording()){
            PlayerTimeline.CreateKeyframe();
        }

        if(VideoRenderer.isRendering()){
            VideoRenderer.CaptureFrame();
        }
    }


    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {


        if (PlayerTimeline.isRecording() || PlayerTimeline.isInPlaybackMode()) {
            PlayerSync.TickServerFlag = true;
        }
    }

    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void onEndClientTick(CallbackInfo ci) {

    }

    /**
     * Overrides getTickDelta to return a custom value based on the 60 FPS playheadIndex,
     * so that 3 rendered frames correspond to one Minecraft tick.
     */
    @Inject(method = "getTickDelta", at = @At("HEAD"), cancellable = true)
    private void overrideGetTickDelta(CallbackInfoReturnable<Float> cir) {
        if (PlayerTimeline.isRecording() || PlayerTimeline.isInPlaybackMode()) {
            int playheadIndex = PlayerTimeline.playheadFrame;
            // Calculate the fractional tick: each tick equals 3 frames (60fps / 20tps)
            float tickDelta = (playheadIndex % 3) / 3.0f;
            cir.setReturnValue(tickDelta);
        }
    }

}
