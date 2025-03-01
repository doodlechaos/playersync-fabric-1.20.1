package com.doodlechaos.playersync.mixin;
import com.doodlechaos.playersync.PlayerSync;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){

        //LOGGER.info("ticking game renderer");
    }

}
