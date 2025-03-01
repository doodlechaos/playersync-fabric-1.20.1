package com.doodlechaos.playersync;

import com.doodlechaos.playersync.command.TimeCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class PlayerSync implements ModInitializer {
	public static final String MOD_ID = "playersync";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean ManualTime = false;

	public static float accumulatedPartialTick = 0.0f; //0 -> 1

	public static final float SEC_PER_TICK = 1.0f / 20.0f;  // 20 TPS
	public static final float SEC_PER_FRAME = 1.0f / 60.0f; // 60 FPS

	public static int TickClient = 0;
	public static int TickServer = 0;

	public static boolean AllowServerTickOnce = false;

	public static int FramesToProcess =0;

	public static long MasterTime = System.currentTimeMillis();;

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
		registerCommands();
		registerEvents();

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			String debugText = "Accumulated Partial Tick: " + accumulatedPartialTick + " ct: " + TickClient + " st: " + TickServer + " timeStopped: " + ManualTime + " masterTime: " + MasterTime;
			// Draw the text at position (10, 10) with white color (0xFFFFFF)
			matrixStack.drawText(client.textRenderer, debugText, 10, 10, 0xFFFFFF, false);
		});
	}

	private void registerCommands(){
		CommandRegistrationCallback.EVENT.register((dispatcher, registryaccess, environment) -> {
			TimeCommands.registerStopTime(dispatcher);
			TimeCommands.registerResumeTime(dispatcher);
			TimeCommands.registerStepTime(dispatcher);
		});

		LOGGER.info("Done registering commands");
	}

	private void registerEvents(){
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
	}


	private static final long WAIT_TIME_MS = 3000; // 3 seconds
	private static long lastTime = System.currentTimeMillis();

	public static void stepSyncFrame(MinecraftServer server)
	{
		if(!ManualTime)
			return;

/*		if(TickServer > 0 || TickClient > 0)
			return;

		long now = System.currentTimeMillis();

		if(now - lastTime < WAIT_TIME_MS)
			return;

		lastTime = now;

		LOGGER.info("STEPPING TIME MANUALLY: time:" + lastTime);

		float fractionOfTick = SEC_PER_FRAME / SEC_PER_TICK;
		accumulatedPartialTick += fractionOfTick;

		while(accumulatedPartialTick >= 1.0f){
			accumulatedPartialTick -= 1;

			TickServer++;
			TickClient++;
		}*/

		FramesToProcess--;
	}


	private void onEndClientTick(MinecraftClient minecraftClient)
	{
		if(TickClient > 0){
			TickClient--;
			LOGGER.info("Done manually ticking client");

		}

	}

	private void onEndServerTick(MinecraftServer minecraftServer)
	{
		if(TickServer > 0){
			TickServer--;
			LOGGER.info("Done manually ticking server");
		}


	}

}