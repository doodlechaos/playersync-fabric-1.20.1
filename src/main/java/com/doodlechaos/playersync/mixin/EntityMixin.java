package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(Entity.class)
public abstract class EntityMixin {
/*
    @Inject(method = "getLerpedPos", at = @At("RETURN"))
    private void onGetLerpedPos(float delta, CallbackInfoReturnable<Vec3d> cir){

        if ((Object)this instanceof SnowballEntity) {
        }
        LOGGER.info("Get lerped pos: " + cir.getReturnValue());

    }

    @Inject(method = "updateTrackedPositionAndAngles", at = @At("HEAD"))
    private void onUpdateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo ci){

        if ((Object)this instanceof SnowballEntity) {
            LOGGER.info("[Entity Position Update] Tracking update: x={}, y={}, z={}, yaw={}, pitch={}, interpolationSteps={}, interpolate={}",
                    x, y, z, yaw, pitch, interpolationSteps, interpolate);
        }
    }

*/

/*    @Shadow private World world;
    @Shadow private Vec3d pos;
    @Shadow protected UUID uuid;

    // Intercept assignments in updatePosition and resetPosition.
    @Redirect(method = {"updatePosition", "resetPosition"},
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/Entity;prevX:D"))
    private void redirectPrevX(net.minecraft.entity.Entity instance, double value) {
        if (!PlayerTimeline.isLockstepMode()) {
            instance.prevX = value;
        }
    }

    @Redirect(method = {"updatePosition", "resetPosition"},
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/Entity;prevY:D"))
    private void redirectPrevY(net.minecraft.entity.Entity instance, double value) {
        if (!PlayerTimeline.isLockstepMode()) {
            instance.prevY = value;
        }
    }

    @Redirect(method = {"updatePosition", "resetPosition"},
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/Entity;prevZ:D"))
    private void redirectPrevZ(net.minecraft.entity.Entity instance, double value) {
        if (!PlayerTimeline.isLockstepMode()) {
            instance.prevZ = value;
        }
    }

    // Intercept assignments in updatePositionAndAngles and resetPosition.
    @Redirect(method = {"updatePositionAndAngles", "resetPosition"},
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/Entity;prevYaw:F"))
    private void redirectPrevYaw(net.minecraft.entity.Entity instance, float value) {
        if (!PlayerTimeline.isLockstepMode()) {
            instance.prevYaw = value;
        }
    }

    @Redirect(method = {"updatePositionAndAngles", "resetPosition"},
            at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/entity/Entity;prevPitch:F"))
    private void redirectPrevPitch(net.minecraft.entity.Entity instance, float value) {
        if (!PlayerTimeline.isLockstepMode()) {
            instance.prevPitch = value;
        }
    }
    @Inject(method = "baseTick", at = @At("TAIL"))
    public void afterBaseTick(CallbackInfo ci){

        if ((Object)this instanceof SnowballEntity) {
            LOGGER.info("Finished ticking snowball! Frame: " + PlayerTimeline.getFrame() + " pos: " + this.pos + " uuid: " + uuid);
        }
    }*/
}
