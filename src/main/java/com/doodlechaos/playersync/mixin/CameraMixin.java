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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Final @Shadow private Quaternionf rotation;

    @Inject(method = "update", at = @At("TAIL"))
    private void onCamEndUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        PlayerSync.camRot = rotation;
    }

    //Block the client from setting the camera pos relative to player if we're playing back and not detatched
    @Inject(method = "setPos(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void setPos(Vec3d pos, CallbackInfo ci) {
        if(PlayerTimeline.isPlaybackEnabled() && !PlayerTimeline.isPlayerDetatched()){
            ci.cancel();
            return;
        }

    }

    //Block the client from setting the camera rot relative to player if we're playing back and not detatched
    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void setRotation(float yaw, float pitch, CallbackInfo ci){
        if(PlayerTimeline.isPlaybackEnabled() && !PlayerTimeline.isPlayerDetatched()){
            ci.cancel();
            return;
        }
    }
}
