package com.doodlechaos.playersync;

import com.doodlechaos.playersync.Sync.AudioSync;
import com.doodlechaos.playersync.Sync.InputsManager;
import com.doodlechaos.playersync.Sync.PlayerKeyframe;
import com.doodlechaos.playersync.Sync.PlayerTimeline;
import com.doodlechaos.playersync.command.AudioCommands;
import com.doodlechaos.playersync.command.RecordCommands;
import com.doodlechaos.playersync.command.RenderCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.gui.DrawContext;

public class PlayerSync implements ModInitializer {
	public static final String MOD_ID = "playersync";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//public static boolean TickServerFlag = false;
	//private static volatile boolean serverTickComplete = false;
	private static volatile boolean waitingForServer = false;
	public static volatile boolean serverTickRequest = false;

	public static boolean isWaitingForServer(){return waitingForServer;}

	public static Quaternionf camRot = new Quaternionf();

	public static boolean OpenScreen = false;

	@Override
	public void onInitialize() {

		registerCommands();
		registerEvents();
		PlayerTimeline.registerDebugText();

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();

			// Use fixed-width formatting for tick delta (assumed to be a float)
			String debugText = String.format("PartialTick: %6.2f  waitingForServer: %s", client.getTickDelta(), waitingForServer);
			// Draw the text at position (10, 10) with white color (0xFFFFFF)
			matrixStack.drawText(client.textRenderer, debugText, 10, 10, 0xFFFFFF, false);

			Vector3f euler = new Vector3f();
			camRot.getEulerAnglesYXZ(euler);
			float xDeg = (float) Math.toDegrees(euler.x);
			float yDeg = (float) Math.toDegrees(euler.y);
			float zDeg = (float) Math.toDegrees(euler.z);

			// Format with fixed-width fields to avoid jitter (adjust width as needed)
			String camRotDegreesText = String.format("camRot: x=%7.2f°, y=%7.2f°, z=%7.2f°", xDeg, yDeg, zDeg);
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
		UseBlockCallback.EVENT.register(this::onUseBlock);
		AttackBlockCallback.EVENT.register(this::onAttackBlock);
		ClientPlayConnectionEvents.JOIN.register(this::onPlayerJoinWorld);

		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();


			if (PlayerTimeline.isRecording()) {
				int padding = 5;
				int dotSize = 15;

				// Calculate positions for the dot
				int x1 = screenWidth - dotSize - padding;
				int y1 = padding;
				int x2 = x1 + dotSize;
				int y2 = y1 + dotSize;

				DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
				drawContext.fill(x1, y1, x2, y2, 0xFFFF0000);
			}
		});
	}

	private void onPlayerJoinWorld(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient minecraftClient) {
		String audioPath = "C:\\Users\\marky\\Downloads\\mainThemeRemix.ogg";
		AudioSync.loadAudio(audioPath);
		if(minecraftClient.player != null)
			minecraftClient.player.sendMessage(Text.literal("loaded audio: " + audioPath));
	}

	private ActionResult onAttackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
		if (playerEntity.getStackInHand(hand).getItem() == Items.WOODEN_AXE) {
			InputsManager.mostRecentCommand = "//pos1 " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
			//playerEntity.sendMessage(Text.of("You set pos1 at: " + InputsManager.mostRecentCommand), false);
		}
		return ActionResult.PASS;
	}

	private ActionResult onUseBlock(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
		if (playerEntity.getStackInHand(hand).getItem() == Items.WOODEN_AXE) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			InputsManager.mostRecentCommand = "//pos2 " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
			//playerEntity.sendMessage(Text.of("You right-clicked a block with a wooden axe! " + InputsManager.mostRecentCommand), false);
		}
		return ActionResult.PASS;
	}

	public static void requestBlockingServerTick() {
		waitingForServer = true;
		serverTickRequest = true;
	}


	private void onStartClientTick(MinecraftClient client){

	}

	// Client tick for playback (single player)
	private void onEndClientTick(MinecraftClient client) {
		if (client.currentScreen == null && OpenScreen) {
			// Open the custom screen with an initial list of sample strings
			//Get the commands of the current keyframe
			PlayerKeyframe keyframe = PlayerTimeline.getCurKeyframe();

			if(keyframe != null)
				client.setScreen(new MyListScreen(client, keyframe.cmds));
		}
	}

	private void onEndServerTick(MinecraftServer minecraftServer)
	{
		waitingForServer = false;
		LOGGER.info("Finished ticking server on frame: " + PlayerTimeline.getFrame());
/*		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null) {
			for (Entity entity : client.world.getEntities()) {
				entity.prevX = entity.getX();
				entity.prevY = entity.getY();
				entity.prevZ = entity.getZ();
				entity.prevYaw = entity.getYaw();
				entity.prevPitch = entity.getPitch();
			}
		}*/
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