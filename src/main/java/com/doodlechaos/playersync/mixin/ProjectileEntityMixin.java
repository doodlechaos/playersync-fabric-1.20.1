package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

//The goal of this mixin is to have deterministic physics for projectiles
@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * Overwrites the original setVelocity to apply a deterministic divergence.
     * Instead of using this.random.nextTriangular(...), we create a new Random seeded
     * with the current frame from PlayerTimeline. This makes the added divergence (the “fuzz”)
     * deterministic per frame.
     * @author
     * @reason
     */
    @Overwrite
    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        // Create a deterministic random based on the current frame number.
        long frame = PlayerTimeline.getFrame();
        PlayerSync.LOGGER.info("Launching projectile on frame: " + frame);
        Random deterministicRandom = new Random(frame);
        double scale = 0.0172275 * divergence;

        // Emulate a triangular distribution:
        double deltaX = (deterministicRandom.nextDouble() - deterministicRandom.nextDouble()) * scale;
        double deltaY = (deterministicRandom.nextDouble() - deterministicRandom.nextDouble()) * scale;
        double deltaZ = (deterministicRandom.nextDouble() - deterministicRandom.nextDouble()) * scale;

        Vec3d vec3d = new Vec3d(x, y, z)
                .normalize()
                .add(deltaX, deltaY, deltaZ)
                .multiply(speed);
        this.setVelocity(vec3d);

        double horizontal = vec3d.horizontalLength();
        this.setYaw((float) (MathHelper.atan2(vec3d.x, vec3d.z) * 180.0F / Math.PI));
        this.setPitch((float) (MathHelper.atan2(vec3d.y, horizontal) * 180.0F / Math.PI));
        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();
    }
}
