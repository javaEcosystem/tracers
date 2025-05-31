package com.johan.tracers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;

import static com.johan.tracers.Tracers.mc;
import static com.johan.tracers.Tracers.showFps;
import static com.johan.tracers.Tracers.showPing;
import static com.johan.tracers.Tracers.showEntities;
import static com.johan.tracers.Tracers.showDirection;
import static com.johan.tracers.Tracers.showCoords;
import static com.johan.tracers.Tracers.showBiome;
import static com.johan.tracers.Tracers.showDimension;
import static com.johan.tracers.Tracers.showLightLvl;

public class Hud {

    private static final ArrayList<String> modules = new ArrayList<String>();
    private static final ArrayList<Boolean> toggle = new ArrayList<Boolean>();

    public static void render(){

        // get Minecraft's font renderer
        FontRenderer renderer = mc.fontRendererObj;

        // get player FPS
        int fps = Minecraft.getDebugFPS();

        //get player ping
        int ping;
        try{
            ping = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime();
        } catch (Exception e){
            ping = 0;
        }

        // get entities count
        int entities = mc.theWorld.loadedEntityList.size();

        // get current biome
        String biome = mc.thePlayer.worldObj.getBiomeGenForCoords(mc.thePlayer.getPosition()).biomeName;

        // get light level
        int light = mc.thePlayer.worldObj.getLightFromNeighbors(mc.thePlayer.getPosition());

        // get current dimension
        String dimension;
        switch (mc.thePlayer.dimension) {
            case -1:
                dimension = "Nether";
                break;
            case 0:
                dimension = "Overworld";
                break;
            default:
                dimension = "End";
                break;
        }

        // get player coordinates
        int x = (int) mc.thePlayer.posX;
        int y = (int) mc.thePlayer.posY;
        int z = (int) mc.thePlayer.posZ;

        // get facing direction
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int facingIndex = (int) ((mc.thePlayer.rotationYaw + 180 + 22.5) / 45.0) % 8;
        String facing = directions[facingIndex];

        // modules update
        modules.clear();
        modules.add("[FPS] " + fps);
        modules.add("[Ping] " + ping + " ms");
        modules.add("[Entities] " + entities);
        modules.add("[Facing] " + facing);
        modules.add("[XYZ] " + x + ", " + y + ", " + z);
        modules.add("[Biome] " + biome);
        modules.add("[Dim] " + dimension);
        modules.add("[Light] " + light);

        // boolean variables update
        toggle.clear();
        toggle.add(showFps);
        toggle.add(showPing);
        toggle.add(showEntities);
        toggle.add(showDirection);
        toggle.add(showCoords);
        toggle.add(showBiome);
        toggle.add(showDimension);
        toggle.add(showLightLvl);

        // save OpenGL state to prevent HUD corruption
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        // scale hud text based on mc gui scale
        switch (mc.gameSettings.guiScale) {
            // normal
            case 2:
                GlStateManager.scale(1.0f, 1.0f, 1.0f);
                break;
            // large
            case 3:
                GlStateManager.scale(0.75f, 0.75f, 1.0f);
                break;
            default:
                break;
        }

        // draw elements on HUD
        int skip = 0;
        for(int c=0; c<8; c++){
            if(toggle.get(c)) {renderer.drawString(modules.get(c), 5, 5+15*(c-skip), 0xFF1494);}
            else {skip+=1;}
        }

        // restore OpenGL state
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}
