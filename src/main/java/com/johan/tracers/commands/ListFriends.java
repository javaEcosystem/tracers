package com.johan.tracers.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

import static com.johan.tracers.Tracers.friends;
import static com.johan.tracers.Tracers.chatPrefix;

public class ListFriends implements ICommand {
    @Override
    public String getCommandName() {
        return "lsfriends";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return chatPrefix + EnumChatFormatting.WHITE + "/lsfriends - Lists all players in your friends list";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("listfriends", "lsf");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (friends.isEmpty()) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.GRAY + "Your friends list is empty!"));
            return;
        }

        sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.GOLD + "=== Friends List (" + friends.size() + ") ==="));
        for (String friend : friends) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.AQUA + "- " + friend));
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
