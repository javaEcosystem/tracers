package com.johan.tracers.commands;

import com.johan.tracers.Tracers;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class ListFriends implements ICommand {
    @Override
    public String getCommandName() {
        return "lsfriends";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return Tracers.chatPrefix + EnumChatFormatting.WHITE + "/lsfriends - Lists all players in your friends list";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("listfriends", "lsf");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (Tracers.friends.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.GRAY + "Your friends list is empty!"));
            return;
        }

        sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.GOLD + "=== Friends List (" + Tracers.friends.size() + ") ==="));
        for (String friend : Tracers.friends) {
            sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.AQUA + "- " + friend));
        }
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
