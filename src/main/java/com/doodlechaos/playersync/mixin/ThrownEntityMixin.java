package com.doodlechaos.playersync.mixin;

import net.minecraft.entity.projectile.thrown.ThrownEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(ThrownEntity.class)
public class ThrownEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){
        LOGGER.info("Thrown entity tick detected");
    }

}
