package com.johan.tracers;

import com.johan.tracers.commands.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

@Mod(modid = Tracers.mod_id, version = Tracers.version)
public class Tracers
{
    // Global variables
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static final String mod_id = "tracers";
    public static final String version = "1.1";
    public static final String chatPrefix = EnumChatFormatting.DARK_RED + "[Tracers] ";
    public static ArrayList<String> friends = new ArrayList<String>();

    // Toggle key for tracer visibility
    private final KeyBinding tracers = new KeyBinding("Toggle Rays", Keyboard.KEY_C, "Tracers");

    // Controls whether tracer lines are rendered
    private boolean showRays = false;

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // Listen to @SubscribeEvents
        MinecraftForge.EVENT_BUS.register(this);

        // Register keybindings
        ClientRegistry.registerKeyBinding(tracers);

        // Register commands
        ClientCommandHandler.instance.registerCommand(new AddFriend());
        ClientCommandHandler.instance.registerCommand(new ListFriends());
        ClientCommandHandler.instance.registerCommand(new RemoveFriend());
    }

    @SubscribeEvent
    public void hudEvent(RenderGameOverlayEvent.Post event)
    {
        // Only render in the TEXT phase (where HUD text belongs)
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        // Get Minecraft's font renderer
        FontRenderer renderer = mc.fontRendererObj;

        // Get current Minecraft FPS
        int fps = Minecraft.getDebugFPS();

        // Save OpenGL state to prevent HUD corruption
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.scale(0.85f, 0.85f, 1.0f);

        // Draw FPS on HUD
        renderer.drawString("[FPS] " + fps, 350, 5, 0x00FF00);

        // Restore OpenGL state
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        // Toggle rays display
        if (tracers.isPressed()) { showRays = !showRays; }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event)
    {
        // Leave early if disabled
        if (!showRays || mc.theWorld == null || mc.thePlayer == null) return;

        // Get player position (interpolated for smooth rendering)
        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * event.partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * event.partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * event.partialTicks;

        // Prepare OpenGL state for 3D line rendering
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(0.1F);
        GL11.glBegin(GL11.GL_LINES);

        for (Entity entity : mc.theWorld.loadedEntityList)
        {
            // Rays target players only
            if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;

            // Calculate entity position relative to player
            double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - playerX;
            double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - playerY;
            double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - playerZ;

            // Green ray if friend, white otherwise
            if(friends.contains(entity.getName())){
                GL11.glColor4f(0f, 1f, 0f, 0.5f);
            } else{
                GL11.glColor4f(1f, 1f, 1f, 0.5f);
            }

            // Draw ray from player to entity
            GL11.glVertex3d(0, mc.thePlayer.height - 1, 0); // Player position
            GL11.glVertex3d(entityX, entityY+entity.getEyeHeight(), entityZ);
        }

        // Restore default OpenGL state
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
    }
}
