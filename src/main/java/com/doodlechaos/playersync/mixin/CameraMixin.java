package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Final @Shadow private Quaternionf rotation;

    @Inject(method = "update", at = @At("TAIL"))
    private void onCamEndUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        PlayerSync.camRot = rotation;
    }

    //Block setting the position if we're playing back
    @Inject(method = "setPos(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void setPos(Vec3d pos, CallbackInfo ci) {
        if(PlayerTimeline.isPlayingBack()){
            ci.cancel();
            return;
        }

    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void setRotation(float yaw, float pitch, CallbackInfo ci){
        if(PlayerTimeline.isPlayingBack()){
            ci.cancel();
            return;
        }
    }
}
