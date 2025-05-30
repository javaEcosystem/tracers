package com.johan.tracers.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

import static com.johan.tracers.Tracers.friends;
import static com.johan.tracers.Tracers.chatPrefix;

public class AddFriend implements ICommand {
    @Override
    public String getCommandName() {
        return "addfriend";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return chatPrefix + EnumChatFormatting.WHITE + "/addfriend <name> - Adds a player to your friends list";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("adf");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
            return;
        }

        String friendName = args[0];
        if (friends.contains(friendName)) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.GRAY + friendName + " is already in your friends list!"));
        } else {
            friends.add(friendName);
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.GREEN + "Added " + friendName + " to your friends list!"));
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
        return index == 0;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
