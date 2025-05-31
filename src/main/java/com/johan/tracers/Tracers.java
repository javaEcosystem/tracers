package com.johan.tracers;

import com.johan.tracers.commands.*;
import com.johan.tracers.opengl.*;

// minecraft lib
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;

// forge lib
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
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
    public static RenderWorldLastEvent rwle;
    public static final String mod_id = "fps";
    public static final String version = "1.4";
    public static final String chatPrefix = EnumChatFormatting.DARK_RED + "[Tracers] ";

    // player-related variables
    public static double playerX;
    public static double playerY;
    public static double playerZ;

    // hud-related variables
    public static boolean showFps = true;
    public static boolean showPing = false;
    public static boolean showEntities = false;
    public static boolean showDirection = false;
    public static boolean showCoords = false;
    public static boolean showBiome = false;
    public static boolean showDimension = false;
    public static boolean showLightLvl = false;

    // opengl-related variables
    public static boolean showPlayerRays = false;
    public static boolean showItemRays = false;
    public static boolean showHitboxes = false;
    private final KeyBinding playerRaysKey = new KeyBinding("Toggle Player Rays", Keyboard.KEY_C, "Tracers");
    private final KeyBinding itemRaysKey = new KeyBinding("Toggle Item Rays", Keyboard.KEY_X, "Tracers");
    private final KeyBinding hitboxKey = new KeyBinding("Toggle Hitboxes", Keyboard.KEY_W, "Tracers");

    // friends-related variables
    public static Configuration config;
    public static ArrayList<String> friends = new ArrayList<String>();
    private static final File CONFIG_DIR = new File(Minecraft.getMinecraft().mcDataDir, "config");

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // init config
        config = new Configuration(new File(CONFIG_DIR, "fps.cfg"));

        // listen to @SubscribeEvents
        MinecraftForge.EVENT_BUS.register(this);

        // register keybindings
        ClientRegistry.registerKeyBinding(playerRaysKey);
        ClientRegistry.registerKeyBinding(itemRaysKey);
        ClientRegistry.registerKeyBinding(hitboxKey);

        // register commands
        ClientCommandHandler.instance.registerCommand(new AddFriend());
        ClientCommandHandler.instance.registerCommand(new Biome());
        ClientCommandHandler.instance.registerCommand(new Coords());
        ClientCommandHandler.instance.registerCommand(new Dimension());
        ClientCommandHandler.instance.registerCommand(new Direction());
        ClientCommandHandler.instance.registerCommand(new Entities());
        ClientCommandHandler.instance.registerCommand(new Fps());
        ClientCommandHandler.instance.registerCommand(new HitboxCmd());
        ClientCommandHandler.instance.registerCommand(new ItemRaysCmd());
        ClientCommandHandler.instance.registerCommand(new Light());
        ClientCommandHandler.instance.registerCommand(new ListFriends());
        ClientCommandHandler.instance.registerCommand(new Ping());
        ClientCommandHandler.instance.registerCommand(new PlayerRaysCmd());
        ClientCommandHandler.instance.registerCommand(new RemoveFriend());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        // load friends list when joining a server
        if (event.player == mc.thePlayer) {
            Friends.loadList();
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        // save friends list when leaving a server
        if (event.player == mc.thePlayer) {
            Friends.saveList();
        }
    }

    @SubscribeEvent
    public void hudEvent(RenderGameOverlayEvent.Post event)
    {
        // only render in the TEXT phase (where HUD text belongs)
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        // leave early if the world is not loaded
        if (mc.thePlayer==null || mc.theWorld==null || mc.getNetHandler()==null) return;

        // try-catch to handle errors
        try{ Hud.render(); } catch (Exception ignored){ }
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

        // Apply event to the global variable so that we can use it everywhere
        rwle = event;

        // get player position (interpolated for smooth rendering)
        playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * rwle.partialTicks;
        playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * rwle.partialTicks;
        playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * rwle.partialTicks;

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
            PlayerRays.render();
        }

        if (showItemRays)
        {
            ItemRays.render();
        }

        if(showHitboxes){
            Hitbox.render();
        }

        // restore default OpenGL state
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }
}