package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.Sync.PlayerTimeline;
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
                    if(PlayerTimeline.isRecording())
                    {
                        PlayerTimeline.setRecording(false);
                        ctx.getSource().sendMessage(Text.literal("Stopped Recording: " + PlayerTimeline.isRecording()));
                    }
                    else{
                        PlayerTimeline.setRecording(true);
                        ctx.getSource().sendMessage(Text.literal("Starting Recording: " + PlayerTimeline.isRecording()));
                    }

                    return 1;
                }));

        dispatcher.register(literal("clearRec")
                // No argument version – uses default value 10
                .executes(ctx -> {
                    PlayerTimeline.clearRecordedKeyframes();
                    ctx.getSource().sendMessage(Text.literal("Cleared recorded keyframes"));
                    return 1;
                })
        );

        dispatcher.register(literal("saveRec")
                .then(CommandManager.argument("filename", StringArgumentType.string())
                    .executes(ctx -> {
                        String filename = StringArgumentType.getString(ctx, "filename");

                        PlayerTimeline.SaveRecToFile(filename);
                        ctx.getSource().sendMessage(Text.literal("Saved " + filename));
                        return 1;
                    })
                )
        );

        dispatcher.register(literal("loadRec")
                .then(CommandManager.argument("filename", StringArgumentType.string())
                        .executes(ctx -> {
                            String filename = StringArgumentType.getString(ctx, "filename");

                            PlayerTimeline.LoadRecFromFile(filename);
                            ctx.getSource().sendMessage(Text.literal("Loaded " + filename));
                            return 1;
                        })
                )
        );

    }

}
