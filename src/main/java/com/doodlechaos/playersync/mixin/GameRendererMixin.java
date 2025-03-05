package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Final @Shadow private Camera camera;

    @Unique
    Vector3f euler = new Vector3f();

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
                    //shift = At.Shift.AFTER
                    shift = At.Shift.BY ,
                    by = 3
            )
    )
    private void injectRoll(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {

        if(!PlayerTimeline.isPlayingBack())
            return;
        //Vector3f euler = new Vector3f();
        camera.getRotation().getEulerAnglesYXZ(euler);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.toDegrees(euler.z)));
    }
}


