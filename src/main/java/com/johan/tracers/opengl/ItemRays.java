package com.johan.tracers.opengl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import java.util.*;

import static com.johan.tracers.Tracers.mc;
import static com.johan.tracers.Tracers.playerX;
import static com.johan.tracers.Tracers.playerY;
import static com.johan.tracers.Tracers.playerZ;

public class ItemRays {

    // boolean variable for rarity check
    private static boolean hasRareEnchantedItem = false;

    // distance threshold for grouping items (in blocks)
    private static final double ITEM_GROUP_DISTANCE = 5.0;

    // list of rare items
    private static final List<Item> RARE_ITEMS = Collections.unmodifiableList(Arrays.asList(Items.diamond_helmet, Items.diamond_boots, Items.iron_chestplate, Items.iron_sword));

    // list of items to skip
    private static final List<Item> ITEMS_TO_SKIP = Collections.unmodifiableList(Arrays.asList(Items.egg, Items.snowball, Items.gold_nugget, Items.gold_ingot, Items.iron_ingot));

    public static void render(){
        // list of processed items
        Set<EntityItem> processedItems = new HashSet<EntityItem>();

        // loop through all entities
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityItem)) continue;

            // convert entity to items and stack
            EntityItem entityItem = (EntityItem) entity;
            ItemStack stack = entityItem.getEntityItem();
            Item item = stack.getItem();

            // skip if item Y pos < 55
            if(entityItem.posY<55) continue;

            // skip some items due to servers kill effects
            if (isBuildingItem(stack) || ITEMS_TO_SKIP.contains(item)) continue;

            // skip if we've already processed this item as part of a group
            if (processedItems.contains(entityItem)) continue;

            // variables to track group properties
            Vec3 groupCenter = new Vec3(entityItem.posX, entityItem.posY, entityItem.posZ);
            int itemCount = 1;
            hasRareEnchantedItem = false;

            // check if the current item is rare+enchanted
            checkRarity(entityItem);

            // loop again to check for nearby items
            for (Entity otherEntity : mc.theWorld.loadedEntityList) {
                if (!(otherEntity instanceof EntityItem) || otherEntity == entityItem) continue;
                EntityItem otherItem = (EntityItem) otherEntity;

                // get distance between initial item and nearby item
                double distance = entityItem.getDistanceToEntity(otherItem);

                // compare it to our limit
                if (distance <= ITEM_GROUP_DISTANCE) {
                    groupCenter = new Vec3(
                            groupCenter.xCoord + otherItem.posX,
                            groupCenter.yCoord + otherItem.posY,
                            groupCenter.zCoord + otherItem.posZ
                    );
                    itemCount++;

                    // add the item to the list if close enough
                    processedItems.add(otherItem);

                    // check if this nearby item is rare+enchanted
                    checkRarity(otherItem);
                }
            }

            // calculate average position for the group
            groupCenter = new Vec3(
                    groupCenter.xCoord / itemCount,
                    groupCenter.yCoord / itemCount,
                    groupCenter.zCoord / itemCount
            );

            // calculate position relative to player
            double itemX = groupCenter.xCoord - playerX;
            double itemY = groupCenter.yCoord - playerY;
            double itemZ = groupCenter.zCoord - playerZ;

            // neon pink ray if rare, yellow otherwise
            if (hasRareEnchantedItem) {
                GL11.glColor4f(1f, 0.08f, 0.58f, 0.5f);
            } else {
                GL11.glColor4f(1f, 1f, 0f, 0.5f);
            }

            // draw ray from player to item group
            GL11.glVertex3d(0, mc.thePlayer.height - 1, 0);
            GL11.glVertex3d(itemX, itemY, itemZ);
        }
    }

    private static void checkRarity(EntityItem i){
        ItemStack s = i.getEntityItem();
        if (RARE_ITEMS.contains(s.getItem()) && s.hasEffect()) {
            hasRareEnchantedItem = true;
        }
    }

    private static boolean isBuildingItem(ItemStack s) {
        if (s == null || s.getItem() == null) return false;

        // get the item from the stack
        Item item = s.getItem();

        // skip if item is a block
        return item instanceof ItemBlock;
    }
}
