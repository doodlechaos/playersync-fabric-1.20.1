package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.AudioSync;
import com.doodlechaos.playersync.Sync.InputsManager;
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

        if(PlayerSync.TickServerFlag && !MinecraftClient.getInstance().isPaused()){ //Server doesn't tick when esc pause menu is up. Must continue or game will freeze.
            ci.cancel();
            return;
        }

        PlayerTimeline.update();
    }
    @Inject(method = "render", at = @At("TAIL"))
    private void onClientRenderFinish(CallbackInfo ci) {

        if(PlayerTimeline.isRecording()){
            PlayerTimeline.CreateKeyframe();
        }

        if(VideoRenderer.isRendering()){
            VideoRenderer.CaptureFrame();
        }

        PlayerTimeline.updatePrevFrame();

        if(PlayerTimeline.isRecording() ||
                (PlayerTimeline.isPlaybackEnabled() && !PlayerTimeline.isPlaybackPaused()))
            PlayerTimeline.advanceFrames(1);
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

        InputsManager.checkPlaybackKeyboardControls();

        float playheadTime = PlayerTimeline.getFrame() / 60.0f;
        AudioSync.updateAudio(playheadTime);
    }

    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onClientTick(CallbackInfo ci) {

        if(PlayerTimeline.isRecording()){
            PlayerSync.LOGGER.info("Ticking client on frame: " + PlayerTimeline.getFrame());
            PlayerSync.TickServerFlag = true;
            return;
        }

        //Only tick server if we're recording or playing back, AND the timeline frame has changed
        if (PlayerTimeline.isPlaybackEnabled() && PlayerTimeline.getPrevFrame() != PlayerTimeline.getFrame()) {
            PlayerSync.LOGGER.info("Ticking client on frame: " + PlayerTimeline.getFrame());
            PlayerSync.TickServerFlag = true;
        }
    }

    //Tick is called from the render loop when necessary
    @Inject(method = "tick", at = @At("TAIL"), cancellable = true)
    private void onEndClientTick(CallbackInfo ci) {

        //Manually tick the server and wait for its completion here?

    }

    @Inject(method = "getTickDelta", at = @At("HEAD"), cancellable = true)
    private void overrideGetTickDelta(CallbackInfoReturnable<Float> cir) {
        if (PlayerTimeline.isRecording() || PlayerTimeline.isPlaybackEnabled()) {
            int playheadIndex = PlayerTimeline.getFrame();
            // Calculate the fractional tick: each tick equals 3 frames (60fps / 20tps)
            float tickDelta = (playheadIndex % 3) / 3.0f;
            cir.setReturnValue(tickDelta);
        }
    }

}
