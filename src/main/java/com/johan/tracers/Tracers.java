package com.johan.tracers;

import com.johan.tracers.commands.*;

// minecraft lib
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

// forge lib
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

// lwjgl lib
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

// java lib
import java.io.File;
import java.util.*;

@Mod(modid = Tracers.mod_id, version = Tracers.version)
public class Tracers
{
    // global variables
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final String mod_id = "fps";
    public static final String version = "1.2";
    public static final String chatPrefix = EnumChatFormatting.DARK_RED + "[Tracers] ";
    public static ArrayList<String> friends = new ArrayList<String>();

    // boolean variables
    private boolean showPlayerRays = false;
    private boolean showItemRays = false;
    private boolean showHitboxes = false;
    private boolean hasRareEnchantedItem = false;

    // keybindings
    private final KeyBinding playerRaysKey = new KeyBinding("Toggle Player Rays", Keyboard.KEY_C, "Tracers");
    private final KeyBinding itemRaysKey = new KeyBinding("Toggle Item Rays", Keyboard.KEY_X, "Tracers");
    private final KeyBinding hitboxKey = new KeyBinding("Toggle Hitboxes", Keyboard.KEY_W, "Tracers");

    // distance threshold for grouping items (in blocks)
    private static final double ITEM_GROUP_DISTANCE = 5.0;

    // list of rare items
    private static final List<Item> RARE_ITEMS = Collections.unmodifiableList(Arrays.asList(Items.diamond_helmet, Items.diamond_boots, Items.iron_chestplate, Items.iron_sword));

    // list of items to skip
    private static final List<Item> ITEMS_TO_SKIP = Collections.unmodifiableList(Arrays.asList(Items.egg, Items.snowball, Items.gold_nugget, Items.gold_ingot, Items.iron_ingot));

    // setup the built-in config utility
    private static Configuration config;
    private static final File CONFIG_DIR = new File(Minecraft.getMinecraft().mcDataDir, "config");

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // initialize configuration
        config = new Configuration(new File(CONFIG_DIR, "tracers.cfg"));

        // listen to @SubscribeEvents
        MinecraftForge.EVENT_BUS.register(this);

        // register keybindings
        ClientRegistry.registerKeyBinding(playerRaysKey);
        ClientRegistry.registerKeyBinding(itemRaysKey);
        ClientRegistry.registerKeyBinding(hitboxKey);

        // register commands
        ClientCommandHandler.instance.registerCommand(new AddFriend());
        ClientCommandHandler.instance.registerCommand(new ListFriends());
        ClientCommandHandler.instance.registerCommand(new RemoveFriend());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        // load friends list when joining a server
        if (event.player == mc.thePlayer) {
            loadFriendsList();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        // save friends list when leaving a server
        if (event.player == mc.thePlayer) {
            saveFriendsList();
        }
    }

    @SubscribeEvent
    public void hudEvent(RenderGameOverlayEvent.Post event)
    {
        // only render in the TEXT phase (where HUD text belongs)
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        // get Minecraft's font renderer
        FontRenderer renderer = mc.fontRendererObj;

        // get current Minecraft FPS
        int fps = Minecraft.getDebugFPS();

        // save OpenGL state to prevent HUD corruption
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.scale(0.85f, 0.85f, 1.0f);

        // draw FPS on HUD
        renderer.drawString("[FPS] " + fps, 350, 5, 0x00FF00);

        // restore OpenGL state
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        // toggle displays
        if (playerRaysKey.isPressed()) { showPlayerRays = !showPlayerRays; }
        if (itemRaysKey.isPressed()) { showItemRays = !showItemRays; }
        if (hitboxKey.isPressed()) { showHitboxes = !showHitboxes; }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event)
    {
        // leave early if all features are disabled
        if ((!showPlayerRays && !showItemRays && !showHitboxes) || mc.theWorld == null || mc.thePlayer == null) return;

        // get player position (interpolated for smooth rendering)
        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        // prepare OpenGL state for 3D line rendering
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(0.1F);
        GL11.glBegin(GL11.GL_LINES);

        if (showPlayerRays)
        {
            for (Entity entity : mc.theWorld.loadedEntityList)
            {
                // rays target players only
                if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;

                // calculate entity position relative to player
                double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - playerX;
                double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - playerY;
                double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - playerZ;

                // green ray if friend, white otherwise
                if(friends.contains(entity.getName())){
                    GL11.glColor4f(0f, 1f, 0f, 0.5f);
                } else{
                    GL11.glColor4f(1f, 1f, 1f, 0.5f);
                }

                // draw ray from player to entity
                GL11.glVertex3d(0, mc.thePlayer.height - 1, 0);
                GL11.glVertex3d(entityX, entityY+entity.getEyeHeight(), entityZ);
            }
        }

        if (showItemRays)
        {
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

        if(showHitboxes){
            for (Entity entity : mc.theWorld.loadedEntityList)
            {
                // only players hitboxes are rendered
                if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;

                // calculate entity position relative to player
                double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - playerX;
                double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - playerY;
                double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - playerZ;

                // neon pink color for hitbox
                GL11.glColor4f(1f, 0.08f, 0.58f, 0.5f);

                // get the entity's bounding box
                AxisAlignedBB bb = entity.getEntityBoundingBox();

                // offset the bounding box by the entity's position
                bb = bb.offset(-entity.posX, -entity.posY, -entity.posZ);
                bb = bb.offset(entityX, entityY, entityZ);

                // draw the hitbox
                drawOutlinedBoundingBox(bb);
            }
        }

        // restore default OpenGL state
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }


    // helper methods

    private void checkRarity(EntityItem i){
        ItemStack s = i.getEntityItem();
        if (RARE_ITEMS.contains(s.getItem()) && s.hasEffect()) {
            hasRareEnchantedItem = true;
        }
    }

    private boolean isBuildingItem(ItemStack s) {
        if (s == null || s.getItem() == null) return false;

        // get the item from the stack
        Item item = s.getItem();

        // skip if item is a block
        return item instanceof ItemBlock;
    }

    private void loadFriendsList() {
        try {
            config.load();
            Property friendsProp = config.get("friends", "list", new String[0]);
            friends.clear();
            friends.addAll(Arrays.asList(friendsProp.getStringList()));
        } catch (Exception e) {
            System.err.println("Failed to load friends list: " + e.getMessage());
        }
    }

    private void saveFriendsList() {
        try {
            config.get("friends", "list", new String[0]).set(friends.toArray(new String[0]));
            config.save();
        } catch (Exception e) {
            System.err.println("Failed to save friends list: " + e.getMessage());
        }
    }

    private void drawOutlinedBoundingBox(AxisAlignedBB bb) {
        // bottom
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);

        // top
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

        // vertical edges
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);

        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
    }

}