package com.johan.tracers.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

import static com.johan.tracers.Tracers.showPing;
import static com.johan.tracers.Tracers.chatPrefix;

public class Ping implements ICommand {
    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return chatPrefix + EnumChatFormatting.WHITE + "/ping - Displays your current ping";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("png");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        showPing = !showPing;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
