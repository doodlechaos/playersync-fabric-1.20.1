package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.PlayerSync;
import com.doodlechaos.playersync.Sync.AudioSync;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.literal;

public class AudioCommands {

    public static void registerAudioCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("audio")
                .then(literal("load")
                        .executes(ctx -> {
                            AudioSync.loadAudio("C:\\Users\\marky\\Downloads\\Alone.ogg");
                            ctx.getSource().sendMessage(Text.literal("Loaded Audio"));
                            return 1;
                        })
                )
                .then(literal("play"))
                    .executes(ctx -> {
                        AudioSync.setPlaying(true);
                        ctx.getSource().sendMessage(Text.literal("Playing audio"));
                        return 1;
                    })
                .then(literal("pause"))
                    .executes(ctx -> {
                        AudioSync.setPlaying(false);
                        ctx.getSource().sendMessage(Text.literal("Paused audio"));
                        return 1;
                    })
                .then(CommandManager.argument("time", FloatArgumentType.floatArg(0))
                        .executes(ctx -> {
                            float playheadTime = FloatArgumentType.getFloat(ctx, "time");
                            AudioSync.setPlayheadTime(playheadTime);
                            ctx.getSource().sendMessage(Text.literal("Set audio playhead to " + playheadTime + " seconds"));
                            return 1;
                        })
                )
        );

        dispatcher.register(literal("roll")
                .then(CommandManager.argument("degrees", FloatArgumentType.floatArg(0))
                        .executes(ctx -> {
                            float degrees = FloatArgumentType.getFloat(ctx, "degrees");

                            PlayerSync.roll = degrees;

                            ctx.getSource().sendMessage(Text.literal("Set  roll to " + degrees + " degrees"));
                            return 1;
                        })
                )
        );

        dispatcher.register(literal("camPos")
                .then(CommandManager.argument("x", FloatArgumentType.floatArg(0))
                        .then(CommandManager.argument("y", FloatArgumentType.floatArg(0))
                                .then(CommandManager.argument("z", FloatArgumentType.floatArg(0))
                                    .executes(ctx -> {
                                        float x = FloatArgumentType.getFloat(ctx, "x");
                                        float y = FloatArgumentType.getFloat(ctx, "y");
                                        float z = FloatArgumentType.getFloat(ctx, "z");

                                        PlayerSync.camPos = new Vec3d(x, y, z);

                                        ctx.getSource().sendMessage(Text.literal("Set canPos to " + PlayerSync.camPos));
                                        return 1;
                                    })
                                )
                        )
                )
        );
    }
}
