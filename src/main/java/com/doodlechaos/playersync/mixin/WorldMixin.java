package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Consumer;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(World.class)
public class WorldMixin {

    @Shadow public boolean isClient;

    //Remember: RETURN = Method will run normally
    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void tickEntity(Consumer<T> tickConsumer, T entity, CallbackInfo ci) {

        if(!PlayerTimeline.isLockstepMode())
            return;

        if(entity instanceof PlayerEntity) //If it's the player entity never cancel
            return;

        if(PlayerTimeline.hasFrameChanged() && PlayerTimeline.isTickFrame()) //Don't cancel if the frame changed
            return;

        ci.cancel();
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void tickBlockEntities(CallbackInfo ci) {

        if(!PlayerTimeline.isLockstepMode())
            return;

        if(PlayerTimeline.hasFrameChanged()) //Don't cancel if the frame changed
            return;

        ci.cancel();
    }
}
