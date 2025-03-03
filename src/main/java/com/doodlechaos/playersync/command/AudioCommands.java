package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.Sync.AudioSyncPlayer;
import com.doodlechaos.playersync.Sync.PlayerRecorderV2;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AudioCommands {

    public static void registerAudioCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("loadAudio")
                .executes(ctx -> {

                    AudioSyncPlayer.loadAudio("C:\\Users\\marky\\Downloads\\Alone.ogg");

                    ctx.getSource().sendMessage(Text.literal("Loaded Audio"));
                    return 1;
                })
        );

        dispatcher.register(literal("playAudio")
                .executes(ctx -> {

                    AudioSyncPlayer.playAudio();

                    ctx.getSource().sendMessage(Text.literal("Playing audio"));
                    return 1;
                })
        );

        dispatcher.register(literal("audioTime")
                .then(CommandManager.argument("time", FloatArgumentType.floatArg(0))
                        .executes(ctx -> {
                            float playheadTime = FloatArgumentType.getFloat(ctx, "time");
                            AudioSyncPlayer.setPlayheadTime(playheadTime);
                            ctx.getSource().sendMessage(Text.literal("Set audio playhead to " + playheadTime + " seconds"));
                            return 1;
                        })
                )
        );

    }
}
