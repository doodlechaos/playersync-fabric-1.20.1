package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {

    @ModifyVariable(method = "beginRenderTick", at = @At("HEAD"), argsOnly = true)
    private long overrideTimeMillis(long timeMillis) {
        if(PlayerSync.ManualTime){
            PlayerSync.MasterTime += 1;
            return PlayerSync.MasterTime;
        }
        PlayerSync.MasterTime = timeMillis;
        return timeMillis;
    }

}
