package com.johan.tracers.opengl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

import static com.johan.tracers.Tracers.mc;
import static com.johan.tracers.Tracers.rwle;
import static com.johan.tracers.Tracers.playerX;
import static com.johan.tracers.Tracers.playerY;
import static com.johan.tracers.Tracers.playerZ;
import static com.johan.tracers.Tracers.friends;

public class PlayerRays {

    public static void render(){
        for (Entity entity : mc.theWorld.loadedEntityList)
        {
            // rays target players only
            if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;

            // calculate entity position relative to player
            double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * rwle.partialTicks - playerX;
            double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * rwle.partialTicks - playerY;
            double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * rwle.partialTicks - playerZ;

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
}
