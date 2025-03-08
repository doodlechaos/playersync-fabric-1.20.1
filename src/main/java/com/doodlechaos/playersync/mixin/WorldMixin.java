package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

@Mixin(World.class)
public class WorldMixin {

    //Remember: RETURN = Method will run normally
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity, CallbackInfo ci) {

        if(PlayerTimeline.isRecording())
            return;

        if(!PlayerTimeline.isPlaybackEnabled())
            return;

        if(entity instanceof PlayerEntity) //If it's the player entity never cancel
            return;

        if(PlayerTimeline.getPrevFrame() != PlayerTimeline.getFrame()) //Don't cancel if the frame changed
            return;

        ci.cancel();
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void tickBlockEntities(CallbackInfo ci) {

        if(PlayerTimeline.isRecording())
            return;

        if(!PlayerTimeline.isPlaybackEnabled())
            return;

        if(PlayerTimeline.getPrevFrame() != PlayerTimeline.getFrame()) //Don't cancel if the frame changed
            return;

        ci.cancel();
    }
}
