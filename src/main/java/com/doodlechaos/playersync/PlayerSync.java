package com.doodlechaos.playersync;

import com.doodlechaos.playersync.Sync.InputEventContainers.*;
import com.doodlechaos.playersync.Sync.PlayerKeyframeV2;
import com.doodlechaos.playersync.Sync.PlayerRecorderV2;
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

import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PlayerSync implements ModInitializer {
	public static final String MOD_ID = "playersync";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean Recording = false;
	public static boolean PlayingBack = false;

	public static int playbackIndex = 0;

	public static int GetRecFrame(){
		return PlayerRecorderV2.getRecordedKeyframes().size();
	};

	public static boolean TickServerFlag = false;

	public static List<Integer> lastSimulatedKeys = new ArrayList<>();

	@Override
	public void onInitialize() {

		registerCommands();
		registerEvents();

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			String debugText = " recFrame: " + GetRecFrame() + " rec: " + Recording + " PlayingBack: " + PlayingBack + " playbackIndex: " + playbackIndex + " TickServerFlag: " + TickServerFlag;
			// Draw the text at position (10, 10) with white color (0xFFFFFF)
			matrixStack.drawText(client.textRenderer, debugText, 10, 10, 0xFFFFFF, false);
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

	public static PlayerKeyframeV2 GetCurKeyframe(){
		List<PlayerKeyframeV2> frames = PlayerRecorderV2.getRecordedKeyframes();

		if(playbackIndex < 0 || playbackIndex >= frames.size())
			return null;

		return frames.get(playbackIndex);
	}

	public static void setPlayerFromKeyframe(){

		MinecraftClient client = MinecraftClient.getInstance();

		if(client.player == null)
			return;

		if(playbackIndex >= PlayerRecorderV2.getRecordedKeyframes().size()){
			lastSimulatedKeys.clear();
			PlayingBack = false;
			playbackIndex = 0;
			client.player.sendMessage(Text.literal("Playback complete"), false);

			if(VideoRenderer.isRendering()){
				VideoRenderer.FinishRendering();
			}

			return;
		}

		PlayerKeyframeV2 keyframe = GetCurKeyframe();

		if(keyframe == null)
			return;

		if(playbackIndex == 0){
			client.player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
			client.player.setYaw(keyframe.playerYaw);
			client.player.setPitch(keyframe.playerPitch);
		}

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

	public static void SimulateInputsFromKeyframe(){
		PlayerKeyframeV2 keyframe = GetCurKeyframe();

		if(keyframe == null)
			return;

		MinecraftClient client = MinecraftClient.getInstance();
		long window = client.getWindow().getHandle();

		for (InputEvent ie : keyframe.recordedInputEvents) {
			ie.simulate(window, client);
		}

	}

}