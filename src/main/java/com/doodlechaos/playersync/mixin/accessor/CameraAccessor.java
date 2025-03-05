package com.doodlechaos.playersync.mixin.accessor;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Camera.class)
public interface CameraAccessor {

    @Accessor("pos")
    void setRawCamPos(Vec3d pos);

    @Accessor("blockPos")
    BlockPos.Mutable myGetBlockPos();

    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("yaw")
    void setYaw(float yaw);

/*    @Accessor("horizontalPlane")
    Vector3f getHorizontalPlane();

    @Accessor("verticalPlane")
    Vector3f getVerticalPlane();

    @Accessor("diagonalPlane")
    Vector3f getDiagonalPlane();*/
}
