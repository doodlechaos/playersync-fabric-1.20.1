package com.doodlechaos.playersync.command;

import com.doodlechaos.playersync.PlayerSync;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TimeCommands {

    public static void registerStopTime(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("stopTime")
                // No argument version – uses default value 10
                .executes(ctx -> {
                    int defaultFrames = 10;
                    PlayerSync.ManualTime = true;
                    PlayerSync.FramesToProcess = defaultFrames;
                    ctx.getSource().sendMessage(Text.literal("Time stopped at "
                            + PlayerSync.accumulatedPartialTick + ". Frames to process: " + defaultFrames));
                    return 1;
                })
                // Version with a frames argument
                .then(argument("frames", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int framesToProcess = IntegerArgumentType.getInteger(ctx, "frames");
                            PlayerSync.ManualTime = true;
                            PlayerSync.FramesToProcess = framesToProcess;
                            ctx.getSource().sendMessage(Text.literal("Time stopped at "
                                    + PlayerSync.accumulatedPartialTick + ". Frames to process: " + framesToProcess));
                            return 1;
                        }))
        );
    }

    public static void registerResumeTime(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resumeTime")
                .executes(ctx -> {
                    PlayerSync.ManualTime = false;
                    ServerCommandSource source = ctx.getSource();
                    source.sendMessage(Text.literal("Time resumed at " + PlayerSync.accumulatedPartialTick));
                    return 1;
                }));
    }

    public static void registerStepTime(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(literal("stepTime")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();
                    PlayerSync.stepSyncFrame(source.getServer());
                    source.sendMessage(Text.literal("Time stepped to " + PlayerSync.accumulatedPartialTick));
                    return 1;
                }));
    }

}