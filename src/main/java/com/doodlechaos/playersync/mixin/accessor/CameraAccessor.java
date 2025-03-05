package com.doodlechaos.playersync.mixin.accessor;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {

    // Invoker for the protected setPos(Vec3d) method.
    @Invoker("setPos")
    void invokeSetPos(Vec3d pos);

    // Accessor for the private final "rotation" field.
    @Mutable
    @Accessor("rotation")
    Quaternionf getRotation();

    @Mutable
    @Accessor("rotation")
    void setRotation(Quaternionf rotation);

    // Accessors for pitch and yaw fields.
    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("yaw")
    void setYaw(float yaw);

    // Accessors for the basis (plane) vectors.
    @Accessor("horizontalPlane")
    Vector3f getHorizontalPlane();

    @Mutable
    @Accessor("horizontalPlane")
    void setHorizontalPlane(Vector3f horizontalPlane);

    @Accessor("verticalPlane")
    Vector3f getVerticalPlane();

    @Mutable
    @Accessor("verticalPlane")
    void setVerticalPlane(Vector3f verticalPlane);

    @Accessor("diagonalPlane")
    Vector3f getDiagonalPlane();

    @Mutable
    @Accessor("diagonalPlane")
    void setDiagonalPlane(Vector3f diagonalPlane);
}
