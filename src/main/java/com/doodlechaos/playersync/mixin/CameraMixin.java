package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.VideoRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
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

/*    @Final
    @Shadow
    private Quaternionf rotation;

    @Inject(method = "update", at = @At("TAIL"), cancellable = true)
    private void onCamEndUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        PlayerSync.camRot = rotation;
    }


    @ModifyArg(
        method = "setRotation", // or "update", depending on where your setRotation call occurs
        at = @At(
                value = "INVOKE",
                target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
                remap = false
        ),
        index = 2
    )
    private float injectPlayerSyncRoll(float originalRoll) {
        // Assuming PlayerSync.roll is in degrees.
        float rollInRadians = (float) Math.toRadians(PlayerSync.roll);
        return originalRoll + rollInRadians;
    }

    // Injection to override the camera's position and block position.
    @Inject(method = "setPos(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
    private void overrideSetPos(Vec3d ignoredPos, CallbackInfo ci) {
        // Instead of using the passed-in position, use PlayerSync.camPos.
        this.pos = PlayerSync.camPos;
        this.blockPos.set(PlayerSync.camPos.x, PlayerSync.camPos.y, PlayerSync.camPos.z);
        // Cancel the original method to prevent overriding our custom values.
        ci.cancel();
    }*/

}
