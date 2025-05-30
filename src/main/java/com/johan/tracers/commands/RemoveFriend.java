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

public class RemoveFriend implements ICommand {
    @Override
    public String getCommandName() {
        return "rmfriend";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return chatPrefix + EnumChatFormatting.WHITE + "/rmfriend <name> - Removes a player from your friends list";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("removefriend", "rmf");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
            return;
        }

        String friendName = args[0];
        if (friends.remove(friendName)) {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.RED + "Removed " + friendName + " from your friends list!"));
        } else {
            sender.addChatMessage(new ChatComponentText(chatPrefix + EnumChatFormatting.GRAY + friendName + " wasn't in your friends list!"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? friends : null;
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
