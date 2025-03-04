package com.doodlechaos.playersync.mixin;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Keyboard.class)
public interface KeyboardAccessor {
    @Invoker("onChar")
    void callOnChar(long window, int codePoint, int modifiers);
}