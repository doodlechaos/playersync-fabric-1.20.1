package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {

    @Shadow public float tickDelta;
    @Shadow public float lastFrameDuration;
    @Shadow private long prevTimeMillis;
    @Shadow @Final private float tickTime;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    private void overrideRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if (PlayerTimeline.isRecording() || PlayerTimeline.isPlayingBack()) {
            // Calculate a constant frame duration of exactly 1/60th second in milliseconds divided by tickTime.
            float constantFrameDuration = (float) (1000.0 / 60.0) / tickTime;

            // Set the last frame duration to our constant value.
            this.lastFrameDuration = constantFrameDuration;

            if(PlayerTimeline.playheadIndex % 20 == 0) //Reset the tick delta so it is deterministic
                this.tickDelta = 0;

            // Update tickDelta accordingly.
            this.tickDelta += constantFrameDuration;
            int ticksToAdvance = (int)this.tickDelta;
            this.tickDelta -= ticksToAdvance;

            // Update the previous time to the current time so that the normal delta calculation is skipped.
            this.prevTimeMillis = timeMillis;

            // Return our computed tick advance, cancelling the rest of the method.
            cir.setReturnValue(ticksToAdvance);
        }
    }

}
