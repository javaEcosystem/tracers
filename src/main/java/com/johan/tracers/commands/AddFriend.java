package com.johan.tracers.commands;

import com.johan.tracers.Tracers;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class AddFriend implements ICommand {
    @Override
    public String getCommandName() {
        return "addfriend";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return Tracers.chatPrefix + EnumChatFormatting.WHITE + "/addfriend <name> - Adds a player to your friends list";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("adf");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
            return;
        }

        String friendName = args[0];
        if (Tracers.friends.contains(friendName)) {
            sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.GRAY + friendName + " is already in your friends list!"));
        } else {
            Tracers.friends.add(friendName);
            sender.addChatMessage(new ChatComponentText(Tracers.chatPrefix + EnumChatFormatting.GREEN + "Added " + friendName + " to your friends list!"));
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
