package com.doodlechaos.playersync;

import com.doodlechaos.playersync.Sync.PlayerKeyframe;
import com.doodlechaos.playersync.Sync.PlayerRecorder;
import com.doodlechaos.playersync.command.RecordCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;

import net.minecraft.text.Text;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
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
		return PlayerRecorder.getRecordedKeyframes().size();
	};

	public static boolean TickServerFlag = false;

	public static long MasterTime = System.currentTimeMillis();;

	public static List<Integer> lastSimulatedKeys = new ArrayList<>();

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
		registerCommands();
		registerEvents();

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			String debugText = "masterTime: " + MasterTime + " recFrame: " + GetRecFrame() + " rec: " + Recording + " PlayingBack: " + PlayingBack + " playbackIndex: " + playbackIndex + " TickServerFlag: " + TickServerFlag;
			// Draw the text at position (10, 10) with white color (0xFFFFFF)
			matrixStack.drawText(client.textRenderer, debugText, 10, 10, 0xFFFFFF, false);
		});
	}

	private void registerCommands(){
		CommandRegistrationCallback.EVENT.register((dispatcher, registryaccess, environment) -> {
			RecordCommands.registerRecordCommands(dispatcher);
		});

		LOGGER.info("Done registering commands");
	}

	private void registerEvents(){
		ClientTickEvents.START_CLIENT_TICK.register(this::onStartClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
	}

	public static PlayerKeyframe GetCurKeyframe(){
		List<PlayerKeyframe> frames = PlayerRecorder.getRecordedKeyframes();

		if(playbackIndex < 0 || playbackIndex >= frames.size())
			return null;

		return frames.get(playbackIndex);
	}

	public static void setPlayerFromKeyframe(){

		MinecraftClient client = MinecraftClient.getInstance();

		if(client.player == null)
			return;

		if(playbackIndex >= PlayerRecorder.getRecordedKeyframes().size()){
			// End of playback: release any keys that remain pressed.
			for (int key : lastSimulatedKeys) {
				simulateKeyEvent(key, GLFW.GLFW_RELEASE);
			}
			lastSimulatedKeys.clear();
			PlayingBack = false;
			playbackIndex = 0;
			client.player.sendMessage(Text.literal("Playback complete"), false);
			return;
		}

		PlayerKeyframe keyframe = GetCurKeyframe();

		if(keyframe == null)
			return;

		//if(playbackIndex % 60 == 0){
			client.player.updatePosition(keyframe.playerPos.x, keyframe.playerPos.y, keyframe.playerPos.z);
		//}
		client.player.setYaw(keyframe.playerYaw);
		client.player.setPitch(keyframe.playerPitch);


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

	public static void SimulateKeystrokes(){
		//Simulate the pressed keys for the current frame here so it's early enough in the loop that they can get picked up by polling for this frame.
		PlayerKeyframe keyframe = GetCurKeyframe();

		if(keyframe == null)
			return;

		// Simulate keyboard events.
		// Compare recorded keys with the keys simulated last tick.
		List<Integer> currentKeys = keyframe.heldKeyboardKeys;

		// For keys that were pressed last tick but are not pressed now, simulate release.
		for (int key : lastSimulatedKeys) {
			if (!currentKeys.contains(key)) {
				simulateKeyEvent(key, GLFW.GLFW_RELEASE);
				LOGGER.info("releasing key " + key + " on frame: " + playbackIndex);
			}
		}
		// For keys that are pressed in this keyframe but weren't simulated last tick, simulate press.
		for (int key : currentKeys) {
			if (!lastSimulatedKeys.contains(key)) {
				simulateKeyEvent(key, GLFW.GLFW_PRESS);
				LOGGER.info("pressing key " + key + " on frame: " + playbackIndex);
			}
		}
		// Update lastSimulatedKeys for next tick.
		lastSimulatedKeys = new ArrayList<>(currentKeys);
	}

	public static void simulateKeyEvent(int keyCode, int action) {
		MinecraftClient client = MinecraftClient.getInstance();
		for (var keyBinding : client.options.allKeys) {
			if (keyBinding.getDefaultKey().getCode() == keyCode) {
				keyBinding.setPressed(action == GLFW.GLFW_PRESS);
				LOGGER.info("SIMULATING KEY EVENT: " + keyCode + " action: " + action);
			}
		}
	}
}