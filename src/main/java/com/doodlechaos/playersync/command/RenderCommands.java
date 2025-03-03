package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.VideoRenderer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class RenderCommands {

    public static void registerRenderCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("render")
                // No argument version – uses default value 10
                .executes(ctx -> {
                    VideoRenderer.StartRendering();
                    PlayerSync.PlayingBack = !PlayerSync.PlayingBack;
                    PlayerSync.playbackIndex = 0;

                    ctx.getSource().sendMessage(Text.literal("Started rendering!"));
                    return 1;
                }));
        }
    }
