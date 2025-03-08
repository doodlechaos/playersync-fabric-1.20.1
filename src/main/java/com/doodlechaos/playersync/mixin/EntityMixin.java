package com.doodlechaos.playersync.mixin;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private World world;
    @Shadow private Vec3d pos;
    @Shadow protected UUID uuid;


/*    @Inject(method = "baseTick", at = @At("HEAD"), cancellable = true)
    public void onBaseTick(CallbackInfo ci){

        if(!PlayerTimeline.isLockstepMode())
            return;

        if ((Object)this instanceof PlayerEntity)
            return;

        //If we're here, it's a tick from the server
        if(PlayerTimeline.hasFrameChanged() && PlayerTimeline.isTickFrame()) //Don't cancel if the frame changed
            return;

        ci.cancel();
    }
    @Inject(method = "baseTick", at = @At("TAIL"))
    public void afterBaseTick(CallbackInfo ci){

        if ((Object)this instanceof SnowballEntity) {
            LOGGER.info("Finished ticking snowball! Frame: " + PlayerTimeline.getFrame() + " pos: " + this.pos + " uuid: " + uuid);

        }

    }*/
}
