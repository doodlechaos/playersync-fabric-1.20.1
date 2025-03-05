package com.doodlechaos.playersync;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.command.AudioCommands;
import com.doodlechaos.playersync.command.RecordCommands;
import com.doodlechaos.playersync.command.RenderCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerSync implements ModInitializer {
	public static final String MOD_ID = "playersync";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean TickServerFlag = false;

	public static Quaternionf camRot = new Quaternionf();

	//public static float roll;
	public static Vec3d camPos = new Vec3d(0, 80, 0);

	@Override
	public void onInitialize() {

		registerCommands();
		registerEvents();
		PlayerTimeline.registerDebugText();

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			String debugText = "PartialTick: " + client.getTickDelta() + " TickServerFlag: " + TickServerFlag;
			// Draw the text at position (10, 10) with white color (0xFFFFFF)
			matrixStack.drawText(client.textRenderer, debugText, 10, 10, 0xFFFFFF, false);

			Vector3f euler = new Vector3f();
			camRot.getEulerAnglesYXZ(euler);
			float xDeg = (float)Math.toDegrees(euler.x);
			float yDeg = (float)Math.toDegrees(euler.y);
			float zDeg = (float)Math.toDegrees(euler.z);
			String camRotDegreesText = String.format("camRot: x=%.2f°, y=%.2f°, z=%.2f°", xDeg, yDeg, zDeg);
			matrixStack.drawText(client.textRenderer, camRotDegreesText, 10, 30, 0xFFFFFF, false);

		});
	}

	private void registerCommands(){
		CommandRegistrationCallback.EVENT.register((dispatcher, registryaccess, environment) -> {
			RecordCommands.registerRecordCommands(dispatcher);
			RenderCommands.registerRenderCommands(dispatcher);
			AudioCommands.registerAudioCommands(dispatcher);
		});

		LOGGER.info("Done registering commands");
	}

	private void registerEvents(){
		ClientTickEvents.START_CLIENT_TICK.register(this::onStartClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
	}


	private void onStartClientTick(MinecraftClient client){

	}

	// Client tick for playback (single player)
	private void onEndClientTick(MinecraftClient client) {

	}

	private void onEndServerTick(MinecraftServer minecraftServer)
	{
		PlayerSync.TickServerFlag = false;
	}

	public static ServerPlayerEntity GetServerPlayer(){
		IntegratedServer minecraftServer = MinecraftClient.getInstance().getServer();
		if (minecraftServer == null) {
			LOGGER.error("Minecraft server is not available.");
			return null;
		}

		var playerList = minecraftServer.getPlayerManager().getPlayerList();

		if(playerList.isEmpty())
			return null;

		return playerList.get(0);
	}

}