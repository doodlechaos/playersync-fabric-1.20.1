package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {

    @Shadow public float tickDelta;
    @Shadow public float lastFrameDuration;
    @Shadow @Final private float tickTime;

    @Unique
    private int counter = 0;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    private void overrideRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {

        if(PlayerTimeline.isRecording() || (PlayerTimeline.isPlaybackEnabled())){

            float constantFrameDuration = (float) (1000.0 / 60.0) / tickTime;
            this.lastFrameDuration = constantFrameDuration;

            this.tickDelta = (PlayerTimeline.getFrame() % 3) / 3.0f;;

            int ticksToAdvance = 0;
            if(tickDelta == 0 || tickDelta == 1) //Only tick when we're recording or playing back
                ticksToAdvance = 1;

            if(PlayerTimeline.isPlaybackPaused() && !PlayerTimeline.isRecording()){
                //Tick every 3rd frame manually
                counter++;
                ticksToAdvance = 0;
                if(counter >= 3){
                    ticksToAdvance = 1;
                    counter = 0;
                }
            }

            cir.setReturnValue(ticksToAdvance);
        }
    }
}
