package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.PlayerRecorder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class RecordCommands {

    public static void registerRecordCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("rec")
                // No argument version – uses default value 10
                .executes(ctx -> {
                    PlayerSync.Recording = !PlayerSync.Recording;
                    ctx.getSource().sendMessage(Text.literal("Set Recording: " + PlayerSync.Recording));
                    return 1;
                }));

        dispatcher.register(literal("clearRec")
                // No argument version – uses default value 10
                .executes(ctx -> {
                    PlayerRecorder.clearRecordedKeyframes();
                    ctx.getSource().sendMessage(Text.literal("Cleared recorded keyframes"));
                    return 1;
                })
        );

        dispatcher.register(literal("saveRec")
                .then(CommandManager.argument("filename", StringArgumentType.string())
                    .executes(ctx -> {
                        String filename = StringArgumentType.getString(ctx, "filename");

                        PlayerRecorder.SaveRecToFile(filename);
                        ctx.getSource().sendMessage(Text.literal("Saved " + filename));
                        return 1;
                    })
                )
        );

        dispatcher.register(literal("loadRec")
                .then(CommandManager.argument("filename", StringArgumentType.string())
                        .executes(ctx -> {
                            String filename = StringArgumentType.getString(ctx, "filename");

                            PlayerRecorder.LoadRecFromFile(filename);
                            ctx.getSource().sendMessage(Text.literal("Loaded " + filename));
                            return 1;
                        })
                )
        );

        dispatcher.register(literal("playRec")
                .executes(ctx -> {
                    if (!PlayerRecorder.getRecordedKeyframes().isEmpty()) {
                        PlayerSync.PlayingBack = !PlayerSync.PlayingBack;
                        PlayerSync.playbackIndex = 0;
                        ctx.getSource().sendMessage(Text.literal("Starting playback"));
                    } else {
                        ctx.getSource().sendMessage(Text.literal("No keyframes to play"));
                    }
                    return 1;
                })
        );

    }

}
