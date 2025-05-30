package com.johan.tracers;

import net.minecraftforge.common.config.Property;
import java.util.Arrays;

import static com.johan.tracers.Tracers.config;
import static com.johan.tracers.Tracers.friends;

public class Friends {

    public static void loadList() {
        try {
            config.load();
            Property friendsProp = config.get("friends", "list", new String[0]);
            friends.clear();
            friends.addAll(Arrays.asList(friendsProp.getStringList()));
        } catch (Exception e) {
            System.err.println("Failed to load friends list: " + e.getMessage());
        }
    }

    public static void saveList() {
        try {
            config.get("friends", "list", new String[0]).set(friends.toArray(new String[0]));
            config.save();
        } catch (Exception e) {
            System.err.println("Failed to save friends list: " + e.getMessage());
        }
    }

}
